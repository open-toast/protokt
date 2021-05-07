/*
 * Copyright (c) 2020 Toast Inc.
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

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp as JavaTimestamp
import com.toasttab.protokt.Timestamp
import com.toasttab.protokt.pack
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.testing.rt.Test as KtTest
import com.toasttab.protokt.testing.rt.TestOuterClass.ListTest as JavaListTest
import com.toasttab.protokt.testing.rt.TestOuterClass.MapTest as JavaMapTest
import com.toasttab.protokt.testing.rt.TestOuterClass.RepeatedAnyTest as JavaRepeatedAnyTest
import com.toasttab.protokt.testing.rt.TestOuterClass.RepeatedPackedTest as JavaRepeatedPackedTest
import com.toasttab.protokt.testing.rt.TestOuterClass.RepeatedTest as JavaRepeatedTest
import com.toasttab.protokt.testing.rt.TestOuterClass.RepeatedUnpackedTest as JavaRepeatedUnpackedTest
import com.toasttab.protokt.testing.rt.TestOuterClass.RepeatedWktTest as JavaRepeatedWktTest
import com.toasttab.protokt.testing.rt.TestOuterClass.Test as JavaTest
import com.toasttab.protokt.unpack
import kotlin.random.Random
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CollectionsJavaInteropTest {
    private val content = "this is a test"
    private val content0 = "this is another test"

    private val bytesContent = Bytes(content.toByteArray())
    private val bytesContent0 = Bytes(content0.toByteArray())

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

    private val protoktTimestamp =
        Timestamp {
            seconds = System.currentTimeMillis() * 1000
            nanos = Random.nextInt(99999)
        }

    private val javaTimestamp =
        JavaTimestamp.newBuilder()
            .setSeconds(protoktTimestamp.seconds)
            .setNanos(protoktTimestamp.nanos)
            .build()

    private val protoktTimestamp0 =
        Timestamp {
            seconds = (System.currentTimeMillis() * 1000) - 60
            nanos = Random.nextInt(99999)
        }

    private val javaTimestamp0 =
        JavaTimestamp.newBuilder()
            .setSeconds(protoktTimestamp0.seconds)
            .setNanos(protoktTimestamp0.nanos)
            .build()

    @Nested
    inner class Basic {
        @Test
        fun `kotlin message round trip smoke test`() {
            assertThat(KtTest.deserialize(protoktSimple.serialize()))
                .isEqualTo(protoktSimple)
        }

        @Test
        fun `java from kotlin`() {
            assertThat(JavaTest.parseFrom(protoktSimple.serialize()))
                .isEqualTo(javaSimple)
        }

        @Test
        fun `kotlin from java`() {
            assertThat(KtTest.deserialize(javaSimple.toByteArray()))
                .isEqualTo(protoktSimple)
        }
    }

    @Nested
    inner class Lists {
        @Nested
        inner class Basic {
            private val protoktList = listOf(protoktSimple, protoktSimple0)
            private val javaList = listOf(javaSimple, javaSimple0)
            private val stringList = listOf("string1", "string2")

            @Test
            fun `kotlin smoke test`() {
                assertThat(
                    ListTest.deserialize(
                        ListTest { list = protoktList }.serialize()
                    ).list
                ).containsExactly(protoktSimple, protoktSimple0).inOrder()
            }

            @Test
            fun `java from kotlin`() {
                assertThat(
                    JavaListTest.parseFrom(
                        ListTest { list = protoktList }.serialize()
                    ).listList
                ).containsExactly(javaSimple, javaSimple0).inOrder()
            }

            @Test
            fun `kotlin from java`() {
                assertThat(
                    ListTest.deserialize(
                        JavaListTest.newBuilder()
                            .addAllList(javaList)
                            .build()
                            .toByteArray()
                    ).list
                ).containsExactly(protoktSimple, protoktSimple0).inOrder()
            }

            @Test
            fun `kotlin strings smoke test`() {
                assertThat(
                    RepeatedTest.deserialize(
                        RepeatedTest { list = stringList }.serialize()
                    ).list
                ).containsExactlyElementsIn(stringList).inOrder()
            }

            @Test
            fun `java from kotlin strings`() {
                assertThat(
                    JavaRepeatedTest.parseFrom(
                        RepeatedTest { list = stringList }.serialize()
                    ).listList
                ).containsExactlyElementsIn(stringList).inOrder()
            }

            @Test
            fun `kotlin from java strings`() {
                assertThat(
                    RepeatedTest.deserialize(
                        JavaRepeatedTest.newBuilder()
                            .addAllList(stringList)
                            .build()
                            .toByteArray()
                    ).list
                ).containsExactlyElementsIn(stringList).inOrder()
            }
        }

        @Nested
        inner class WellKnownTypes {
            private val protoktWktList = listOf(protoktTimestamp, protoktTimestamp0)
            private val javaWktList = listOf(javaTimestamp, javaTimestamp0)

            @Test
            fun `kotlin well-known type smoke test`() {
                assertThat(
                    RepeatedWktTest.deserialize(
                        RepeatedWktTest { list = protoktWktList }.serialize()
                    ).list
                ).containsExactlyElementsIn(protoktWktList).inOrder()
            }

            @Test
            fun `java from kotlin well-known type`() {
                assertThat(
                    JavaRepeatedWktTest.parseFrom(
                        RepeatedWktTest { list = protoktWktList }.serialize()
                    ).listList
                ).containsExactlyElementsIn(javaWktList).inOrder()
            }

            @Test
            fun `kotlin from java well-known type`() {
                assertThat(
                    RepeatedWktTest.deserialize(
                        JavaRepeatedWktTest.newBuilder()
                            .addAllList(javaWktList)
                            .build()
                            .toByteArray()
                    ).list
                ).containsExactlyElementsIn(protoktWktList).inOrder()
            }
        }

        @Nested
        inner class AnyTests {
            @Test
            fun `kotlin any smoke test`() {
                assertThat(
                    RepeatedAnyTest.deserialize(
                        RepeatedAnyTest {
                            list = listOf(
                                com.toasttab.protokt.Any.pack(protoktSimple),
                                com.toasttab.protokt.Any.pack(protoktSimple0)
                            )
                        }.serialize()
                    ).list.map { it.unpack(KtTest) }
                ).containsExactly(protoktSimple, protoktSimple0).inOrder()
            }

            @Test
            fun `java from kotlin`() {
                assertThat(
                    JavaRepeatedAnyTest.parseFrom(
                        RepeatedAnyTest {
                            list = listOf(
                                com.toasttab.protokt.Any.pack(protoktSimple),
                                com.toasttab.protokt.Any.pack(protoktSimple0)
                            )
                        }.serialize()
                    ).listList.map { it.unpack(javaSimple.javaClass) }
                ).containsExactly(javaSimple, javaSimple0).inOrder()
            }

            @Test
            fun `kotlin from java`() {
                assertThat(
                    RepeatedAnyTest.deserialize(
                        JavaRepeatedAnyTest.newBuilder()
                            .addAllList(
                                listOf(
                                    com.google.protobuf.Any.pack(javaSimple),
                                    com.google.protobuf.Any.pack(javaSimple0)
                                )
                            ).build().toByteArray()
                    ).list.map { it.unpack(KtTest) }
                ).containsExactly(protoktSimple, protoktSimple0)
            }
        }

        @Nested
        inner class Packing {
            private val int64List = listOf(123L, 456L)

            @Test
            fun `kotlin packed smoke test`() {
                assertThat(
                    RepeatedPackedTest.deserialize(
                        RepeatedPackedTest { list = int64List }.serialize()
                    ).list
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `java packed from kotlin packed`() {
                assertThat(
                    JavaRepeatedPackedTest
                        .parseFrom(
                            RepeatedPackedTest { list = int64List }.serialize()
                        ).listList
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `kotlin packed from java packed`() {
                assertThat(
                    RepeatedPackedTest.deserialize(
                        JavaRepeatedPackedTest.newBuilder()
                            .addAllList(int64List)
                            .build()
                            .toByteArray()
                    ).list
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `kotlin unpacked smoke test`() {
                assertThat(
                    RepeatedUnpackedTest.deserialize(
                        RepeatedUnpackedTest { list = int64List }.serialize()
                    ).list
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `java unpacked from kotlin unpacked`() {
                assertThat(
                    JavaRepeatedUnpackedTest
                        .parseFrom(
                            RepeatedUnpackedTest { list = int64List }
                                .serialize()
                        ).listList
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `kotlin unpacked from java unpacked`() {
                assertThat(
                    RepeatedUnpackedTest.deserialize(
                        JavaRepeatedUnpackedTest.newBuilder()
                            .addAllList(int64List)
                            .build()
                            .toByteArray()
                    ).list
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `kotlin unpacked from packed smoke test`() {
                assertThat(
                    RepeatedUnpackedTest.deserialize(
                        RepeatedPackedTest { list = int64List }.serialize()
                    ).list
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `java unpacked from kotlin packed`() {
                assertThat(
                    JavaRepeatedUnpackedTest
                        .parseFrom(
                            RepeatedPackedTest { list = int64List }
                                .serialize()
                        ).listList
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `kotlin unpacked from java packed`() {
                assertThat(
                    RepeatedUnpackedTest.deserialize(
                        JavaRepeatedPackedTest.newBuilder()
                            .addAllList(int64List)
                            .build()
                            .toByteArray()
                    ).list
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `kotlin packed from unpacked smoke test`() {
                assertThat(
                    RepeatedPackedTest.deserialize(
                        RepeatedUnpackedTest { list = int64List }.serialize()
                    ).list
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `java packed from kotlin unpacked`() {
                assertThat(
                    JavaRepeatedPackedTest
                        .parseFrom(
                            RepeatedUnpackedTest { list = int64List }
                                .serialize()
                        ).listList
                ).containsExactlyElementsIn(int64List).inOrder()
            }

            @Test
            fun `kotlin packed from java unpacked`() {
                assertThat(
                    RepeatedPackedTest.deserialize(
                        JavaRepeatedUnpackedTest.newBuilder()
                            .addAllList(int64List)
                            .build()
                            .toByteArray()
                    ).list
                ).containsExactlyElementsIn(int64List).inOrder()
            }
        }
    }

    @Nested
    inner class Maps {
        @Test
        fun `kotlin smoke test`() {
            assertThat(
                MapTest.deserialize(
                    MapTest {
                        map =
                            mapOf(
                                content to protoktSimple,
                                content0 to protoktSimple0
                            )
                    }.serialize()
                ).map
            ).containsExactly(
                content, protoktSimple,
                content0, protoktSimple0
            )
        }

        @Test
        fun `java from kotlin`() {
            assertThat(
                JavaMapTest.parseFrom(
                    MapTest {
                        map =
                            mapOf(
                                content to protoktSimple,
                                content0 to protoktSimple0
                            )
                    }.serialize()
                ).mapMap
            ).containsExactly(
                content, javaSimple,
                content0, javaSimple0
            )
        }

        @Test
        fun `kotlin from java`() {
            assertThat(
                MapTest.deserialize(
                    JavaMapTest.newBuilder().apply {
                        putMap(content, javaSimple)
                        putMap(content0, javaSimple0)
                    }.build().toByteArray()
                ).map
            ).containsExactly(
                content, protoktSimple,
                content0, protoktSimple0
            )
        }

        @Test
        fun `java from kotlin with default value`() {
            assertThat(
                JavaMapTest.parseFrom(
                    MapTest {
                        map = mapOf("nullTest" to KtTest { })
                    }.serialize()
                ).mapMap
            ).containsExactly("nullTest", TestOuterClass.Test.getDefaultInstance())
        }

        @Test
        fun `kotlin from java with default value`() {
            assertThat(
                MapTest.deserialize(
                    JavaMapTest.newBuilder()
                        .putMap("nullTest", TestOuterClass.Test.getDefaultInstance())
                        .build()
                        .toByteArray()
                ).map
            ).containsExactly("nullTest", KtTest { })
        }
    }
}
