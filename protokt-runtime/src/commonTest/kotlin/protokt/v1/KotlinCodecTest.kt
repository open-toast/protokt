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

import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(OnlyForUseByGeneratedProtoCode::class)
class KotlinCodecTest {
    private fun roundtripVarint32(value: Int): Int {
        val buf = ByteArray(10)
        val writer = KotlinWriter(buf)
        writer.write(value)
        val reader = KotlinReader(buf)
        return reader.readInt64().toInt()
    }

    private fun roundtripVarint64(value: Long): Long {
        val buf = ByteArray(10)
        val writer = KotlinWriter(buf)
        writer.write(value)
        val reader = KotlinReader(buf)
        return reader.readInt64()
    }

    @Test
    fun varint32_zero() {
        assertEquals(0, roundtripVarint32(0))
    }

    @Test
    fun varint32_one() {
        assertEquals(1, roundtripVarint32(1))
    }

    @Test
    fun varint32_127() {
        assertEquals(127, roundtripVarint32(127))
    }

    @Test
    fun varint32_128() {
        assertEquals(128, roundtripVarint32(128))
    }

    @Test
    fun varint32_maxValue() {
        assertEquals(Int.MAX_VALUE, roundtripVarint32(Int.MAX_VALUE))
    }

    @Test
    fun varint32_negative() {
        assertEquals(-1, roundtripVarint32(-1))
    }

    @Test
    fun varint32_minValue() {
        assertEquals(Int.MIN_VALUE, roundtripVarint32(Int.MIN_VALUE))
    }

    @Test
    fun varint64_zero() {
        assertEquals(0L, roundtripVarint64(0L))
    }

    @Test
    fun varint64_maxValue() {
        assertEquals(Long.MAX_VALUE, roundtripVarint64(Long.MAX_VALUE))
    }

    @Test
    fun varint64_negative() {
        assertEquals(-1L, roundtripVarint64(-1L))
    }

    @Test
    fun varint64_minValue() {
        assertEquals(Long.MIN_VALUE, roundtripVarint64(Long.MIN_VALUE))
    }

    @Test
    fun uint32_roundtrip() {
        val buf = ByteArray(5)
        val writer = KotlinWriter(buf)
        writer.writeUInt32(0xDEADBEEFu)
        val reader = KotlinReader(buf)
        assertEquals(0xDEADBEEFu, reader.readUInt32())
    }

    @Test
    fun uint64_roundtrip() {
        val buf = ByteArray(10)
        val writer = KotlinWriter(buf)
        writer.writeUInt64(0xDEADBEEFCAFEBABEuL)
        val reader = KotlinReader(buf)
        assertEquals(0xDEADBEEFCAFEBABEuL, reader.readUInt64())
    }

    @Test
    fun sint32_roundtrip() {
        for (value in listOf(0, 1, -1, 127, -128, Int.MAX_VALUE, Int.MIN_VALUE)) {
            val buf = ByteArray(10)
            val writer = KotlinWriter(buf)
            writer.writeSInt32(value)
            val reader = KotlinReader(buf)
            assertEquals(value, reader.readSInt32(), "sint32 roundtrip failed for $value")
        }
    }

    @Test
    fun sint64_roundtrip() {
        for (value in listOf(0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE)) {
            val buf = ByteArray(10)
            val writer = KotlinWriter(buf)
            writer.writeSInt64(value)
            val reader = KotlinReader(buf)
            assertEquals(value, reader.readSInt64(), "sint64 roundtrip failed for $value")
        }
    }

    @Test
    fun fixed32_roundtrip() {
        val buf = ByteArray(4)
        val writer = KotlinWriter(buf)
        writer.writeFixed32(0xDEADBEEFu)
        val reader = KotlinReader(buf)
        assertEquals(0xDEADBEEFu, reader.readFixed32())
    }

    @Test
    fun sfixed32_roundtrip() {
        val buf = ByteArray(4)
        val writer = KotlinWriter(buf)
        writer.writeSFixed32(-42)
        val reader = KotlinReader(buf)
        assertEquals(-42, reader.readSFixed32())
    }

