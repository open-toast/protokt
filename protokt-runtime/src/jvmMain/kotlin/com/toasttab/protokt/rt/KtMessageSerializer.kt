/*
 * Copyright (c) 2023 Toast, Inc.
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

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
interface KtMessageSerializer {
    fun write(i: Fixed32)
    fun write(i: SFixed32)
    fun write(i: UInt32)
    fun write(i: SInt32)
    fun write(i: Int32)
    fun write(l: Fixed64)
    fun write(l: SFixed64)
    fun write(l: UInt64)
    fun write(l: SInt64)
    fun write(l: Int64)
    fun write(f: Float)
    fun write(d: Double)
    fun write(s: String)
    fun write(b: Boolean)
    fun write(b: ByteArray)
    fun write(b: BytesSlice)

    fun write(b: Bytes) =
        write(b.value)

    fun write(t: Tag) =
        also { write(UInt32(t.value)) }

    fun write(e: KtEnum) =
        write(Int32(e.value))

    fun write(m: KtMessage) {
        write(Int32(m.messageSize))
        m.serialize(this)
    }

    fun writeUnknown(u: UnknownFieldSet) {
        u.unknownFields.forEach { (k, v) -> v.write(k, this) }
    }
}

@Suppress("DEPRECATION")
internal fun serializer(stream: CodedOutputStream): KtMessageSerializer {
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

        override fun write(b: BytesSlice) {
            stream.writeUInt32NoTag(b.length)
            stream.write(b.array, b.offset, b.length)
        }
    }
}
