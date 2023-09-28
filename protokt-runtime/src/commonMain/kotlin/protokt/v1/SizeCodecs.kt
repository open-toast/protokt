/*
 * Copyright (c) 2019 Toast, Inc.
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

import kotlin.jvm.JvmStatic

object SizeCodecs {
    @JvmStatic
    fun sizeOf(enum: KtEnum) =
        sizeOf(enum.value)

    @JvmStatic
    fun sizeOf(msg: KtMessage) =
        sizeOf(msg.messageSize.toUInt()) + msg.messageSize

    @JvmStatic
    fun sizeOf(b: Bytes) =
        sizeOf(b.value)

    @JvmStatic
    fun sizeOf(b: BytesSlice) =
        sizeOf(b.length) + b.length

    @JvmStatic
    fun sizeOf(b: ByteArray) =
        sizeOf(b.size) + b.size

    @JvmStatic
    fun sizeOf(l: Long) =
        sizeOf(l.toULong())

    @JvmStatic
    fun sizeOfSInt32(i: Int) =
        sizeOf(i.zigZagEncoded.toUInt())

    @JvmStatic
    fun sizeOfSInt64(l: Long) =
        sizeOf(l.zigZagEncoded.toULong())

    private val Int.zigZagEncoded
        get() = (this shl 1) xor (this shr 31)

    private val Long.zigZagEncoded
        get() = (this shl 1) xor (this shr 63)

    @JvmStatic
    fun sizeOf(i: Int) =
        if (i >= 0) {
            sizeOf(i.toUInt())
        } else {
            10
        }

    @JvmStatic
    fun sizeOf(i: UInt) =
        when {
            i and (0.inv() shl 7).toUInt() == 0u -> 1
            i and (0.inv() shl 14).toUInt() == 0u -> 2
            i and (0.inv() shl 21).toUInt() == 0u -> 3
            i and (0.inv() shl 28).toUInt() == 0u -> 4
            else -> 5
        }

    @JvmStatic
    fun sizeOf(l: ULong): Int {
        var value = l.toLong()
        if (value and (0L.inv() shl 7) == 0L) {
            return 1
        }
        if (value < 0L) {
            return 10
        }
        var n = 2
        if (value and (0L.inv() shl 35) != 0L) {
            n += 4
            value = value ushr 28
        }
        if (value and (0L.inv() shl 21) != 0L) {
            n += 2
            value = value ushr 14
        }
        if (value and (0L.inv() shl 14) != 0L) {
            n += 1
        }
        return n
    }

    @JvmStatic
    fun sizeOf(s: String): Int {
        val length =
            Iterable { CodePointIterator(s) }
                .sumOf {
                    when (it) {
                        in 0..0x7f -> 1
                        in 0x80..0x7ff -> 2
                        in 0x800..0xffff -> 3
                        else -> 4
                    }.toInt()
                }
        return sizeOf(length) + length
    }

    private class CodePointIterator(
        private val s: String
    ) : Iterator<Int> {
        var pos = 0

        override fun hasNext() =
            pos < s.length

        override fun next(): Int {
            if (pos >= s.length) throw NoSuchElementException()

            val v = s[pos++]
            if (v.isHighSurrogate() && pos < s.length) {
                val l = s[pos]
                if (l.isLowSurrogate()) {
                    pos++
                    return 0x10000 + (v - 0xD800).code * 0x400 + (l - 0xDC00).code
                }
            }
            return v.code and 0xffff
        }
    }

    @JvmStatic
    fun <K, V> sizeOf(
        m: Map<K, V>,
        tag: UInt,
        sizeOf: (K, V) -> Int
    ) =
        sizeOf(tag).let { t ->
            m.entries.sumOf { (k, v) ->
                t + sizeOf(k, v).let { s ->
                    s + sizeOf(s)
                }
            }
        }
}
