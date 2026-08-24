/*
 * Copyright (c) 2021 Toast, Inc.
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

class BytesTest {
    @Test
    fun `empty bytes render with size and an empty preview`() {
        assertThat(Bytes.empty().toString()).isEqualTo("Bytes(size=0, hex=\"\")")
    }

    @Test
    fun `bytes to bytes slice and back`() {
        val array = byteArrayOf(1, 2, 3, 4)
        val bytes = Bytes(array)
        val slice = bytes.toBytesSlice()
        val backToBytes = slice.toBytes()

        assertThat(bytes.toString()).isEqualTo("Bytes(size=4, hex=\"01020304\")")
        assertThat(slice.toString()).isEqualTo(bytes.toString())
        assertThat(backToBytes.toString()).isEqualTo(slice.toString())
        assertThat(backToBytes.toString()).isEqualTo(bytes.toString())

        assertThat(backToBytes.bytes).isEqualTo(array)
    }

    @Test
    fun `bytes slice with offset to bytes and back`() {
        val array = byteArrayOf(1, 2, 3, 4)
        val subarray = byteArrayOf(2, 3)
        val slice = BytesSlice(array, 1, 2)
        val bytes = slice.toBytes()

        assertThat(slice.toString()).isEqualTo("Bytes(size=2, hex=\"0203\")")
        assertThat(bytes.toString()).isEqualTo(slice.toString())

        assertThat(bytes.bytes).isEqualTo(subarray)
    }

    @Test
    fun `bytes render unsigned lowercase hex`() {
        val bytes = Bytes.from(byteArrayOf(0, 0x0f, 0x10, 0x7f, 0x80.toByte(), 0xff.toByte()))

        assertThat(bytes.toString()).isEqualTo("Bytes(size=6, hex=\"000f107f80ff\")")
    }

    @Test
    fun `preview includes all of a 32-byte value`() {
        val bytes = Bytes.from(ByteArray(32) { it.toByte() })

        assertThat(bytes.toString())
            .isEqualTo("Bytes(size=32, hex=\"000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f\")")
    }

    @Test
    fun `preview truncates values longer than 32 bytes`() {
        val bytes = Bytes.from(ByteArray(33) { it.toByte() })

        assertThat(bytes.toString())
            .isEqualTo("Bytes(size=33, hex=\"000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f...\")")
    }

    @Test
    fun `slice preview does not include bytes outside its bounds`() {
        val slice = BytesSlice(byteArrayOf(0x7f, 1, 2, 3, 0x7f), 1, 3)

        assertThat(slice.toString()).isEqualTo("Bytes(size=3, hex=\"010203\")")
    }
}
