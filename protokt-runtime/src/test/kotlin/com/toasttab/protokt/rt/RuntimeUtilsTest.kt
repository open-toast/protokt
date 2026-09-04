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

package com.toasttab.protokt.rt

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.LinkedList
import java.util.RandomAccess

class RuntimeUtilsTest {
    @Nested
    inner class Lists {
        private val source = mutableListOf("a", "b", "c")
        private val finished = finishList(source)

        @Test
        fun `finished list has the same contents and equality as a regular list`() {
            assertThat(finished).containsExactly("a", "b", "c").inOrder()
            assertThat(finished).isEqualTo(listOf("a", "b", "c"))
            assertThat(finished.hashCode()).isEqualTo(listOf("a", "b", "c").hashCode())
            assertThat(finished.toString()).isEqualTo(listOf("a", "b", "c").toString())
            assertThat(finished).isInstanceOf(RandomAccess::class.java)
        }

        @Test
        fun `finished list is detached from its source`() {
            source.add("d")
            assertThat(finished).containsExactly("a", "b", "c").inOrder()
        }

        @Test
        fun `finished list rejects mutation through java collection APIs`() {
            @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            val asMutable = finished as java.util.List<String>

            assertThrows<UnsupportedOperationException> { asMutable.add("d") }
            assertThrows<UnsupportedOperationException> { asMutable.set(0, "d") }
            assertThrows<UnsupportedOperationException> { asMutable.remove(0) }
            assertThrows<UnsupportedOperationException> { asMutable.clear() }
        }

        @Test
        fun `empty and null lists finish to the shared empty list`() {
            assertThat(finishList<String>(null)).isSameInstanceAs(emptyList<String>())
            assertThat(finishList(mutableListOf<String>())).isSameInstanceAs(emptyList<String>())
            assertThat(copyList(listOf<String>())).isSameInstanceAs(emptyList<String>())
        }

        @Test
        fun `finishing or copying an already finished list returns the same instance`() {
            assertThat(finishList(finished)).isSameInstanceAs(finished)
            assertThat(copyList(finished)).isSameInstanceAs(finished)
        }

        @Test
        fun `copy list copies non-array-list sources`() {
            val linked = LinkedList(listOf(1, 2))
            val copied = copyList(linked)
            linked.add(3)
            assertThat(copied).containsExactly(1, 2).inOrder()
        }

        @Test
        fun `finished list survives java serialization`() {
            val bytes = ByteArrayOutputStream()
            ObjectOutputStream(bytes).use { it.writeObject(finished) }

            val restored =
                ObjectInputStream(ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() }

            assertThat(restored).isEqualTo(finished)
        }
    }

    @Nested
    inner class Maps {
        private val source = mutableMapOf("c" to 3, "a" to 1, "b" to 2)
        private val finished = finishMap(source)

        @Test
        fun `finished map keeps insertion order and map equality`() {
            assertThat(finished.keys).containsExactly("c", "a", "b").inOrder()
            assertThat(finished).isEqualTo(mapOf("c" to 3, "a" to 1, "b" to 2))
            assertThat(finished.hashCode()).isEqualTo(source.hashCode())
            assertThat(finished.toString()).isEqualTo(source.toString())
        }

        @Test
        fun `small and large maps finish with the same contents`() {
            (1..20).forEach { size ->
                val map = (1..size).associateWith { it * 10 }
                assertThat(finishMap(LinkedHashMap(map))).isEqualTo(map)
                assertThat(finishMap(LinkedHashMap(map)).keys).containsExactlyElementsIn(map.keys).inOrder()
            }
        }

        @Test
        fun `small finished maps are detached from their source`() {
            source["d"] = 4
            assertThat(finished).hasSize(3)
        }

        @Test
        fun `finished map rejects mutation through java collection APIs`() {
            @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            val asJava = finished as java.util.Map<String, Int>

            assertThrows<UnsupportedOperationException> { asJava.put("d", 4) }
            assertThrows<UnsupportedOperationException> { asJava.remove("a") }
            assertThrows<UnsupportedOperationException> { asJava.clear() }
            assertThrows<UnsupportedOperationException> {
                asJava.entrySet().iterator().apply { next() }.remove()
            }
        }

        @Test
        fun `empty and null maps finish to the shared empty map`() {
            assertThat(finishMap<String, Int>(null)).isSameInstanceAs(emptyMap<String, Int>())
            assertThat(finishMap(mutableMapOf<String, Int>())).isSameInstanceAs(emptyMap<String, Int>())
            assertThat(copyMap(mapOf<String, Int>())).isSameInstanceAs(emptyMap<String, Int>())
        }

        @Test
        fun `finishing an already finished or copied map returns the same instance`() {
            assertThat(finishMap(finished)).isSameInstanceAs(finished)

            val copied = copyMap(source)
            assertThat(finishMap(copied)).isSameInstanceAs(copied)
        }

        @Test
        fun `copy map is detached from its source`() {
            val copied = copyMap(source)
            source["d"] = 4
            assertThat(copied).hasSize(3)
        }
    }
}
