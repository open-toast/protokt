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
    fun `bytes to bytes slice and back`() {
        val array = byteArrayOf(1, 2, 3, 4)
        val bytes = Bytes(array)
        val slice = bytes.toBytesSlice()
        val backToBytes = slice.toBytes()

        assertThat(bytes.toString()).isEqualTo(array.contentToString())
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

        assertThat(slice.toString()).isEqualTo(subarray.contentToString())
        assertThat(bytes.toString()).isEqualTo(slice.toString())

        assertThat(bytes.bytes).isEqualTo(subarray)
    }
}
