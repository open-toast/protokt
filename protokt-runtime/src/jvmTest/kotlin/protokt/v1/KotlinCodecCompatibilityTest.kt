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

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests that KotlinCodec produces byte-level correct protobuf wire format
 * by comparing against known encodings.
 */
@OptIn(OnlyForUseByGeneratedProtoCode::class)
class KotlinCodecCompatibilityTest {
    @Test
    fun `varint encoding of 1`() {
        val buf = ByteArray(1)
        KotlinWriter(buf).write(1)
        assertThat(buf).isEqualTo(byteArrayOf(0x01))
    }

    @Test
    fun `varint encoding of 150`() {
        // 150 = 0x96 -> varint: 0x96 0x01
        val buf = ByteArray(2)
        KotlinWriter(buf).write(150)
        assertThat(buf).isEqualTo(byteArrayOf(0x96.toByte(), 0x01))
    }

    @Test
    fun `varint encoding of 300`() {
        // 300 = 0x012c -> varint: 0xac 0x02
        val buf = ByteArray(2)
        KotlinWriter(buf).write(300)
        assertThat(buf).isEqualTo(byteArrayOf(0xAC.toByte(), 0x02))
    }

    @Test
    fun `varint encoding of negative int uses 10 bytes`() {
        // Negative int32 is sign-extended to 64 bits = 10 byte varint
        val buf = ByteArray(10)
        KotlinWriter(buf).write(-1)
        // -1 as int32 varint = ff ff ff ff ff ff ff ff ff 01
        val expected = byteArrayOf(
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x01
        )
        assertThat(buf).isEqualTo(expected)
    }

    @Test
    fun `fixed32 little-endian encoding`() {
        val buf = ByteArray(4)
        KotlinWriter(buf).writeFixed32(0x01020304u)
        // Little-endian: least significant byte first
        assertThat(buf).isEqualTo(byteArrayOf(0x04, 0x03, 0x02, 0x01))
    }

    @Test
    fun `fixed64 little-endian encoding`() {
        val buf = ByteArray(8)
        KotlinWriter(buf).writeFixed64(0x0102030405060708uL)
        assertThat(buf).isEqualTo(byteArrayOf(0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01))
    }

    @Test
    fun `sint32 zigzag encoding`() {
        // zigzag(0)=0, zigzag(-1)=1, zigzag(1)=2, zigzag(-2)=3, zigzag(2)=4
        val cases = mapOf(
            0 to byteArrayOf(0x00),
            -1 to byteArrayOf(0x01),
            1 to byteArrayOf(0x02),
            -2 to byteArrayOf(0x03),
            2147483647 to byteArrayOf(0xFE.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x0F),
            -2147483648 to byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x0F)
        )
        for ((value, expected) in cases) {
            val buf = ByteArray(5)
            KotlinWriter(buf).writeSInt32(value)
            assertThat(buf.take(expected.size).toByteArray())
                .isEqualTo(expected)
        }
    }

    @Test
    fun `string encoding is length-prefixed UTF-8`() {
        val buf = ByteArray(8)
        KotlinWriter(buf).write("testing")
        // length 7, then "testing" in ASCII
        assertThat(buf).isEqualTo(byteArrayOf(7, 't'.code.toByte(), 'e'.code.toByte(), 's'.code.toByte(), 't'.code.toByte(), 'i'.code.toByte(), 'n'.code.toByte(), 'g'.code.toByte()))
    }

    @Test
    fun `boolean true encodes as varint 1`() {
        val buf = ByteArray(1)
        KotlinWriter(buf).write(true)
        assertThat(buf).isEqualTo(byteArrayOf(0x01))
    }

    @Test
    fun `boolean false encodes as varint 0`() {
        val buf = ByteArray(1)
        KotlinWriter(buf).write(false)
        assertThat(buf).isEqualTo(byteArrayOf(0x00))
    }

    @Test
    fun `float encoding matches IEEE 754 little-endian`() {
        val buf = ByteArray(4)
        KotlinWriter(buf).write(1.0f)
        // 1.0f = 0x3F800000 -> little-endian: 00 00 80 3f
        assertThat(buf).isEqualTo(byteArrayOf(0x00, 0x00, 0x80.toByte(), 0x3F))
    }

    @Test
    fun `double encoding matches IEEE 754 little-endian`() {
        val buf = ByteArray(8)
        KotlinWriter(buf).write(1.0)
        // 1.0 = 0x3FF0000000000000 -> little-endian: 00 00 00 00 00 00 f0 3f
        assertThat(buf).isEqualTo(byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0.toByte(), 0x3F))
    }
}
