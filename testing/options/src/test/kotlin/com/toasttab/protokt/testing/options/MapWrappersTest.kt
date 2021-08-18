/*
 * Copyright (c) 2021 Toast Inc.
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

package com.toasttab.protokt.testing.options

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.Any
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.UUID
import org.junit.jupiter.api.Test

class MapWrappersTest {
    @Test
    fun `test map with string key wrap`() {
        val msg =
            MapWrappers {
                mapStringKeyWrapped = mapOf(StringBox("foo") to 5)
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapStringKeyWrapped)
            .containsEntry(StringBox("foo"), 5)
    }

    @Test
    fun `test map with int key wrap`() {
        val msg =
            MapWrappers {
                mapIntKeyWrapped = mapOf(IntBox(1) to 5)
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapIntKeyWrapped)
            .containsEntry(IntBox(1), 5)
    }

    @Test
    fun `test map with string value wrap`() {
        val msg =
            MapWrappers {
                mapStringValueWrapped = mapOf("foo" to StringBox("bar"))
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapStringValueWrapped)
            .containsEntry("foo", StringBox("bar"))
    }

    @Test
    fun `test map with int value wrap`() {
        val msg =
            MapWrappers {
                mapIntValueWrapped = mapOf(1 to IntBox(5))
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapIntValueWrapped)
            .containsEntry(1, IntBox(5))
    }

    @Test
    fun `test map with string key wrap and string value wrap`() {
        val msg =
            MapWrappers {
                mapStringDoubleWrapped = mapOf(StringBox("foo") to StringBox("bar"))
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapStringDoubleWrapped)
            .containsEntry(StringBox("foo"), StringBox("bar"))
    }

    @Test
    fun `test map with int key wrap and int value wrap`() {
        val msg =
            MapWrappers {
                mapIntDoubleWrapped = mapOf(IntBox(1) to IntBox(5))
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapIntDoubleWrapped)
            .containsEntry(IntBox(1), IntBox(5))
    }

    @Test
    fun `test map with string key wrap and bytes value wrap`() {
        val uuid = UUID.randomUUID()

        val msg =
            MapWrappers {
                mapStringUuid = mapOf(StringBox("foo") to uuid)
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapStringUuid)
            .containsEntry(StringBox("foo"), uuid)
    }

    @Test
    fun `test map with string key wrap and message value wrap`() {
        val addr = InetSocketAddress(InetAddress.getLocalHost(), 4)

        val msg =
            MapWrappers {
                mapStringSocketAddress = mapOf(StringBox("foo") to addr)
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapStringSocketAddress)
            .containsEntry(StringBox("foo"), addr)
    }

    @Test
    fun `test map with string key wrap and unqualified parameterized value wrap`() {
        val msg =
            MapWrappers {
                mapStringListWrapper = mapOf("foo" to listOf("bar", "baz"))
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapStringListWrapper)
            .containsEntry("foo", listOf("bar", "baz"))
    }

    @Test
    fun `test map with string key wrap and qualified parameterized value wrap`() {
        val msg =
            MapWrappers {
                mapAnyListWrapper = mapOf("foo" to listOf(Any { typeUrl = "bar" }))
            }

        assertThat(MapWrappers.deserialize(msg.serialize()).mapAnyListWrapper)
            .containsEntry("foo", listOf(Any { typeUrl = "bar" }))
    }
}
