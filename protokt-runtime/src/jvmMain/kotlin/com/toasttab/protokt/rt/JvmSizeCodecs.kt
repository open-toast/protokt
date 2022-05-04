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

import com.google.protobuf.CodedOutputStream

actual fun sizeof(b: Bytes) = CodedOutputStream.computeByteArraySizeNoTag(b.value)
actual fun sizeof(b: ByteArray) = CodedOutputStream.computeByteArraySizeNoTag(b)
actual fun sizeof(s: String) = CodedOutputStream.computeStringSizeNoTag(s)
actual fun sizeof(b: Boolean) = CodedOutputStream.computeBoolSizeNoTag(b)
actual fun sizeof(l: Int64) = CodedOutputStream.computeInt64SizeNoTag(l.value)
actual fun sizeof(d: Double) = CodedOutputStream.computeDoubleSizeNoTag(d)
actual fun sizeof(f: Float) = CodedOutputStream.computeFloatSizeNoTag(f)
actual fun sizeof(i: Fixed32) = CodedOutputStream.computeFixed32SizeNoTag(i.value)
actual fun sizeof(l: Fixed64) = CodedOutputStream.computeFixed64SizeNoTag(l.value)
actual fun sizeof(i: SFixed32) = CodedOutputStream.computeSFixed32SizeNoTag(i.value)
actual fun sizeof(l: SFixed64) = CodedOutputStream.computeSFixed64SizeNoTag(l.value)
actual fun sizeof(i: Int32) = CodedOutputStream.computeInt32SizeNoTag(i.value)
actual fun sizeof(i: UInt32) = CodedOutputStream.computeUInt32SizeNoTag(i.value)
actual fun sizeof(i: SInt32) = CodedOutputStream.computeSInt32SizeNoTag(i.value)
actual fun sizeof(l: UInt64) = CodedOutputStream.computeUInt64SizeNoTag(l.value)
actual fun sizeof(l: SInt64): Int = CodedOutputStream.computeSInt64SizeNoTag(l.value)
actual fun sizeof(t: Tag) = CodedOutputStream.computeTagSize(t.value)
