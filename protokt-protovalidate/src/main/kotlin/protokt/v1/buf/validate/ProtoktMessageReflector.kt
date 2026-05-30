/*
 * Copyright (c) 2026 Toast, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package protokt.v1.buf.validate

import build.buf.protovalidate.MessageReflector
import build.buf.protovalidate.Value
import com.google.common.primitives.UnsignedLong
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.google.protobuf.DynamicMessage
import com.google.protobuf.MapEntry
import com.google.protobuf.UnsafeByteOperations
import com.google.protobuf.WireFormat
import dev.cel.common.values.CelByteString
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.fieldOptions
import protokt.v1.google.protobuf.getField
import protokt.v1.google.protobuf.hasField
import protokt.v1.google.protobuf.wrap
import com.google.protobuf.Message as JvmMessage

/**
 * A [MessageReflector] backed by a protokt [Message]. Exposes the message's fields to
 * protovalidate-java's evaluators via protokt reflection.
 */
internal class ProtoktMessageReflector(
    val message: Message,
    val descriptor: Descriptor,
    val context: RuntimeContext,
) : MessageReflector {
    override fun getDescriptorForType(): Descriptor = descriptor

    override fun hasField(field: FieldDescriptor) =
        message.hasField(field)

    override fun getField(field: FieldDescriptor): Value {
        val raw = message.getField(field)!!
        // Protokt's `wrap` annotation lets a MESSAGE-typed field surface as a scalar (e.g. an
        // Int32Value field returns a Kotlin Int). Native rule evaluators and Value#messageValue
        // expect a com.google.protobuf.Message for MESSAGE fields, so reverse the wrap when the
        // raw value isn't already a protokt Message.
        if (field.type == Type.MESSAGE && raw !is Message && raw !is List<*> && raw !is Map<*, *>) {
            val wrap = wrap(field, fieldOptions(field))
            if (wrap != null) {
                val onWire = context.unwrap(raw, field, wrap)
                return ProtoktObjectValue(field, context.convertValue(onWire), context)
            }
        }
        return ProtoktObjectValue(field, raw, context)
    }

    override fun celValue(): Any =
        ProtoktStructValue(message, descriptor, context)
}

internal class ProtoktObjectValue(
    private val fieldDescriptor: FieldDescriptor,
    private val value: Any,
    private val context: RuntimeContext,
) : Value {
    override fun fieldDescriptor() =
        fieldDescriptor

    // Native rule evaluators (NumericRulesEvaluator / BytesRulesEvaluator / EnumRulesEvaluator /
    // StringRulesEvaluator / RepeatedRulesEvaluator) cast rawValue() to the same Java boxed type
    // com.google.protobuf.Message#getField would return for the field's wire type. Translate
    // protokt's runtime types to that shape.
    override fun rawValue(): Any =
        rawValueOf(fieldDescriptor, value)

    override fun messageValue(): MessageReflector =
        when (value) {
            is Message -> ProtoktMessageReflector(value, fieldDescriptor.messageType, context)
            is JvmMessage -> JvmMessageReflector(value, fieldDescriptor.messageType, context)
            else -> error("messageValue called on non-message ${value::class}")
        }

    override fun repeatedValue() =
        (value as List<*>).map { ProtoktObjectValue(fieldDescriptor, it!!, context) }

    override fun mapValue(): Map<Value, Value> {
        val input = value as Map<*, *>
        val keyDesc = fieldDescriptor.messageType.findFieldByNumber(1)
        val valDesc = fieldDescriptor.messageType.findFieldByNumber(2)
        return input.entries.associate { (k, v) ->
            Pair(
                ProtoktObjectValue(keyDesc, k!!, context),
                ProtoktObjectValue(valDesc, v!!, context),
            )
        }
    }

    override fun celValue(): Any =
        when (value) {
            is Map<*, *> -> {
                val keyDesc = fieldDescriptor.messageType.findFieldByNumber(1)
                val valDesc = fieldDescriptor.messageType.findFieldByNumber(2)
                value.entries.associate { (k, v) ->
                    scalarCelValue(keyDesc, k!!) to scalarCelValue(valDesc, v!!)
                }
            }
            is List<*> -> value.map { scalarCelValue(fieldDescriptor, it!!) }
            else -> scalarCelValue(fieldDescriptor, value)
        }

    private fun scalarCelValue(fd: FieldDescriptor, raw: Any): Any =
        when (raw) {
            is Enum -> raw.value.toLong()
            is Float -> raw.toDouble()
            is Int -> raw.toLong()
            is UInt -> UnsignedLong.valueOf(raw.toLong())
            is ULong -> UnsignedLong.valueOf(raw.toLong())
            is Bytes -> CelByteString.of(raw.bytes)
            is Message -> wrapMessage(raw, fd.messageType, context)
            is JvmMessage -> raw
            else -> raw
        }

    override fun <T : Any> jvmValue(clazz: Class<T>): T? =
        context.convertValue(value)
            ?.let {
                when (it) {
                    is Int -> it.toLong()
                    is Float -> it.toDouble()
                    else -> it
                }
            }
            ?.let(clazz::cast)
}

