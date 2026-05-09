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

import com.google.common.primitives.UnsignedLong
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import dev.cel.common.types.CelType
import dev.cel.common.types.StructTypeReference
import dev.cel.common.values.BaseProtoCelValueConverter
import dev.cel.common.values.CelByteString
import dev.cel.common.values.StructValue
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.getField
import protokt.v1.google.protobuf.hasField
import java.util.Optional

/**
 * A [StructValue] over a protokt [Message]. Handed to CEL as the `this` variable so
 * evaluators can navigate protokt messages directly via reflection, without converting to
 * `com.google.protobuf.DynamicMessage` for the outer message.
 *
 * `value()` returns `this` so that `ProtoCelValueConverter.maybeUnwrap` leaves us as a
 * `StructValue` when selecting nested fields — a subsequent field selection then calls back
 * into `toRuntimeValue`, which recognizes `CelValue` and passes us through unchanged.
 *
 * For Well-Known Types (Duration, Timestamp, wrappers, FieldMask, Any, JSON Value/Struct/List),
 * we convert the protokt value to a `com.google.protobuf.Message` via [RuntimeContext.convertValue]
 * and then pre-unwrap through [BaseProtoCelValueConverter.toRuntimeValue] — this converts
 * Timestamp → Instant, Duration → java.time.Duration, wrappers → primitives, etc. CEL's
 * maybeUnwrap won't call toRuntimeValue for us on the way out, so we have to do it up-front.
 *
 * A null [message] represents a default (unset) message — [find] always returns empty and
 * [select] returns field defaults.
 */
internal class ProtoktStructValue(
    private val message: Message?,
    private val descriptor: Descriptor,
    private val context: RuntimeContext,
) : StructValue<String, ProtoktStructValue>() {

    private val celType: CelType = StructTypeReference.create(descriptor.fullName)

    override fun value(): ProtoktStructValue = this

    override fun celType(): CelType = celType

    override fun isZeroValue(): Boolean = message == null

    override fun select(field: String): Any {
        val fd = findField(field) ?: throw IllegalArgumentException(
            "field '$field' is not declared in message '${descriptor.fullName}'"
        )
        return fieldValue(fd)
    }

    override fun find(field: String): Optional<Any> {
        if (message == null) return Optional.empty()
        val fd = findField(field) ?: return Optional.empty()
        return if (message.hasField(fd)) Optional.of(fieldValue(fd)) else Optional.empty()
    }

    private fun findField(name: String): FieldDescriptor? =
        descriptor.findFieldByName(name) ?: descriptor.fields.firstOrNull { it.jsonName == name }

    private fun fieldValue(fd: FieldDescriptor): Any {
        val raw = message?.getField(fd)
        if (raw == null) {
            return defaultFor(fd)
        }
        return when {
            fd.isMapField -> mapValue(fd, raw as Map<*, *>)
            fd.isRepeated -> (raw as List<*>).map { scalarCelValue(fd, it!!) }
            else -> scalarCelValue(fd, raw)
        }
    }

    private fun mapValue(fd: FieldDescriptor, raw: Map<*, *>): Map<Any, Any> {
        val keyDesc = fd.messageType.findFieldByNumber(1)
        val valDesc = fd.messageType.findFieldByNumber(2)
        return raw.entries.associate { (k, v) ->
            scalarCelValue(keyDesc, k!!) to scalarCelValue(valDesc, v!!)
        }
    }

    private fun scalarCelValue(fd: FieldDescriptor, raw: Any): Any =
        when (raw) {
            is Enum -> raw.value.toLong()
            is Float -> raw.toDouble()
            is Int ->
                if (fd.type == Type.UINT32 || fd.type == Type.FIXED32) {
                    UnsignedLong.valueOf(raw.toLong() and 0xFFFFFFFFL)
                } else {
                    raw.toLong()
                }
            is UInt -> UnsignedLong.valueOf(raw.toLong() and 0xFFFFFFFFL)
            is Long ->
                if (fd.type == Type.UINT64 || fd.type == Type.FIXED64) {
                    UnsignedLong.fromLongBits(raw)
                } else {
                    raw
                }
            is ULong -> UnsignedLong.fromLongBits(raw.toLong())
            is Bytes -> CelByteString.of(raw.bytes)
            is Message -> wrapMessage(raw, fd.messageType, context)
            else -> raw
        }

    private fun defaultFor(fd: FieldDescriptor): Any =
        when {
            fd.isMapField -> emptyMap<Any, Any>()
            fd.isRepeated -> emptyList<Any>()
            fd.type == Type.MESSAGE ->
                // Unset message fields materialize as a default ProtoktStructValue.
                if (WellKnownTypes.isWellKnown(fd.messageType.fullName)) {
                    WellKnownTypes.unwrap(
                        com.google.protobuf.DynamicMessage.getDefaultInstance(fd.messageType)
                    )
                } else {
                    ProtoktStructValue(null, fd.messageType, context)
                }
            else -> fd.defaultValue
        }
}

internal object WellKnownTypes {
    private val NAMES = setOf(
        "Any",
        "Duration",
        "Timestamp",
        "FieldMask",
        "Empty",
        "Value",
        "Struct",
        "ListValue",
        "BoolValue",
        "BytesValue",
        "DoubleValue",
        "FloatValue",
        "Int32Value",
        "Int64Value",
        "StringValue",
        "UInt32Value",
        "UInt64Value",
    )

    fun isWellKnown(typeName: String): Boolean =
        typeName.startsWith("google.protobuf.") && typeName.substringAfterLast('.') in NAMES

    private val converter: BaseProtoCelValueConverter = object : BaseProtoCelValueConverter() {}

    fun unwrap(dynamicMessage: Any): Any = converter.toRuntimeValue(dynamicMessage)
}

internal fun wrapMessage(
    raw: Message,
    messageType: Descriptor,
    context: RuntimeContext,
): Any =
    if (WellKnownTypes.isWellKnown(messageType.fullName)) {
        WellKnownTypes.unwrap(context.convertValue(raw))
    } else {
        ProtoktStructValue(raw, messageType, context)
    }
