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

@OnlyForUseByGeneratedProtoCode
interface Writer {
    fun writeFixed32(i: UInt)
    fun writeUInt32(i: UInt)
    fun writeFixed64(l: ULong)
    fun writeUInt64(l: ULong)
    fun write(s: String)
    fun write(b: ByteArray)
    fun write(b: BytesSlice)

    fun writeSFixed32(i: Int) =
        writeFixed32(i.toUInt())

    fun writeSFixed64(l: Long) =
        writeFixed64(l.toULong())

    fun writeSInt32(i: Int) =
        writeUInt32(((i shl 1) xor (i shr 31)).toUInt())

    fun writeSInt64(l: Long) =
        writeUInt64(((l shl 1) xor (l shr 63)).toULong())

    fun write(f: Float) =
        writeFixed32(f.toRawBits().toUInt())

    fun write(d: Double) =
        writeFixed64(d.toRawBits().toULong())

    fun write(l: Long) =
        writeUInt64(l.toULong())

    fun write(i: Int) {
        if (i >= 0) {
            writeUInt32(i.toUInt())
        } else {
            writeUInt64(i.toLong().toULong())
        }
    }

    fun write(b: Boolean) =
        write(if (b) 1 else 0)

    fun write(b: Bytes) =
        write(b.value)

    fun writeTag(tag: UInt) =
        also { writeUInt32(tag) }

    fun write(e: Enum) =
        write(e.value)

    fun write(m: Message) {
        write(m.serializedSize())
        m.serialize(this)
    }

    fun writeUnknown(u: UnknownFieldSet) {
        u.unknownFields.forEach { (k, v) -> v.write(k, this) }
    }

    fun toByteArray(): ByteArray
}