// Native rule evaluators (NumericRulesEvaluator, BytesRulesEvaluator, etc.) consume rawValue() as
// the boxed Java type that com.google.protobuf.Message#getField returns for the field's wire type:
// Integer for int32/uint32/fixed32/sfixed32/sint32, Long for int64/uint64/fixed64/sfixed64/sint64,
// ByteString for bytes, EnumValueDescriptor for enums, and so on. Repeated fields surface as
// List<rawValueOf(elem)> and maps as Map<rawValueOf(k), rawValueOf(v)>. Translate from protokt's
// runtime types here.
private fun rawValueOf(field: FieldDescriptor, raw: Any): Any =
    when {
        raw is Map<*, *> -> mapEntriesOf(field, raw)
        raw is List<*> -> raw.map { rawValueOf(field, it!!) }
        raw is UInt -> raw.toInt()
        raw is ULong -> raw.toLong()
        raw is Enum -> field.enumType.findValueByNumberCreatingIfUnknown(raw.value)
        raw is Bytes -> UnsafeByteOperations.unsafeWrap(raw.asReadOnlyBuffer())
        else -> raw
    }

// Mirror com.google.protobuf.Message#getField for a map field: a list of synthetic MapEntry
// messages, one per pair, each carrying the key under field 1 and the value under field 2.
private fun mapEntriesOf(field: FieldDescriptor, raw: Map<*, *>): List<MapEntry<Any?, Any?>> {
    val entryDescriptor = field.messageType
    val keyDesc = entryDescriptor.findFieldByNumber(1)
    val valDesc = entryDescriptor.findFieldByNumber(2)
    @Suppress("UNCHECKED_CAST")
    val defaultEntry =
        MapEntry.newDefaultInstance(
            entryDescriptor,
            WireFormat.FieldType.valueOf(keyDesc.type.name),
            defaultForMapField(keyDesc),
            WireFormat.FieldType.valueOf(valDesc.type.name),
            defaultForMapField(valDesc),
        ) as MapEntry<Any?, Any?>
    return raw.entries.map { (k, v) ->
        defaultEntry.toBuilder()
            .setKey(adaptForMapStorage(keyDesc, k!!))
            .setValue(adaptForMapStorage(valDesc, v!!))
            .build()
    }
}

private fun adaptForMapStorage(fd: FieldDescriptor, raw: Any): Any =
    if (raw is Enum) raw.value else rawValueOf(fd, raw)

private fun defaultForMapField(fd: FieldDescriptor): Any? =
    when (fd.type) {
        Type.MESSAGE -> DynamicMessage.getDefaultInstance(fd.messageType)
        Type.ENUM -> fd.enumType.values.first().number
        else -> fd.defaultValue
    }

// Adapts a com.google.protobuf.Message (the result of unwrapping a protokt-side wrapper field)
// to the MessageReflector interface so native rule evaluators can navigate its fields.
internal class JvmMessageReflector(
    private val message: JvmMessage,
    private val descriptor: Descriptor,
    private val context: RuntimeContext,
) : MessageReflector {
    override fun getDescriptorForType(): Descriptor = descriptor

    override fun hasField(field: FieldDescriptor) =
        if (field.hasPresence()) message.hasField(field) else !field.isDefaultValue(message.getField(field))

    override fun getField(field: FieldDescriptor): Value =
        JvmObjectValue(field, message.getField(field), context)

    override fun celValue(): Any = message
}

private fun FieldDescriptor.isDefaultValue(value: Any?): Boolean =
    when {
        value == null -> true
        isRepeated -> (value as List<*>).isEmpty()
        else -> value == defaultValue
    }

internal class JvmObjectValue(
    private val fieldDescriptor: FieldDescriptor,
    private val value: Any,
    private val context: RuntimeContext,
) : Value {
    override fun fieldDescriptor() = fieldDescriptor

    override fun rawValue(): Any = value

    override fun messageValue(): MessageReflector =
        JvmMessageReflector(value as JvmMessage, fieldDescriptor.messageType, context)

    override fun repeatedValue() =
        (value as List<*>).map { JvmObjectValue(fieldDescriptor, it!!, context) }

    override fun mapValue(): Map<Value, Value> {
        val input = value as Map<*, *>
        val keyDesc = fieldDescriptor.messageType.findFieldByNumber(1)
        val valDesc = fieldDescriptor.messageType.findFieldByNumber(2)
        return input.entries.associate { (k, v) ->
            Pair(JvmObjectValue(keyDesc, k!!, context), JvmObjectValue(valDesc, v!!, context))
        }
    }

    override fun celValue(): Any = value

    override fun <T : Any> jvmValue(clazz: Class<T>): T? = clazz.cast(value)
}
