/*
 * Copyright (c) 2026 Toast, Inc.
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

@OptIn(OnlyForUseByGeneratedProtoCode::class)
internal class KotlinWriter(
    private val buf: ByteArray
) : Writer {
    private var pos = 0

    override fun writeFixed32(i: UInt) =
        writeFixed32Bits(i)

    override fun writeSFixed32(i: Int) =
        writeFixed32Bits(i.toUInt())

    override fun writeUInt32(i: UInt) =
        writeRawVarint32(i.toInt())

    override fun writeSInt32(i: Int) =
        writeRawVarint32((i shl 1) xor (i shr 31))

    override fun writeFixed64(l: ULong) =
        writeFixed64Bits(l)

    override fun writeSFixed64(l: Long) =
        writeFixed64Bits(l.toULong())

    override fun writeUInt64(l: ULong) =
        writeRawVarint64(l.toLong())

    override fun writeSInt64(l: Long) =
        writeRawVarint64((l shl 1) xor (l shr 63))

    override fun write(i: Int) {
        if (i >= 0) {
            writeRawVarint32(i)
        } else {
            writeRawVarint64(i.toLong())
        }
    }

    override fun write(l: Long) =
        writeRawVarint64(l)

    override fun write(f: Float) =
        writeFixed32Bits(f.toRawBits().toUInt())

    override fun write(d: Double) =
        writeFixed64Bits(d.toRawBits().toULong())

    override fun write(s: String) {
        val length = utf8Length(s)
        writeRawVarint32(length)
        encodeUtf8Into(s, buf, pos)
        pos += length
    }

    override fun write(b: Boolean) =
        writeRawByte(if (b) 1 else 0)

    override fun write(b: ByteArray) {
        writeRawVarint32(b.size)
        b.copyInto(buf, pos)
        pos += b.size
    }

    override fun write(b: BytesSlice) {
        writeRawVarint32(b.length)
        b.array.copyInto(buf, pos, b.offset, b.offset + b.length)
        pos += b.length
    }

    private fun writeRawByte(value: Int) {
        buf[pos++] = value.toByte()
    }

    private fun writeRawVarint32(value: Int) {
        var v = value
        while (v and 0x7f.inv() != 0) {
            writeRawByte((v and 0x7f) or 0x80)
            v = v ushr 7
        }
        writeRawByte(v)
    }

    private fun writeRawVarint64(value: Long) {
        var v = value
        while (v and 0x7fL.inv() != 0L) {
            writeRawByte(((v and 0x7f) or 0x80).toInt())
            v = v ushr 7
        }
        writeRawByte(v.toInt())
    }

    private fun writeFixed32Bits(value: UInt) {
        val v = value.toInt()
        writeRawByte(v and 0xff)
        writeRawByte((v ushr 8) and 0xff)
        writeRawByte((v ushr 16) and 0xff)
        writeRawByte((v ushr 24) and 0xff)
    }

    override fun toByteArray(): ByteArray =
        buf

    private fun writeFixed64Bits(value: ULong) {
        val v = value.toLong()
        writeRawByte((v and 0xff).toInt())
        writeRawByte(((v ushr 8) and 0xff).toInt())
        writeRawByte(((v ushr 16) and 0xff).toInt())
        writeRawByte(((v ushr 24) and 0xff).toInt())
        writeRawByte(((v ushr 32) and 0xff).toInt())
        writeRawByte(((v ushr 40) and 0xff).toInt())
        writeRawByte(((v ushr 48) and 0xff).toInt())
        writeRawByte(((v ushr 56) and 0xff).toInt())
    }
}
