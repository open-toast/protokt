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

fun sizeof(t: Tag): Int = sizeof(UInt32(t.value shl 3 or 0))
fun sizeof(enum: KtEnum) = sizeof(Int32(enum.value))
fun sizeof(msg: KtMessage) = sizeof(UInt32(msg.messageSize)) + msg.messageSize
fun sizeof(b: Bytes) = sizeof(b.value)
fun sizeof(b: BytesSlice) = sizeof(UInt32(b.length)) + b.length
fun sizeof(b: ByteArray) = sizeof(UInt32(b.size)) + b.size
fun sizeof(l: Int64) = sizeof(UInt64(l.value))
fun sizeof(i: SInt32) = sizeof(UInt32(i.value.zigZagEncoded))
fun sizeof(l: SInt64) = sizeof(UInt64(l.value.zigZagEncoded))

fun sizeof(i: Int32) =
    if (i.value >= 0) {
        sizeof(UInt32(i.value))
    } else {
        10
    }

fun sizeof(i: UInt32) =
    when {
        i.value and (0.inv() shl 7) == 0 -> 1
        i.value and (0.inv() shl 14) == 0 -> 2
        i.value and (0.inv() shl 21) == 0 -> 3
        i.value and (0.inv() shl 28) == 0 -> 4
        else -> 5
    }

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

@Suppress("UNUSED_PARAMETER") fun sizeof(d: Double) = 8
@Suppress("UNUSED_PARAMETER") fun sizeof(b: Boolean) = 1
@Suppress("UNUSED_PARAMETER") fun sizeof(f: Float) = 4
@Suppress("UNUSED_PARAMETER") fun sizeof(i: Fixed32) = 4
@Suppress("UNUSED_PARAMETER") fun sizeof(l: Fixed64) = 8
@Suppress("UNUSED_PARAMETER") fun sizeof(i: SFixed32) = 4
@Suppress("UNUSED_PARAMETER") fun sizeof(l: SFixed64) = 8

expect fun sizeof(s: String): Int

private val Int.zigZagEncoded
    get() = (this shl 1) xor (this shr 31)

private val Long.zigZagEncoded
    get() = (this shl 1) xor (this shr 63)

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
