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

import kotlin.reflect.KClass

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
class Bytes(internal val value: ByteArray) {
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

enum class PType(val kotlinRepresentation: KClass<*>? = null) {
    BOOL,
    BYTES,
    DOUBLE,
    ENUM,
    FIXED32(Fixed32::class),
    FIXED64(Fixed64::class),
    FLOAT,
    INT32(Int32::class),
    INT64(Int64::class),
    MESSAGE,
    SFIXED32(SFixed32::class),
    SFIXED64(SFixed64::class),
    SINT32(SInt32::class),
    SINT64(SInt64::class),
    STRING,
    UINT32(UInt32::class),
    UINT64(UInt64::class);

    val packed get() =
        this != BYTES &&
        this != MESSAGE &&
        this != STRING
}
