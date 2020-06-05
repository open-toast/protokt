/*
 * Copyright (c) 2019 Toast Inc.
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
import com.google.protobuf.UnknownFieldSet as JavaUnknownFieldSet
import com.toasttab.protokt.Timestamp
import com.toasttab.protokt.pack
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.FieldBuilder
import com.toasttab.protokt.rt.Unknown
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.VarintVal
import com.toasttab.protokt.testing.rt.Test as KtTest
import com.toasttab.protokt.unpack
import org.junit.jupiter.api.Test

class CollectionTest {
    val s0 = Bytes("this is a test".toByteArray())
    val s1 = Bytes("this is another test".toByteArray())
    val kotlinTest = KtTest { `val` = s0 }
    val kotlinTest2 = KtTest { `val` = s1 }
    val unknowns = listOf(Unknown.varint(666, 555), Unknown.varint(777, 444))
    val kotlinTest3 =
        KtTest {
            `val` = s1
            unknownFields = UnknownFieldSet.from(
                unknowns.map {
                    it.fieldNumber to FieldBuilder().add(it.value)
                }.toMap()
            )
        }
    val unknownFieldSet = JavaUnknownFieldSet.newBuilder().let { f ->
        unknowns.forEach {
            f.addField(
                it.fieldNumber,
                JavaUnknownFieldSet.Field.newBuilder()
                    .addVarint((it.value as VarintVal).value.value).build()
            )
        }
        f.build()
    }
    val javaTest = TestOuterClass.Test.newBuilder()
        .setVal(ByteString.copyFrom(s0.bytes)).build()
    val javaTest2 = TestOuterClass.Test.newBuilder()
        .setVal(ByteString.copyFrom(s1.bytes)).build()
    val javaTest3 = TestOuterClass.Test.newBuilder()
        .setVal(ByteString.copyFrom(s1.bytes))
        .setUnknownFields(unknownFieldSet).build()
    val stringList = listOf("string1", "string2")
    val int64List = listOf(123L, 456L)
    val ts0 = JavaTimestamp.newBuilder()
        .setSeconds(System.currentTimeMillis() * 1000).build()
    val ts00 =
        Timestamp {
            seconds = ts0.seconds
            nanos = ts0.nanos
        }
    val ts1 = JavaTimestamp.newBuilder()
        .setSeconds((System.currentTimeMillis() * 1000) - 60).build()
    val ts01 =
        Timestamp {
            seconds = ts1.seconds
            nanos = ts1.nanos
        }

    @Test
    fun `kotlin message test`() {
        assertThat(kotlinTest).isEqualTo(KtTest.deserialize(kotlinTest.serialize()))
    }

    @Test
    fun `kotlin → java test`() {
        assertThat(javaTest).isEqualTo(TestOuterClass.Test.parseFrom(kotlinTest.serialize()))
    }

    @Test
    fun `java → kotlin test`() {
        assertThat(kotlinTest).isEqualTo(KtTest.deserialize(javaTest.toByteArray()))
    }

    @Test
    fun `java → kotlin unknown field test`() {
        assertThat(kotlinTest3).isEqualTo(KtTest.deserialize(javaTest3.toByteArray()))
    }

    @Test
    fun `kotlin list test`() {
        val lt0 = ListTest { list = listOf(kotlinTest, kotlinTest2) }
        val ltb = lt0.serialize()
        val lt = ListTest.deserialize(ltb)
        assertThat(lt.list).containsExactly(kotlinTest, kotlinTest2)
    }

    @Test
    fun `kotlin → java list test`() {
        assertThat(TestOuterClass.ListTest.parseFrom(
            ListTest { list = listOf(kotlinTest, kotlinTest2) }
                .serialize()).listList).containsExactly(javaTest, javaTest2)
    }

    @Test
    fun `java → kotlin list test`() {
        assertThat(ListTest.deserialize(
            TestOuterClass.ListTest.parseFrom(
                ListTest { list = listOf(kotlinTest, kotlinTest2) }
                    .serialize()).toByteArray()).list)
            .containsExactly(kotlinTest, kotlinTest2)
    }

    @Test
    fun `kotlin map test`() {
        assertThat(
            MapTest.deserialize(
                MapTest {
                    map =
                        mapOf(
                            kotlinTest.`val`.bytes.toString() to kotlinTest,
                            kotlinTest2.`val`.bytes.toString() to kotlinTest2
                        )
                }.serialize()
            ).map.values
        ).containsExactly(kotlinTest, kotlinTest2)
    }

    @Test
    fun `kotlin → java map test`() {
        assertThat(
            TestOuterClass.MapTest.parseFrom(
                MapTest {
                    map =
                        mapOf(
                            kotlinTest.`val`.bytes.toString() to kotlinTest,
                            kotlinTest2.`val`.bytes.toString() to kotlinTest2
                        )
                }.serialize()
            ).mapMap.values
        ).containsExactly(javaTest, javaTest2)
    }

    @Test
    fun `java → kotlin map test`() {
        assertThat(
            MapTest.deserialize(
                TestOuterClass.MapTest.newBuilder()
                    .putMap("nullTest", TestOuterClass.Test.newBuilder().build())
                    .putMap(javaTest.`val`.toStringUtf8(), javaTest)
                    .putMap(javaTest2.`val`.toStringUtf8(), javaTest2)
                    .build().toByteArray()
            ).map.values
        ).containsAtLeast(kotlinTest, kotlinTest2)
    }

    @Test
    fun `repeated test`() {
        assertThat(
            RepeatedTest.deserialize(
                RepeatedTest { list = stringList }.serialize()
            ).list
        ).isEqualTo(stringList)
    }

    @Test
    fun `repeated packed kotlin → kotlin test`() {
        assertThat(
            RepeatedPackedTest.deserialize(
                RepeatedPackedTest { list = int64List }.serialize()
            ).list
        ).isEqualTo(int64List)
    }

    @Test
    fun `repeated packed java → kotlin test`() {
        assertThat(
            RepeatedPackedTest.deserialize(
                TestOuterClass.RepeatedPackedTest.newBuilder()
                    .addList(123L)
                    .addList(456L)
                    .build().toByteArray()
            ).list
        ).containsExactly(123L, 456L)
    }

    @Test
    fun `repeated packed kotlin → java test`() {
        assertThat(
            TestOuterClass.RepeatedPackedTest
                .parseFrom(RepeatedPackedTest { list = listOf(123L, 456L) }.serialize())
                .listList
        ).containsExactly(123L, 456L)
    }

    @Test
    fun `kotlin repeated wkt test`() {
        assertThat(
            RepeatedWktTest.deserialize(
                RepeatedWktTest { list = listOf(ts01, ts00) }.serialize()
            ).list
        ).containsExactly(ts01, ts00)
    }

    @Test
    fun `kotlin to java repeated wkt test`() {
        assertThat(
            RepeatedWktTest.deserialize(
                TestOuterClass.RepeatedWktTest.newBuilder()
                    .addList(ts1)
                    .addList(ts0)
                    .build().toByteArray()
            ).list
        ).containsExactly(ts01, ts00)
    }

    @Test
    fun `kotlin repeated any test`() {
        assertThat(RepeatedAnyTest.deserialize(
            RepeatedAnyTest {
                list = listOf(
                    com.toasttab.protokt.Any.pack(kotlinTest),
                    com.toasttab.protokt.Any.pack(kotlinTest2)
                )
            }.serialize()
        ).list.map { it.unpack(KtTest) }).containsExactly(kotlinTest, kotlinTest2)
    }

    @Test
    fun `java → kotlin repeated any test`() {
        assertThat(
            RepeatedAnyTest.deserialize(
                TestOuterClass.RepeatedAnyTest.newBuilder()
                    .addList(com.google.protobuf.Any.pack(javaTest))
                    .addList(com.google.protobuf.Any.pack(javaTest2))
                    .build().toByteArray()
            ).list.map { it.unpack(KtTest) }
        ).containsExactly(kotlinTest, kotlinTest2)
    }

    @Test
    fun `kotlin → java repeated any test`() {
        val tests = TestOuterClass.RepeatedAnyTest.parseFrom(
            RepeatedAnyTest {
                list = listOf(
                    com.toasttab.protokt.Any.pack(kotlinTest),
                    com.toasttab.protokt.Any.pack(kotlinTest2)
                )
            }.serialize()
        ).listList.map { it.unpack(javaTest.javaClass) }

        assertThat(tests).containsExactly(javaTest, javaTest2)
    }
}
