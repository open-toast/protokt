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

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(t: Tag): Int = sizeof(UInt32(t.value shl 3 or 0))

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(enum: KtEnum) = sizeof(Int32(enum.value))

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(msg: KtMessage) = sizeof(UInt32(msg.messageSize)) + msg.messageSize

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(b: Bytes) = sizeof(b.value)

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(b: BytesSlice) = sizeof(UInt32(b.length)) + b.length

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(b: ByteArray) = sizeof(UInt32(b.size)) + b.size

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(l: Int64) = sizeof(UInt64(l.value))

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(i: SInt32) = sizeof(UInt32(i.value.zigZagEncoded))

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(l: SInt64) = sizeof(UInt64(l.value.zigZagEncoded))

private val Int.zigZagEncoded
    get() = (this shl 1) xor (this shr 31)

private val Long.zigZagEncoded
    get() = (this shl 1) xor (this shr 63)

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(i: Int32) =
    if (i.value >= 0) {
        sizeof(UInt32(i.value))
    } else {
        10
    }

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(i: UInt32) =
    when {
        i.value and (0.inv() shl 7) == 0 -> 1
        i.value and (0.inv() shl 14) == 0 -> 2
        i.value and (0.inv() shl 21) == 0 -> 3
        i.value and (0.inv() shl 28) == 0 -> 4
        else -> 5
    }

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(l: UInt64): Int {
    var value = l.value
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

@Deprecated("for backwards compatibility only")
@Suppress("UNUSED_PARAMETER")
fun sizeof(d: Double) = 8

@Deprecated("for backwards compatibility only")
@Suppress("UNUSED_PARAMETER")
fun sizeof(b: Boolean) = 1

@Deprecated("for backwards compatibility only")
@Suppress("UNUSED_PARAMETER")
fun sizeof(f: Float) = 4

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION", "UNUSED_PARAMETER")
fun sizeof(i: Fixed32) = 4

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION", "UNUSED_PARAMETER")
fun sizeof(l: Fixed64) = 8

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION", "UNUSED_PARAMETER")
fun sizeof(i: SFixed32) = 4

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION", "UNUSED_PARAMETER")
fun sizeof(l: SFixed64) = 8

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun sizeof(s: String): Int {
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
    return sizeof(UInt32(length)) + length
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

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
fun <K, V> sizeofMap(
    m: Map<K, V>,
    tag: Tag,
    sizeof: (K, V) -> Int
) =
    sizeof(tag).let { t ->
        m.entries.sumOf { (k, v) ->
            t + sizeof(k, v).let { s ->
                s + sizeof(UInt32(s))
            }
        }
    }
