/*
 * Copyright (c) 2026 Toast Inc.
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

package com.toasttab.protokt.testing.rt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.UnknownField
import com.toasttab.protokt.rt.UnknownFieldSet
import org.junit.jupiter.api.Test
import toasttab.protokt.testing.rt.Empty
import toasttab.protokt.testing.rt.ListTest
import toasttab.protokt.testing.rt.MapTest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import toasttab.protokt.testing.rt.Test as KtTest

class MessageSizeTest {
    private val simple = KtTest { `val` = Bytes("this is a test".toByteArray()) }

    private val nested =
        ListTest {
            list = listOf(simple, KtTest { `val` = Bytes(ByteArray(300)) })
        }

    @Test
    fun `message size matches the serialized size`() {
        val withMap = MapTest { map = mapOf("k" to simple) }

        assertThat(simple.messageSize).isEqualTo(simple.serialize().size)
        assertThat(nested.messageSize).isEqualTo(nested.serialize().size)
        assertThat(withMap.messageSize).isEqualTo(withMap.serialize().size)
    }

    @Test
    fun `an empty message has size zero on every read`() {
        val empty = Empty { }

        assertThat(empty.messageSize).isEqualTo(0)
        assertThat(empty.messageSize).isEqualTo(0)
        assertThat(empty.serialize()).isEmpty()
    }

    @Test
    fun `unknown fields count towards the message size`() {
        val withUnknowns =
            simple.copy {
                unknownFields =
                    UnknownFieldSet.Builder()
                        .apply { add(UnknownField.varint(111, 111)) }
                        .build()
            }

        assertThat(withUnknowns.messageSize).isGreaterThan(simple.messageSize)
        assertThat(withUnknowns.messageSize).isEqualTo(withUnknowns.serialize().size)
    }

    @Test
    fun `repeated reads return the same value`() {
        val first = nested.messageSize
        repeat(10) {
            assertThat(nested.messageSize).isEqualTo(first)
        }
    }

    @Test
    fun `concurrent first reads agree`() {
        val executor = Executors.newFixedThreadPool(8)
        try {
            repeat(50) {
                val message = ListTest { list = listOf(simple, simple, simple) }
                val sizes =
                    executor.invokeAll(List(8) { Callable { message.messageSize } })
                        .map { it.get() }

                assertThat(sizes.toSet()).containsExactly(message.serialize().size)
            }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `memoized size is not visible to field-based JSON serialization`() {
        val json =
            ObjectMapper()
                .registerModule(KotlinModule.Builder().build())
                .writeValueAsString(simple)

        assertThat(json).doesNotContain("memoized")
    }
}
