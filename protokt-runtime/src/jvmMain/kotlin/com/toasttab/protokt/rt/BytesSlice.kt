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

package com.toasttab.protokt.rt

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
class BytesSlice(
    internal val array: ByteArray,
    internal val offset: Int,
    val length: Int,
) {
    constructor(array: ByteArray) : this(array, 0, array.size)

    fun isEmpty() =
        length == 0

    fun isNotEmpty() =
        length > 0

    private fun asSequence() =
        sequence {
            for (i in offset until length + offset) {
                yield(array[i])
            }
        }

    override fun equals(other: Any?) =
        equalsUsingSequence(other, BytesSlice::length, BytesSlice::asSequence)

    override fun hashCode() =
        hashCodeUsingSequence(asSequence())

    override fun toString() =
        asSequence().joinToString(prefix = "[", postfix = "]")

    companion object {
        private val EMPTY = BytesSlice(ByteArray(0), 0, 0)

        fun empty() =
            EMPTY
    }
}

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun BytesSlice.toBytes() =
    Bytes(array.sliceArray(offset until offset + length))
