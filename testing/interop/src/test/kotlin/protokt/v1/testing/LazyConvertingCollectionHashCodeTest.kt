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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import protokt.v1.Bytes
import protokt.v1.testing.Test as KtTest

class LazyConvertingCollectionHashCodeTest {
    @Nested
    inner class Map {
        @Test
        fun `hashCode is stable across calls before deserialization`() {
            val mapTest = MapTest {
                map = mapOf(
                    "foo" to KtTest { `val` = Bytes.from("bar".toByteArray()) }
                )
            }

            val hash1 = mapTest.hashCode()
            val hash2 = mapTest.hashCode()
            assertThat(hash1).isEqualTo(hash2)
        }

        @Test
        fun `hashCode is stable after serialization round trip`() {
            val original = MapTest {
                map = mapOf(
                    "foo" to KtTest { `val` = Bytes.from("bar".toByteArray()) }
                )
            }

            val deserialized = MapTest.deserialize(original.serialize())

            val hash1 = deserialized.hashCode()
            // Access the map entries to trigger lazy conversion
            deserialized.map.forEach { (k, v) -> k.length + v.`val`.bytes.size }
            val hash2 = deserialized.hashCode()

            assertThat(hash1).isEqualTo(hash2)
        }

        @Test
        fun `equals is consistent after serialization round trip`() {
            val original = MapTest {
                map = mapOf(
                    "foo" to KtTest { `val` = Bytes.from("bar".toByteArray()) }
                )
            }

            val deserialized = MapTest.deserialize(original.serialize())

            assertThat(deserialized).isEqualTo(original)
            assertThat(original).isEqualTo(deserialized)
        }
    }

    @Nested
    inner class List {
        @Test
        fun `hashCode is stable across calls before deserialization`() {
            val listTest = ListTest {
                list = listOf(
                    KtTest { `val` = Bytes.from("bar".toByteArray()) }
                )
            }

            val hash1 = listTest.hashCode()
            val hash2 = listTest.hashCode()
            assertThat(hash1).isEqualTo(hash2)
        }

        @Test
        fun `hashCode is stable after serialization round trip`() {
            val original = ListTest {
                list = listOf(
                    KtTest { `val` = Bytes.from("bar".toByteArray()) }
                )
            }

            val deserialized = ListTest.deserialize(original.serialize())

            val hash1 = deserialized.hashCode()
            // Access the list entries to trigger lazy conversion
            deserialized.list.forEach { it.`val`.bytes.size }
            val hash2 = deserialized.hashCode()

            assertThat(hash1).isEqualTo(hash2)
        }

        @Test
        fun `equals is consistent after serialization round trip`() {
            val original = ListTest {
                list = listOf(
                    KtTest { `val` = Bytes.from("bar".toByteArray()) }
                )
            }

            val deserialized = ListTest.deserialize(original.serialize())

            assertThat(deserialized).isEqualTo(original)
            assertThat(original).isEqualTo(deserialized)
        }
    }
}
