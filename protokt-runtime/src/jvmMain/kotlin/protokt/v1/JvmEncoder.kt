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

import com.google.protobuf.CodedOutputStream

internal fun serializer(stream: CodedOutputStream): Encoder {
    return object : Encoder {
        override fun writeFixed32(i: UInt) =
            stream.writeFixed32NoTag(i.toInt())

        override fun writeSFixed32(i: Int) =
            stream.writeSFixed32NoTag(i)

        override fun writeUInt32(i: UInt) =
            stream.writeUInt32NoTag(i.toInt())

        override fun writeSInt32(i: Int) =
            stream.writeSInt32NoTag(i)

        override fun writeFixed64(l: ULong) =
            stream.writeFixed64NoTag(l.toLong())

        override fun writeSFixed64(l: Long) =
            stream.writeSFixed64NoTag(l)

        override fun writeUInt64(l: ULong) =
            stream.writeUInt64NoTag(l.toLong())

        override fun writeSInt64(l: Long) =
            stream.writeSInt64NoTag(l)

        override fun write(i: Int) =
            stream.writeInt32NoTag(i)

        override fun write(l: Long) =
            stream.writeInt64NoTag(l)

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

        override fun write(b: BytesSlice) {
            stream.writeUInt32NoTag(b.length)
            stream.write(b.array, b.offset, b.length)
        }
    }
}
