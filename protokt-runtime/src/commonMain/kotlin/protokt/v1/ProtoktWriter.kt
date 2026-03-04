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
internal class ProtoktWriter(
    private val buf: ByteArray
) : Writer {
    private var pos = 0

    override fun writeFixed32(i: UInt) =
        writeFixed32Bits(i)

    override fun writeUInt32(i: UInt) =
        writeRawVarint32(i.toInt())

    override fun writeFixed64(l: ULong) =
        writeFixed64Bits(l)

    override fun writeUInt64(l: ULong) =
        writeRawVarint64(l.toLong())

    // Reserve-and-backtrack: when the varint length prefix size is the same for
    // min (all ASCII) and max (all 3-byte) UTF-8 encodings, skip the measurement
    // pass - reserve space for the varint, encode directly, backfill the byte count.
    // See also protobuf-java's CodedOutputStream.writeStringNoTag.
    override fun write(s: String) {
        val minVarIntSize = computeVarint32Size(s.length)
        val maxVarIntSize = computeVarint32Size(s.length * 3)

        if (minVarIntSize == maxVarIntSize) {
            // Fast path: encode first, backfill length.
            val encodePos = pos + minVarIntSize
            val bytesWritten = encodeUtf8Into(s, buf, encodePos)
            writeRawVarint32(bytesWritten)
            pos = encodePos + bytesWritten
        } else {
            // Varint size depends on actual UTF-8 length; measure first.
            val length = utf8Length(s)
            writeRawVarint32(length)
            encodeUtf8Into(s, buf, pos)
            pos += length
        }
    }

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
        if (pos == buf.size) buf else buf.copyOfRange(0, pos)

    private fun computeVarint32Size(value: Int): Int {
        val clz = value.countLeadingZeroBits()
        return ((32 * 9 + 64) - (clz * 9)) ushr 6
    }

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
