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
import kotlin.test.assertFails
import kotlin.test.assertTrue

class CollectionsTest {
    @Test
    fun freezingEmptyListDoesNotRetainMutableInput() {
        val input = mutableListOf<Int>()
        val frozen = DefaultCollectionFactory.freezeList(input)

        input.add(1)

        assertTrue(frozen.isEmpty())
    }

    @Test
    fun frozenListSubListCannotMutateTheList() {
        val frozen = DefaultCollectionFactory.freezeList(listOf(1, 2, 3))
        val view = frozen.subList(0, 2)

        assertFails { (view as MutableList<Int>).clear() }
        assertEquals(listOf(1, 2, 3), frozen)
        assertEquals(listOf(1, 2), view)
    }

    @Test
    fun frozenMapKeysHaveSetEqualityAndHashing() {
        val original = mapOf(1 to "one", 2 to "two")
        val keys = DefaultCollectionFactory.freezeMap(original).keys

        assertEquals(original.keys, keys)
        assertEquals(keys, original.keys)
        assertEquals(original.keys.hashCode(), keys.hashCode())
        assertEquals(original.keys.toString(), keys.toString())
    }

    @Test
    fun frozenMapEntriesHaveSetEqualityAndHashing() {
        val original = mapOf(1 to "one", 2 to "two")
        val entries = DefaultCollectionFactory.freezeMap(original).entries

        assertEquals(original.entries, entries)
        assertEquals(entries, original.entries)
        assertEquals(original.entries.hashCode(), entries.hashCode())
        assertEquals(original.entries.toString(), entries.toString())
    }

    @Test
    fun frozenMapEntryHasValueEqualityAndHashing() {
        val original = mapOf(1 to "one")
        val entry = DefaultCollectionFactory.freezeMap(original).entries.single()
        val expected = original.entries.single()

        assertEquals(expected, entry)
        assertEquals(entry, expected)
        assertEquals(expected.hashCode(), entry.hashCode())
        assertEquals(expected.toString(), entry.toString())
    }
}
