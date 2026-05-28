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

import kotlin.test.Test
import kotlin.test.assertEquals

class MultiplatformRoundTripTest {
    @Test
    fun emptyMessageRoundTrip() {
        val msg = MpTest {}
        assertEquals(msg, MpTest.deserialize(msg.serialize()))
    }

    @Test
    fun stringFieldRoundTrip() {
        val msg = MpTest { foo = "hello from native" }
        val deserialized = MpTest.deserialize(msg.serialize())
        assertEquals(msg, deserialized)
        assertEquals("hello from native", deserialized.foo)
    }

    @Test
    fun serializedSizeMatchesBytes() {
        val msg = MpTest { foo = "test" }
        assertEquals(msg.serializedSize(), msg.serialize().size)
    }

    @Test
    fun defaultValuesRoundTrip() {
        val msg = MpTest { foo = "" }
        val deserialized = MpTest.deserialize(msg.serialize())
        assertEquals("", deserialized.foo)
    }

    @Test
    fun unicodeRoundTrip() {
        val msg = MpTest { foo = "\u00e9\u00e8\u4e16\u754c\uD83D\uDE00" }
        val deserialized = MpTest.deserialize(msg.serialize())
        assertEquals(msg, deserialized)
    }
}
