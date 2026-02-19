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

package protokt.v1

import protokt.v1.Sizes.sizeOf
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

class UnknownField private constructor(
    val fieldNumber: UInt,
    val value: UnknownValue
) {
    companion object {
        @JvmStatic
        fun varint(fieldNumber: UInt, varint: Long) =
            UnknownField(fieldNumber, VarintVal(varint.toULong()))

        @JvmStatic
        fun fixed32(fieldNumber: UInt, fixed32: UInt) =
            UnknownField(fieldNumber, Fixed32Val(fixed32))

        @JvmStatic
        fun fixed64(fieldNumber: UInt, fixed64: ULong) =
            UnknownField(fieldNumber, Fixed64Val(fixed64))

        @JvmStatic
        fun lengthDelimited(fieldNumber: UInt, bytes: ByteArray) =
            UnknownField(fieldNumber, LengthDelimitedVal(Bytes(bytes)))
    }
}

interface UnknownValue {
    fun size(): Int
}

@JvmInline
value class VarintVal(
    val value: ULong
) : UnknownValue {
    @OptIn(OnlyForUseByGeneratedProtoCode::class)
    override fun size() =
        sizeOf(value)
}

@JvmInline
value class Fixed32Val(
    val value: UInt
) : UnknownValue {
    override fun size() =
        4
}

@JvmInline
value class Fixed64Val(
    val value: ULong
) : UnknownValue {
    override fun size() =
        8
}

@JvmInline
value class LengthDelimitedVal(
    val value: Bytes
) : UnknownValue {
    @OptIn(OnlyForUseByGeneratedProtoCode::class)
    override fun size() =
        sizeOf(value)
}
