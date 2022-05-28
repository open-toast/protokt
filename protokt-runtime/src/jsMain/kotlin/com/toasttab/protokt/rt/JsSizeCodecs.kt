/*
 * Copyright (c) 2022 Toast Inc.
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

actual fun sizeof(b: ByteArray): Int =
    sizeof(UInt32(b.size)) + b.size

actual fun sizeof(s: String) =
    util.utf8.length(s).let { it + sizeof(UInt32(it)) }

actual fun sizeof(b: Boolean) = 1

actual fun sizeof(l: Int64) =
    sizeof(UInt64(l.value))

actual fun sizeof(d: Double) = 8

actual fun sizeof(f: Float) = 4

actual fun sizeof(i: Fixed32) = 4

actual fun sizeof(l: Fixed64) = 8

actual fun sizeof(i: SFixed32) = 4

actual fun sizeof(l: SFixed64) = 8

actual fun sizeof(i: Int32) =
    if (i.value >= 0) {
        sizeof(UInt32(i.value))
    } else {
        10
    }

actual fun sizeof(i: SInt32) =
    sizeof(UInt32(i.value.zigZagEncoded))

actual fun sizeof(l: UInt64): Int {
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

actual fun sizeof(l: SInt64): Int =
    sizeof(UInt64(l.value))

actual fun sizeof(t: Tag): Int =
    sizeof(UInt32(t.value shl 3 or 0))

actual fun sizeof(i: UInt32) =
    when {
        i.value and (0.inv() shl 7) == 0 -> 1
        i.value and (0.inv() shl 14) == 0 -> 2
        i.value and (0.inv() shl 21) == 0 -> 3
        i.value and (0.inv() shl 28) == 0 -> 4
        else -> 5
    }
