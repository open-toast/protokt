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
import com.google.protobuf.ByteString
import com.toasttab.protokt.v1.testing.TestOuterClass
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import protokt.v1.Bytes
import com.toasttab.protokt.v1.testing.TestOuterClass.ListTest as JavaListTest
import com.toasttab.protokt.v1.testing.TestOuterClass.MapTest as JavaMapTest
import com.toasttab.protokt.v1.testing.TestOuterClass.Test as JavaTest
import protokt.v1.testing.Test as KtTest

class PersistentCollectionsTest {
    private val content = "this is a test"
    private val content0 = "this is another test"

    private val bytesContent = Bytes.from(content.toByteArray())
    private val bytesContent0 = Bytes.from(content0.toByteArray())

    private val protoktSimple = KtTest { `val` = bytesContent }
    private val protoktSimple0 = KtTest { `val` = bytesContent0 }

    private val javaSimple =
        TestOuterClass.Test.newBuilder()
            .setVal(ByteString.copyFrom(bytesContent.bytes))
            .build()

    private val javaSimple0 =
        JavaTest.newBuilder()
            .setVal(ByteString.copyFrom(bytesContent0.bytes))
            .build()

    @Nested
    inner class Lists {
        private val protoktList = listOf(protoktSimple, protoktSimple0)
        private val javaList = listOf(javaSimple, javaSimple0)
        private val stringList = listOf("string1", "string2")

        @Test
        fun `deserialized list is persistent`() {
            val deserialized = ListTest.deserialize(
                ListTest { list = protoktList }.serialize()
            )
            assertThat(deserialized.list).isInstanceOf(PersistentList::class.java)
        }

        @Test
        fun `round trip with persistent collections`() {
            val original = ListTest { list = protoktList }
            val deserialized = ListTest.deserialize(original.serialize())
            assertThat(deserialized.list).containsExactly(protoktSimple, protoktSimple0).inOrder()
        }

        @Test
        fun `java from kotlin with persistent collections`() {
            assertThat(
                JavaListTest.parseFrom(
                    ListTest { list = protoktList }.serialize()
                ).listList
            ).containsExactly(javaSimple, javaSimple0).inOrder()
        }

        @Test
        fun `kotlin from java produces persistent list`() {
            val deserialized = ListTest.deserialize(
                JavaListTest.newBuilder()
                    .addAllList(javaList)
                    .build()
                    .toByteArray()
            )
            assertThat(deserialized.list).containsExactly(protoktSimple, protoktSimple0).inOrder()
            assertThat(deserialized.list).isInstanceOf(PersistentList::class.java)
        }

        @Test
        fun `string list round trip`() {
            val deserialized = RepeatedTest.deserialize(
                RepeatedTest { list = stringList }.serialize()
            )
            assertThat(deserialized.list).containsExactlyElementsIn(stringList).inOrder()
            assertThat(deserialized.list).isInstanceOf(PersistentList::class.java)
        }

        @Test
        fun `packed list round trip`() {
            val int64List = listOf(123L, 456L)
            val deserialized = RepeatedPackedTest.deserialize(
                RepeatedPackedTest { list = int64List }.serialize()
            )
            assertThat(deserialized.list).containsExactlyElementsIn(int64List).inOrder()
            assertThat(deserialized.list).isInstanceOf(PersistentList::class.java)
        }

        @Test
        fun `serialized bytes match java protobuf`() {
            val protoktBytes = ListTest { list = protoktList }.serialize()
            val javaBytes = JavaListTest.newBuilder()
                .addAllList(listOf(javaSimple, javaSimple0))
                .build()
                .toByteArray()

            assertThat(protoktBytes).isEqualTo(javaBytes)
        }

        @Test
        fun `copy append produces persistent list`() {
            val original = ListTest.deserialize(
                ListTest { list = listOf(protoktSimple) }.serialize()
            )
            val copied = original.copy { list = list + protoktSimple0 }
            assertThat(copied.list).containsExactly(protoktSimple, protoktSimple0).inOrder()
        }
    }

    @Nested
    inner class Maps {
        @Test
        fun `deserialized map is persistent`() {
            val deserialized = MapTest.deserialize(
                MapTest {
                    map = mapOf(content to protoktSimple, content0 to protoktSimple0)
                }.serialize()
            )
            assertThat(deserialized.map).isInstanceOf(PersistentMap::class.java)
        }

        @Test
        fun `round trip with persistent collections`() {
            val original = MapTest {
                map = mapOf(content to protoktSimple, content0 to protoktSimple0)
            }
            val deserialized = MapTest.deserialize(original.serialize())
            assertThat(deserialized.map).containsExactly(
                content,
                protoktSimple,
                content0,
                protoktSimple0
            )
        }

        @Test
        fun `java from kotlin with persistent collections`() {
            assertThat(
                JavaMapTest.parseFrom(
                    MapTest {
                        map = mapOf(content to protoktSimple, content0 to protoktSimple0)
                    }.serialize()
                ).mapMap
            ).containsExactly(
                content,
                javaSimple,
                content0,
                javaSimple0
            )
        }

        @Test
        fun `kotlin from java produces persistent map`() {
            val deserialized = MapTest.deserialize(
                JavaMapTest.newBuilder().apply {
                    putMap(content, javaSimple)
                    putMap(content0, javaSimple0)
                }.build().toByteArray()
            )
            assertThat(deserialized.map).containsExactly(
                content,
                protoktSimple,
                content0,
                protoktSimple0
            )
            assertThat(deserialized.map).isInstanceOf(PersistentMap::class.java)
        }

        @Test
        fun `serialized bytes match java protobuf`() {
            val protoktBytes = MapTest {
                map = mapOf(content to protoktSimple, content0 to protoktSimple0)
            }.serialize()

            val javaBytes = JavaMapTest.newBuilder().apply {
                putMap(content, javaSimple)
                putMap(content0, javaSimple0)
            }.build().toByteArray()

            assertThat(protoktBytes).isEqualTo(javaBytes)
        }

        @Test
        fun `copy append produces persistent map`() {
            val original = MapTest.deserialize(
                MapTest {
                    map = mapOf(content to protoktSimple)
                }.serialize()
            )
            val copied = original.copy { map = map + (content0 to protoktSimple0) }
            assertThat(copied.map).containsExactly(
                content,
                protoktSimple,
                content0,
                protoktSimple0
            )
        }
    }

    @Nested
    inner class FreezePassThrough {
        @Test
        fun `persistent list passes through freezeList unchanged`() {
            val deserialized = ListTest.deserialize(
                ListTest { list = listOf(protoktSimple) }.serialize()
            )
            val list = deserialized.list
            assertThat(list).isInstanceOf(PersistentList::class.java)

            val copied = deserialized.copy { this.list = list }
            assertThat(copied.list).isSameInstanceAs(list)
        }

        @Test
        fun `persistent map passes through freezeMap unchanged`() {
            val deserialized = MapTest.deserialize(
                MapTest { map = mapOf(content to protoktSimple) }.serialize()
            )
            val map = deserialized.map
            assertThat(map).isInstanceOf(PersistentMap::class.java)

            val copied = deserialized.copy { this.map = map }
            assertThat(copied.map).isSameInstanceAs(map)
        }
    }
}
