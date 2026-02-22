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

import kotlinx.io.Buffer
import kotlinx.io.UnsafeIoApi
import kotlinx.io.unsafe.UnsafeBufferOperations
import kotlinx.io.writeString

// Writes protobuf wire format into a Buffer using UnsafeBufferOperations for
// direct segment access. Each varint or fixed-width write is a single
// writeToTail call with direct byte array writes, avoiding the per-byte
// virtual dispatch chain (Sink.writeByte -> Buffer.writeByte -> Segment) and
// segment pool operations that dominate small-field workloads.
//
// The caller (KotlinCodec.serialize) transfers the buffer to the actual Sink
// after serialization, moving segment pointers rather than copying bytes.
@OptIn(OnlyForUseByGeneratedProtoCode::class, UnsafeIoApi::class)
internal class KotlinSinkWriter(
    private val buffer: Buffer
) : Writer {
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

    // Encode into a temporary Buffer to learn the UTF-8 byte count without a
    // separate measurement pass. The transfer to buffer moves segment pointers
    // rather than copying bytes; the only overhead is the Buffer allocation and
    // segment pool checkout. Reserve-and-backtrack (as in KotlinWriter.write(String))
    // isn't possible on a linked segment chain.
    override fun write(s: String) {
        val tmp = Buffer()
        tmp.writeString(s)
        writeRawVarint32(tmp.size.toInt())
        buffer.write(tmp, tmp.size)
    }

    override fun write(b: Boolean) =
        writeRawByte(if (b) 1 else 0)

    override fun write(b: ByteArray) {
        writeRawVarint32(b.size)
        buffer.write(b)
    }

    override fun write(b: BytesSlice) {
        writeRawVarint32(b.length)
        buffer.write(b.array, b.offset, b.offset + b.length)
    }

    private fun writeRawByte(value: Int) {
        UnsafeBufferOperations.writeToTail(buffer, 1) { bytes, start, _ ->
            bytes[start] = value.toByte()
            1
        }
    }

    private fun writeRawVarint32(value: Int) {
        UnsafeBufferOperations.writeToTail(buffer, 5) { bytes, start, _ ->
            var v = value
            var pos = start
            while (v and 0x7f.inv() != 0) {
                bytes[pos++] = ((v and 0x7f) or 0x80).toByte()
                v = v ushr 7
            }
            bytes[pos++] = v.toByte()
            pos - start
        }
    }

    private fun writeRawVarint64(value: Long) {
        UnsafeBufferOperations.writeToTail(buffer, 10) { bytes, start, _ ->
            var v = value
            var pos = start
            while (v and 0x7fL.inv() != 0L) {
                bytes[pos++] = ((v and 0x7f) or 0x80).toByte()
                v = v ushr 7
            }
            bytes[pos++] = v.toByte()
            pos - start
        }
    }

    private fun writeFixed32Bits(value: UInt) {
        UnsafeBufferOperations.writeToTail(buffer, 4) { bytes, start, _ ->
            val v = value.toInt()
            bytes[start] = (v and 0xff).toByte()
            bytes[start + 1] = ((v ushr 8) and 0xff).toByte()
            bytes[start + 2] = ((v ushr 16) and 0xff).toByte()
            bytes[start + 3] = ((v ushr 24) and 0xff).toByte()
            4
        }
    }

    private fun writeFixed64Bits(value: ULong) {
        UnsafeBufferOperations.writeToTail(buffer, 8) { bytes, start, _ ->
            val v = value.toLong()
            bytes[start] = (v and 0xff).toByte()
            bytes[start + 1] = ((v ushr 8) and 0xff).toByte()
            bytes[start + 2] = ((v ushr 16) and 0xff).toByte()
            bytes[start + 3] = ((v ushr 24) and 0xff).toByte()
            bytes[start + 4] = ((v ushr 32) and 0xff).toByte()
            bytes[start + 5] = ((v ushr 40) and 0xff).toByte()
            bytes[start + 6] = ((v ushr 48) and 0xff).toByte()
            bytes[start + 7] = ((v ushr 56) and 0xff).toByte()
            8
        }
    }

    override fun toByteArray(): ByteArray =
        throw UnsupportedOperationException("KotlinSinkWriter does not support toByteArray()")
}
