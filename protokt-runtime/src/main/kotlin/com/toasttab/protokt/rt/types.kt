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

/*
 * Inline classes are experimental
 * see:
 * https://kotlinlang.org/docs/reference/inline-classes.html
 * https://kotlinexpertise.com/kotlin-inline-classes/
 */
inline class Tag(val value: Int)

inline class Int32(val value: Int)

inline class Fixed32(val value: Int)

inline class SFixed32(val value: Int)

inline class UInt32(val value: Int)

inline class SInt32(val value: Int)

inline class Int64(val value: Long)

inline class Fixed64(val value: Long)

inline class SFixed64(val value: Long)

inline class UInt64(val value: Long)

inline class SInt64(val value: Long)

/**
 * ByteArray wrapper to provide equality
 */
class Bytes(val value: ByteArray) {
    fun isNotEmpty() = value.isNotEmpty()
    fun isEmpty() = value.isEmpty()

    override fun equals(other: Any?) =
        other is Bytes && value.contentEquals(other.value)

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
    override fun toString(): String {
        return value.contentToString()
    }
    companion object {
        val empty = Bytes(ByteArray(0))
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
        val empty = BytesSlice(ByteArray(0), 0, 0)
    }
}

enum class PType {
    BOOL,
    BYTES,
    DOUBLE,
    ENUM,
    FIXED32,
    FIXED64,
    FLOAT,
    INT32,
    INT64,
    MESSAGE,
    SFIXED32,
    SFIXED64,
    SINT32,
    SINT64,
    STRING,
    UINT32,
    UINT64;

    val packed get() =
        this != BYTES &&
        this != MESSAGE &&
        this != STRING
}
