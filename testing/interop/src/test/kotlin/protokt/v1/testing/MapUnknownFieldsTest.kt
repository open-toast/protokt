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

package protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.Bytes

class MapUnknownFieldsTest {
    @Test
    fun unknownFieldsInsideMapEntryAreConsumed() {
        val entry =
            byteArrayOf(
                0x18, 0x07,
                0x0a, 0x03, 'k'.code.toByte(), 'e'.code.toByte(), 'y'.code.toByte(),
                0x21, 1, 2, 3, 4, 5, 6, 7, 8,
                0x12, 0x03, 0x0a, 0x01, 0x7f,
                0x2a, 0x02, 0xaa.toByte(), 0xbb.toByte(),
                0x35, 9, 10, 11, 12
            )
        val message = MapTest.deserialize(byteArrayOf(0x0a, entry.size.toByte()) + entry)

        assertThat(message.map).containsExactly("key", Test { `val` = Bytes.from(byteArrayOf(0x7f)) })
    }
}
