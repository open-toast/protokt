/*
 * Copyright (c) 2024 Toast, Inc.
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

package protokt.v1.google.protobuf

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.UnsafeByteOperations
import protokt.v1.Beta
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.GeneratedMessage
import protokt.v1.Message
import kotlin.Any
import kotlin.reflect.full.findAnnotation

@Beta
class RuntimeContext(descriptors: Iterable<Descriptors.Descriptor>) {
    internal val descriptorsByTypeName = descriptors.associateBy { it.fullName }

    fun convertValue(value: Any) =
        when (value) {
            is Enum -> value.value

            is UInt -> value.toInt()

            is ULong -> value.toLong()

            is Message -> toDynamicMessage(value, this)

            is Bytes -> UnsafeByteOperations.unsafeWrap(value.asReadOnlyBuffer())

            // pray
            else -> value
        }
}

private fun toDynamicMessage(message: Message, context: RuntimeContext): DynamicMessage {
    val descriptor =
        context.descriptorsByTypeName
            .getValue(message::class.findAnnotation<GeneratedMessage>()!!.fullTypeName)

    return DynamicMessage.parseFrom(descriptor, message.serialize())
}
