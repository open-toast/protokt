/*
 * Copyright (c) 2019. Toast Inc.
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

import com.google.protobuf.CodedOutputStream
import com.google.protobuf.MessageLite

fun sizeof(enum: KtEnum) = sizeof(Int32(enum.value))
fun sizeof(msg: KtMessage) = sizeof(UInt32(msg.messageSize)) + msg.messageSize
fun sizeof(msg: MessageLite) = CodedOutputStream.computeMessageSizeNoTag(msg)
fun sizeof(b: Bytes) = CodedOutputStream.computeByteArraySizeNoTag(b.value)
fun sizeof(b: BytesSlice) = sizeof(UInt32(b.length)) + b.length
fun sizeof(ba: ByteArray) = CodedOutputStream.computeByteArraySizeNoTag(ba)
fun sizeof(s: String) = CodedOutputStream.computeStringSizeNoTag(s)
fun sizeof(b: Boolean) = CodedOutputStream.computeBoolSizeNoTag(b)
fun sizeof(l: Int64) = CodedOutputStream.computeInt64SizeNoTag(l.value)
fun sizeof(d: Double) = CodedOutputStream.computeDoubleSizeNoTag(d)
fun sizeof(f: Float) = CodedOutputStream.computeFloatSizeNoTag(f)
fun sizeof(i: Fixed32) = CodedOutputStream.computeFixed32SizeNoTag(i.value)
fun sizeof(l: Fixed64) = CodedOutputStream.computeFixed64SizeNoTag(l.value)
fun sizeof(i: SFixed32) = CodedOutputStream.computeSFixed32SizeNoTag(i.value)
fun sizeof(l: SFixed64) = CodedOutputStream.computeSFixed64SizeNoTag(l.value)
fun sizeof(i: Int32) = CodedOutputStream.computeInt32SizeNoTag(i.value)
fun sizeof(i: UInt32) = CodedOutputStream.computeUInt32SizeNoTag(i.value)
fun sizeof(i: SInt32) = CodedOutputStream.computeSInt32SizeNoTag(i.value)
fun sizeof(i: UInt64) = CodedOutputStream.computeUInt64SizeNoTag(i.value)
fun sizeof(i: SInt64): Int = CodedOutputStream.computeSInt64SizeNoTag(i.value)
fun sizeof(t: Tag) = CodedOutputStream.computeTagSize(t.value)

fun <K, V> sizeofMap(
    m: Map<K, V>,
    tag: Tag,
    sizeof: (K, V) -> Int
) =
    sizeof(tag).let { t ->
        m.entries.sumBy { (k, v) ->
            t + sizeof(k, v).let {
                s -> s + sizeof(UInt32(s))
            }
        }
    }
