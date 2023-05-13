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

package com.toasttab.protokt.v1

class UnknownField
private constructor(
    val fieldNumber: Int,
    val value: UnknownValue
) {
    companion object {
        fun varint(fieldNumber: Int, varint: Long) =
            UnknownField(fieldNumber, VarintVal(UInt64(varint.toULong())))

        fun fixed32(fieldNumber: Int, fixed32: UInt) =
            UnknownField(fieldNumber, Fixed32Val(Fixed32(fixed32)))

        fun fixed64(fieldNumber: Int, fixed64: ULong) =
            UnknownField(fieldNumber, Fixed64Val(Fixed64(fixed64)))

        fun lengthDelimited(fieldNumber: Int, bytes: ByteArray) =
            UnknownField(fieldNumber, LengthDelimitedVal(Bytes(bytes)))
    }
}

interface UnknownValue {
    fun size(): Int
}

data class VarintVal(val value: UInt64) : UnknownValue {
    override fun size() =
        sizeof(value)
}

data class Fixed32Val(val value: Fixed32) : UnknownValue {
    override fun size() =
        sizeof(value)
}

data class Fixed64Val(val value: Fixed64) : UnknownValue {
    override fun size() =
        sizeof(value)
}

data class LengthDelimitedVal(val value: Bytes) : UnknownValue {
    override fun size() =
        sizeof(value)
}
