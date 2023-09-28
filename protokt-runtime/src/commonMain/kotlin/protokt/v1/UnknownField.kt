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

import protokt.v1.SizeCodecs.sizeOf

class UnknownField
private constructor(
    val fieldNumber: UInt,
    val value: UnknownValue
) {
    companion object {
        fun varint(fieldNumber: UInt, varint: Long) =
            UnknownField(fieldNumber, VarintVal(varint.toULong()))

        fun fixed32(fieldNumber: UInt, fixed32: UInt) =
            UnknownField(fieldNumber, Fixed32Val(fixed32))

        fun fixed64(fieldNumber: UInt, fixed64: ULong) =
            UnknownField(fieldNumber, Fixed64Val(fixed64))

        fun lengthDelimited(fieldNumber: UInt, bytes: ByteArray) =
            UnknownField(fieldNumber, LengthDelimitedVal(Bytes(bytes)))
    }
}

interface UnknownValue {
    fun size(): Int
}

data class VarintVal(val value: ULong) : UnknownValue {
    override fun size() =
        sizeOf(value)
}

data class Fixed32Val(val value: UInt) : UnknownValue {
    override fun size() =
        4
}

data class Fixed64Val(val value: ULong) : UnknownValue {
    override fun size() =
        8
}

data class LengthDelimitedVal(val value: Bytes) : UnknownValue {
    override fun size() =
        sizeOf(value)
}
