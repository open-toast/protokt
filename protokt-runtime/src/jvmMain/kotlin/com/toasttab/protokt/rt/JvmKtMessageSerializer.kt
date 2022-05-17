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

import com.google.protobuf.CodedOutputStream

fun serializer(stream: CodedOutputStream): KtMessageSerializer {
    return object : KtMessageSerializer {
        override fun write(i: Fixed32) =
            stream.writeFixed32NoTag(i.value)

        override fun write(i: SFixed32) =
            stream.writeSFixed32NoTag(i.value)

        override fun write(i: UInt32) =
            stream.writeUInt32NoTag(i.value)

        override fun write(i: SInt32) =
            stream.writeSInt32NoTag(i.value)

        override fun write(i: Int32) =
            stream.writeInt32NoTag(i.value)

        override fun write(l: Fixed64) =
            stream.writeFixed64NoTag(l.value)

        override fun write(l: SFixed64) =
            stream.writeSFixed64NoTag(l.value)

        override fun write(l: UInt64) =
            stream.writeUInt64NoTag(l.value)

        override fun write(l: SInt64) =
            stream.writeSInt64NoTag(l.value)

        override fun write(l: Int64) =
            stream.writeInt64NoTag(l.value)

        override fun write(b: Boolean) =
            stream.writeBoolNoTag(b)

        override fun write(s: String) =
            stream.writeStringNoTag(s)

        override fun write(f: Float) =
            stream.writeFloatNoTag(f)

        override fun write(d: Double) =
            stream.writeDoubleNoTag(d)

        override fun write(b: ByteArray) =
            stream.writeByteArrayNoTag(b)

        override fun write(e: KtEnum) =
            stream.writeInt32NoTag(e.value)

        override fun write(m: KtMessage) {
            stream.writeUInt32NoTag(m.messageSize)
            m.serialize(this)
        }

        override fun write(b: BytesSlice) {
            stream.writeUInt32NoTag(b.length)
            stream.write(b.array, b.offset, b.length)
        }

        override fun writeUnknown(u: UnknownFieldSet) {
            u.unknownFields.forEach { (k, v) -> v.write(k, this) }
        }

        override fun write(t: Tag) =
            also { stream.writeUInt32NoTag(t.value) }
    }
}