    @Test
    fun fixed64_roundtrip() {
        val buf = ByteArray(8)
        val writer = KotlinWriter(buf)
        writer.writeFixed64(0xDEADBEEFCAFEBABEuL)
        val reader = KotlinReader(buf)
        assertEquals(0xDEADBEEFCAFEBABEuL, reader.readFixed64())
    }

    @Test
    fun sfixed64_roundtrip() {
        val buf = ByteArray(8)
        val writer = KotlinWriter(buf)
        writer.writeSFixed64(-123456789L)
        val reader = KotlinReader(buf)
        assertEquals(-123456789L, reader.readSFixed64())
    }

    @Test
    fun float_roundtrip() {
        for (value in listOf(0.0f, 1.0f, -1.0f, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN, Float.POSITIVE_INFINITY)) {
            val buf = ByteArray(4)
            val writer = KotlinWriter(buf)
            writer.write(value)
            val reader = KotlinReader(buf)
            val result = reader.readFloat()
            assertEquals(value.toRawBits(), result.toRawBits(), "float roundtrip failed for $value")
        }
    }

    @Test
    fun double_roundtrip() {
        for (value in listOf(0.0, 1.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.POSITIVE_INFINITY)) {
            val buf = ByteArray(8)
            val writer = KotlinWriter(buf)
            writer.write(value)
            val reader = KotlinReader(buf)
            val result = reader.readDouble()
            assertEquals(value.toRawBits(), result.toRawBits(), "double roundtrip failed for $value")
        }
    }

    @Test
    fun boolean_roundtrip() {
        val buf = ByteArray(1)
        val writer = KotlinWriter(buf)
        writer.write(true)
        val reader = KotlinReader(buf)
        assertEquals(true, reader.readBool())
    }

    @Test
    fun string_ascii_roundtrip() {
        val value = "hello world"
        val buf = ByteArray(1 + value.encodeToByteArray().size)
        val writer = KotlinWriter(buf)
        writer.write(value)
        val reader = KotlinReader(buf)
        assertEquals(value, reader.readString())
    }

    @Test
    fun string_multibyte_utf8_roundtrip() {
        val value = "hello \u00e9\u00e8\u00ea \u4e16\u754c \uD83D\uDE00"
        val encoded = value.encodeToByteArray()
        val buf = ByteArray(Sizes.sizeOf(encoded.size) + encoded.size)
        val writer = KotlinWriter(buf)
        writer.write(value)
        val reader = KotlinReader(buf)
        assertEquals(value, reader.readString())
    }

    @Test
    fun bytes_roundtrip() {
        val value = byteArrayOf(1, 2, 3, 4, 5)
        val buf = ByteArray(1 + value.size)
        val writer = KotlinWriter(buf)
        writer.write(value)
        val reader = KotlinReader(buf)
        val result = reader.readBytes()
        assertEquals(value.toList(), result.value.toList())
    }

    @Test
    fun bytesSlice_roundtrip() {
        val value = BytesSlice(byteArrayOf(0, 1, 2, 3, 4), 1, 3)
        val buf = ByteArray(1 + value.length)
        val writer = KotlinWriter(buf)
        writer.write(value)
        val reader = KotlinReader(buf)
        val result = reader.readBytesSlice()
        assertEquals(3, result.length)
        assertEquals(listOf<Byte>(1, 2, 3), (0 until result.length).map { result.array[result.offset + it] })
    }

    @Test
    fun tag_roundtrip() {
        val tag = (5u shl 3) or 0u // field 5, varint wire type
        val buf = ByteArray(1)
        val writer = KotlinWriter(buf)
        writer.writeTag(tag)
        val reader = KotlinReader(buf)
        assertEquals(tag, reader.readTag())
    }

    @Test
    fun readTag_at_end_returns_zero() {
        val reader = KotlinReader(ByteArray(0))
        assertEquals(0u, reader.readTag())
    }

    @Test
    fun reader_with_offset_and_limit() {
        val inner = byteArrayOf(0, 0, 42, 0, 0)
        val reader = KotlinReader(inner, 2, 3)
        assertEquals(42L, reader.readInt64())
    }
}
