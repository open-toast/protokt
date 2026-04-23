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

import protokt.v1.Writer
import kotlin.reflect.KClass

internal sealed class FieldType {
    open val kotlinRepresentation: KClass<*>? = null
    open val inlineRepresentation: KClass<*>? = null
    open val ktRepresentation: KClass<*>? = null

    sealed class Nonscalar(
        override val kotlinRepresentation: KClass<*>? = null,
        override val ktRepresentation: KClass<*>? = null
    ) : FieldType()

    object Enum : Nonscalar(ktRepresentation = protokt.v1.Enum::class)
    object Message : Nonscalar(ktRepresentation = protokt.v1.Message::class)
    object String : Nonscalar(kotlin.String::class)
    object Bytes : Nonscalar(protokt.v1.Bytes::class)

    sealed class Scalar(
        override val kotlinRepresentation: KClass<*>? = null
    ) : FieldType()

    object Bool : Scalar(Boolean::class)
    object Double : Scalar(kotlin.Double::class)
    object Float : Scalar(kotlin.Float::class)
    object Fixed32 : Scalar(UInt::class)
    object Fixed64 : Scalar(ULong::class)
    object Int32 : Scalar(Int::class)
    object Int64 : Scalar(Long::class)
    object SFixed32 : Scalar(Int::class)
    object SFixed64 : Scalar(Long::class)
    object SInt32 : Scalar(Int::class)
    object SInt64 : Scalar(Long::class)
    object UInt32 : Scalar(UInt::class)
    object UInt64 : Scalar(ULong::class)

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
            Fixed32 -> Writer::writeFixed32.name
            SFixed32 -> Writer::writeSFixed32.name
            UInt32 -> Writer::writeUInt32.name
            SInt32 -> Writer::writeSInt32.name
            Fixed64 -> Writer::writeFixed64.name
            SFixed64 -> Writer::writeSFixed64.name
            UInt64 -> Writer::writeUInt64.name
            SInt64 -> Writer::writeSInt64.name
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
        fun from(typeValue: Int) =
            when (typeValue) {
                1 -> Double
                2 -> Float
                3 -> Int64
                4 -> UInt64
                5 -> Int32
                6 -> Fixed64
                7 -> Fixed32
                8 -> Bool
                9 -> String
                11 -> Message
                12 -> Bytes
                13 -> UInt32
                14 -> Enum
                15 -> SFixed32
                16 -> SFixed64
                17 -> SInt32
                18 -> SInt64
                else -> error("Unknown type: $typeValue")
            }
    }
}
