/*
 * Copyright (c) 2023 Toast, Inc.
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

@file:JvmName("Messages")

package protokt.v1.google.protobuf

import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.google.protobuf.DynamicMessage
import protokt.v1.Beta
import protokt.v1.Bytes
import protokt.v1.Message

@Beta
fun Message.toDynamicMessage(context: RuntimeContext): DynamicMessage =
    context.convertValue(this) as DynamicMessage

@Beta
fun Message.hasField(field: FieldDescriptor): Boolean {
    val value = getField(field)

    return if (field.hasPresence()) {
        value != null
    } else {
        value != defaultValue(field)
    }
}

@Beta
fun Message.getField(field: FieldDescriptor) =
    ProtoktReflect.getField(this, field)

private fun defaultValue(field: FieldDescriptor) =
    when (field.type) {
        Type.UINT64, Type.FIXED64 -> 0uL
        Type.UINT32, Type.FIXED32 -> 0u
        Type.BYTES -> Bytes.empty()
        else -> field.defaultValue
    }
