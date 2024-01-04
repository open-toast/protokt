/*
 * Copyright (c) 2020 Toast, Inc.
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

package protokt.v1.reflect

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type
import protokt.v1.KtEnum
import protokt.v1.KtMessage
import protokt.v1.KtMessageSerializer
import kotlin.reflect.KClass

internal sealed class FieldType {
    open val kotlinRepresentation: KClass<*>? = null
    open val inlineRepresentation: KClass<*>? = null
    open val ktRepresentation: KClass<*>? = null

    sealed class Nonscalar(
        override val kotlinRepresentation: KClass<*>? = null,
        override val ktRepresentation: KClass<*>? = null
    ) : FieldType()

    data object Enum : Nonscalar(ktRepresentation = KtEnum::class)
    data object Message : Nonscalar(ktRepresentation = KtMessage::class)
    data object String : Nonscalar(kotlin.String::class)
    data object Bytes : Nonscalar(protokt.v1.Bytes::class)

    sealed class Scalar(
        override val kotlinRepresentation: KClass<*>? = null
    ) : FieldType()

    data object Bool : Scalar(Boolean::class)
    data object Double : Scalar(kotlin.Double::class)
    data object Float : Scalar(kotlin.Float::class)
    data object Fixed32 : Scalar(UInt::class)
    data object Fixed64 : Scalar(ULong::class)
    data object Int32 : Scalar(Int::class)
    data object Int64 : Scalar(Long::class)
    data object SFixed32 : Scalar(Int::class)
    data object SFixed64 : Scalar(Long::class)
    data object SInt32 : Scalar(Int::class)
    data object SInt64 : Scalar(Long::class)
    data object UInt32 : Scalar(UInt::class)
    data object UInt64 : Scalar(ULong::class)

    val protoktFieldType
        get() = when (this) {
            is Bytes -> protokt.v1.Bytes::class
            else ->
                requireNotNull(kotlinRepresentation) {
                    "no protokt field type for $this"
                }
        }

    val packable
        get() = this !in setOf(Bytes, Message, String)

    val writeFn
        get() = when (this) {
            Fixed32 -> KtMessageSerializer::writeFixed32.name
            SFixed32 -> KtMessageSerializer::writeSFixed32.name
            UInt32 -> KtMessageSerializer::writeUInt32.name
            SInt32 -> KtMessageSerializer::writeSInt32.name
            Fixed64 -> KtMessageSerializer::writeFixed64.name
            SFixed64 -> KtMessageSerializer::writeSFixed64.name
            UInt64 -> KtMessageSerializer::writeUInt64.name
            SInt64 -> KtMessageSerializer::writeSInt64.name
            else -> "write"
        }

    val scalar
        get() = this is Scalar

    val wireType
        get() = when (this) {
            Bool,
            Enum,
            Int32,
            Int64,
            SInt32,
            SInt64,
            UInt32,
            UInt64 -> 0

            Double,
            Fixed64,
            SFixed64 -> 1

            Bytes,
            Message,
            String -> 2

            Float,
            Fixed32,
            SFixed32 -> 5
        }

    companion object {
        fun from(type: Type) =
            when (type) {
                Type.TYPE_BOOL -> Bool
                Type.TYPE_BYTES -> Bytes
                Type.TYPE_DOUBLE -> Double
                Type.TYPE_ENUM -> Enum
                Type.TYPE_FIXED32 -> Fixed32
                Type.TYPE_FIXED64 -> Fixed64
                Type.TYPE_FLOAT -> Float
                Type.TYPE_INT32 -> Int32
                Type.TYPE_INT64 -> Int64
                Type.TYPE_MESSAGE -> Message
                Type.TYPE_SFIXED32 -> SFixed32
                Type.TYPE_SFIXED64 -> SFixed64
                Type.TYPE_SINT32 -> SInt32
                Type.TYPE_SINT64 -> SInt64
                Type.TYPE_STRING -> String
                Type.TYPE_UINT32 -> UInt32
                Type.TYPE_UINT64 -> UInt64
                else -> error("Unknown type: $type")
            }
    }
}
