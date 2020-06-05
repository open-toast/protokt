/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.rt

inline class Tag(val value: Int)

interface Serialized {
    val wireFormat: Int
}

interface InstanceSerialized {
    val wireType: Serialized
}

interface DelegatingSerialized : Serialized, InstanceSerialized {
    override val wireFormat
        get() = wireType.wireFormat
}

interface Boxed : DelegatingSerialized {
    val value: Number
}

interface WireType0 : Serialized {
    override val wireFormat
        get() = 0
}

interface WireType1 : Serialized {
    override val wireFormat
        get() = 1
}

interface WireType2 : Serialized {
    override val wireFormat
        get() = 2
}

interface WireType5 : Serialized {
    override val wireFormat
        get() = 5
}

interface InstanceWithWireType2 : DelegatingSerialized {
    override val wireType
        get() = InstanceWithWireType2

    companion object : WireType2
}

interface BoxedInstanceType0 : Boxed {
    override val wireType
        get() = BoxedInstanceType0

    companion object : WireType0
}

interface BoxedInstanceType1 : Boxed {
    override val wireType
        get() = BoxedInstanceType1

    companion object : WireType1
}

interface BoxedInstanceType5 : Boxed {
    override val wireType
        get() = BoxedInstanceType5

    companion object : WireType5
}

inline class Int32(override val value: Int) : BoxedInstanceType0
inline class Int64(override val value: Long) : BoxedInstanceType0
inline class SInt32(override val value: Int) : BoxedInstanceType0
inline class SInt64(override val value: Long) : BoxedInstanceType0
inline class UInt32(override val value: Int) : BoxedInstanceType0
inline class UInt64(override val value: Long) : BoxedInstanceType0

inline class Fixed64(override val value: Long) : BoxedInstanceType1
inline class SFixed64(override val value: Long) : BoxedInstanceType1

inline class Fixed32(override val value: Int) : BoxedInstanceType5
inline class SFixed32(override val value: Int) : BoxedInstanceType5

class Bytes(internal val value: ByteArray) : InstanceWithWireType2 {
    val bytes
        get() = value.clone()

    fun isNotEmpty() =
        value.isNotEmpty()

    fun isEmpty() =
        value.isEmpty()

    override fun equals(other: Any?) =
        other is Bytes && value.contentEquals(other.value)

    override fun hashCode() =
        value.contentHashCode()

    override fun toString() =
        value.contentToString()

    companion object {
        private val EMPTY = Bytes(ByteArray(0))

        fun empty() =
            EMPTY
    }
}

class BytesSlice(
    internal val array: ByteArray,
    internal val offset: Int,
    val length: Int
) {
    constructor(array: ByteArray) : this(array, 0, array.size)

    fun isEmpty() =
        length == 0

    fun isNotEmpty() =
        length > 0

    private fun asSequence() = sequence {
        for (i in offset until length + offset) {
            yield(array[i])
        }
    }

    override fun equals(other: Any?) =
        other is BytesSlice &&
            length == other.length &&
            asSequence().zip(other.asSequence()).all { (l, r) -> l == r }

    override fun hashCode() =
        asSequence().fold(1) { hash, elt -> 31 * hash + elt }

    override fun toString() =
        asSequence().joinToString(prefix = "[", postfix = "]")

    companion object {
        private val EMPTY = BytesSlice(ByteArray(0), 0, 0)

        fun empty() =
            EMPTY
    }
}
