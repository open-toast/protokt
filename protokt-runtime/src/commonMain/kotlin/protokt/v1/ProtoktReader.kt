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
internal class ProtoktReader(
    private val buf: ByteArray,
    private var pos: Int = 0,
    private var limit: Int = buf.size
) : Reader {
    private var _lastTag: Int = 0
    private var messageDepth: Int = 0

    override val lastTag: UInt
        get() = _lastTag.toUInt()

    override fun readTag(): UInt {
        if (pos >= limit) {
            _lastTag = 0
            return 0u
        }
        _lastTag = readRawVarint32()
        if (_lastTag == 0 || WireFormat.getTagFieldNumber(_lastTag) == 0) {
            throw IllegalStateException("Invalid tag: $_lastTag")
        }
        return _lastTag.toUInt()
    }

    override fun readDouble(): Double =
        Double.fromBits(readFixed64Bits().toLong())

    override fun readFloat(): Float =
        Float.fromBits(readFixed32Bits().toInt())

    override fun readFixed32(): UInt =
        readFixed32Bits()

    override fun readFixed64(): ULong =
        readFixed64Bits()

    override fun readInt64(): Long =
        readRawVarint64().toLong()

    override fun readSFixed32(): Int =
        readFixed32Bits().toInt()

    override fun readSFixed64(): Long =
        readFixed64Bits().toLong()

    override fun readSInt32(): Int {
        val n = readRawVarint32()
        return (n ushr 1) xor -(n and 1)
    }

    override fun readSInt64(): Long {
        val n = readRawVarint64().toLong()
        return (n ushr 1) xor -(n and 1)
    }

    override fun readString(): String {
        val length = readRawVarint32()
        checkLength(length)
        val s = buf.decodeToString(pos, pos + length)
        pos += length
        return s
    }

    override fun readUInt64(): ULong =
        readRawVarint64()

    override fun readBytes(): Bytes {
        val length = readRawVarint32()
        checkLength(length)
        val bytes = buf.copyOfRange(pos, pos + length)
        pos += length
        return Bytes(bytes)
    }

    override fun readBytesSlice(): BytesSlice {
        val length = readRawVarint32()
        checkLength(length)
        val slice = BytesSlice(buf, pos, length)
        pos += length
        return slice
    }

    override fun readRepeated(packed: Boolean, acc: Reader.() -> Unit) {
        if (!packed || WireFormat.getTagWireType(_lastTag) != WireFormat.WIRETYPE_LENGTH_DELIMITED) {
            acc(this)
        } else {
            val length = readRawVarint32()
            checkLength(length)
            val oldLimit = limit
            limit = pos + length
            while (pos < limit) {
                acc(this)
            }
            limit = oldLimit
        }
    }

    override fun <T : Message> readMessage(m: Deserializer<T>): T {
        check(++messageDepth <= WireFormat.DEFAULT_RECURSION_LIMIT) { WireFormat.TOO_MANY_LEVELS_OF_NESTING }
        try {
            val length = readRawVarint32()
            checkLength(length)
            val oldLimit = limit
            limit = pos + length
            val res = m.deserialize(this)
            require(pos == limit) { "Message not fully consumed: pos=$pos, limit=$limit" }
            limit = oldLimit
            return res
        } finally {
            messageDepth--
        }
    }

    private fun checkLength(length: Int) {
        check(length >= 0) { WireFormat.NEGATIVE_SIZE }
        check(length <= limit - pos) { WireFormat.TRUNCATED_MESSAGE }
    }

    private fun checkAvailable(size: Int) {
        check(limit - pos >= size) { WireFormat.TRUNCATED_MESSAGE }
    }

    private fun readRawVarint32(): Int {
        var result = 0
        var shift = 0
        while (shift < 32) {
            checkAvailable(1)
            val b = buf[pos++].toInt()
            result = result or ((b and 0x7f) shl shift)
            if (b and 0x80 == 0) {
                return result
            }
            shift += 7
        }
        // discard upper bits for oversized varints
        while (true) {
            checkAvailable(1)
            val b = buf[pos++].toInt()
            if (b and 0x80 == 0) {
                return result
            }
        }
    }

    private fun readRawVarint64(): ULong {
        var result = 0UL
        var shift = 0
        while (shift < 64) {
            checkAvailable(1)
            val b = buf[pos++].toInt()
            result = result or ((b.toLong() and 0x7f).toULong() shl shift)
            if (b and 0x80 == 0) {
                return result
            }
            shift += 7
        }
        throw IllegalStateException("Varint too long")
    }

    private fun readFixed32Bits(): UInt {
        checkAvailable(4)
        val b0 = buf[pos++].toInt() and 0xff
        val b1 = buf[pos++].toInt() and 0xff
        val b2 = buf[pos++].toInt() and 0xff
        val b3 = buf[pos++].toInt() and 0xff
        return (b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)).toUInt()
    }

    private fun readFixed64Bits(): ULong {
        checkAvailable(8)
        val b0 = buf[pos++].toLong() and 0xff
        val b1 = buf[pos++].toLong() and 0xff
        val b2 = buf[pos++].toLong() and 0xff
        val b3 = buf[pos++].toLong() and 0xff
        val b4 = buf[pos++].toLong() and 0xff
        val b5 = buf[pos++].toLong() and 0xff
        val b6 = buf[pos++].toLong() and 0xff
        val b7 = buf[pos++].toLong() and 0xff
        return (
            b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24) or
                (b4 shl 32) or (b5 shl 40) or (b6 shl 48) or (b7 shl 56)
            ).toULong()
    }
}
