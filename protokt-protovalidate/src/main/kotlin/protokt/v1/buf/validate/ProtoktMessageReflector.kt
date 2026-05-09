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
import dev.cel.common.values.CelByteString
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.getField
import protokt.v1.google.protobuf.hasField

/**
 * A [MessageReflector] backed by a protokt [Message]. Exposes the message's fields to
 * protovalidate-java's evaluators via protokt reflection.
 */
internal class ProtoktMessageReflector(
    val message: Message,
    val descriptor: Descriptor,
    val context: RuntimeContext,
) : MessageReflector {
    override fun hasField(field: FieldDescriptor) =
        message.hasField(field)

    override fun getField(field: FieldDescriptor): Value =
        ProtoktObjectValue(field, message.getField(field)!!, context)

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

    override fun messageValue(): MessageReflector =
        ProtoktMessageReflector(value as Message, fieldDescriptor.messageType, context)

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
