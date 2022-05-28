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

fun sizeof(enum: KtEnum) = sizeof(Int32(enum.value))
fun sizeof(msg: KtMessage) = sizeof(UInt32(msg.messageSize)) + msg.messageSize
fun sizeof(b: Bytes) = sizeof(b.value)
fun sizeof(b: BytesSlice) = sizeof(UInt32(b.length)) + b.length
expect fun sizeof(b: ByteArray): Int
expect fun sizeof(s: String): Int
expect fun sizeof(b: Boolean): Int
expect fun sizeof(l: Int64): Int
expect fun sizeof(d: Double): Int
expect fun sizeof(f: Float): Int
expect fun sizeof(i: Fixed32): Int
expect fun sizeof(l: Fixed64): Int
expect fun sizeof(i: SFixed32): Int
expect fun sizeof(l: SFixed64): Int
expect fun sizeof(i: Int32): Int
expect fun sizeof(i: UInt32): Int
expect fun sizeof(i: SInt32): Int
expect fun sizeof(l: UInt64): Int
expect fun sizeof(l: SInt64): Int
expect fun sizeof(t: Tag): Int

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
