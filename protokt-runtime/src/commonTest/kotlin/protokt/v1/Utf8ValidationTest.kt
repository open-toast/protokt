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
import kotlin.test.assertFailsWith

class Utf8ValidationTest {
    @Test
    fun `malformed byte sequences are rejected`() {
        listOf(
            byteArrayOf(0xC0.toByte(), 0xAF.toByte()),
            byteArrayOf(0x80.toByte()),
            byteArrayOf(0xED.toByte(), 0xA0.toByte(), 0x80.toByte()),
            byteArrayOf(0xE2.toByte(), 0x82.toByte()),
            byteArrayOf(0xF4.toByte(), 0x90.toByte(), 0x80.toByte(), 0x80.toByte()),
            byteArrayOf(0xE2.toByte(), 0x28, 0xA1.toByte()),
        ).forEach { bytes ->
            assertFailsWith<ProtoktDecodeException> { decodeUtf8(bytes) }
        }
    }
}
