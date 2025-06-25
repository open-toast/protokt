/*
 * Copyright (c) 2025 Toast, Inc.
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

package build.buf.protovalidate

import com.google.common.primitives.UnsignedLong
import com.google.protobuf.Descriptors.FieldDescriptor
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.getField
import protokt.v1.google.protobuf.hasField

internal class ProtoktMessageLike(
    val message: Message,
    val context: RuntimeContext,
) : MessageReflector {
    override fun hasField(field: FieldDescriptor) =
        message.hasField(field)

    override fun getField(field: FieldDescriptor) =
        ProtoktObjectValue(
            field,
            message.getField(field)!!,
            context
        )
}

internal class ProtoktMessageValue(
    private val message: Message,
    private val context: RuntimeContext,
) : Value {
    override fun fieldDescriptor() =
        null

    override fun messageValue() =
        ProtoktMessageLike(message, context)

    override fun repeatedValue() =
        emptyList<Value>()

    override fun mapValue() =
        emptyMap<Value, Value>()

    override fun celValue() =
        context.convertValue(message)

    override fun <T : Any> jvmValue(clazz: Class<T>) =
        null
}

internal class ProtoktObjectValue(
    private val fieldDescriptor: FieldDescriptor,
    private val value: Any,
    private val context: RuntimeContext,
) : Value {
    override fun fieldDescriptor() =
        fieldDescriptor

    override fun messageValue(): MessageReflector =
        ProtoktMessageLike(value as Message, context)

    override fun repeatedValue() =
        (value as List<*>).map { ProtoktObjectValue(fieldDescriptor, it!!, context) }

    override fun mapValue(): Map<Value, Value> {
        val input = value as Map<*, *>

        val keyDesc = fieldDescriptor.messageType.findFieldByNumber(1)
        val valDesc = fieldDescriptor.messageType.findFieldByNumber(2)

        return input.entries.associate { (key, value) ->
            Pair(
                ProtoktObjectValue(keyDesc, key!!, context),
                ProtoktObjectValue(valDesc, value!!, context),
            )
        }
    }

    override fun celValue() =
        when (value) {
            is Enum -> value.value
            is UInt -> UnsignedLong.valueOf(value.toLong())
            is ULong -> UnsignedLong.valueOf(value.toLong())
            is Message, is Bytes -> context.convertValue(value)

            // pray
            else -> value
        }

    override fun <T : Any> jvmValue(clazz: Class<T>): T? =
        context.convertValue(value)?.let(clazz::cast)
}
