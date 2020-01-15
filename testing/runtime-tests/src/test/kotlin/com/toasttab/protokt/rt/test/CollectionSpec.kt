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

package com.toasttab.protokt.rt.test

import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp as JavaTimestamp
import com.google.protobuf.UnknownFieldSet
import com.toasttab.protokt.Timestamp
import com.toasttab.protokt.pack
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.Unknown
import com.toasttab.protokt.rt.VarIntVal
import com.toasttab.protokt.unpack
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainValues
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import test.kt.ListTest
import test.kt.MapTest
import test.kt.RepeatedAnyTest
import test.kt.RepeatedPackedTest
import test.kt.RepeatedTest
import test.kt.RepeatedWktTest
import test.kt.Test
import test.kt.TestOuterClass

class CollectionSpec : StringSpec({
    val s0 = Bytes("this is a test".toByteArray())
    val s1 = Bytes("this is another test".toByteArray())
    val kotlinTest = Test(s0)
    val kotlinTest2 = Test(s1)
    val unknowns = listOf(Unknown(666, 555), Unknown(777, 444))
    val kotlinTest3 = Test(s1, unknowns.map { it.fieldNum to it }.toMap())
    val unknownFieldSet = UnknownFieldSet.newBuilder().let { f ->
        unknowns.forEach {
            f.addField(
                it.fieldNum,
                UnknownFieldSet.Field.newBuilder()
                    .addVarint((it.value as VarIntVal).value).build())
        }
        f.build()
    }
    val javaTest = TestOuterClass.Test.newBuilder()
        .setVal(ByteString.copyFrom(s0.value)).build()
    val javaTest2 = TestOuterClass.Test.newBuilder()
        .setVal(ByteString.copyFrom(s1.value)).build()
    val javaTest3 = TestOuterClass.Test.newBuilder()
        .setVal(ByteString.copyFrom(s1.value))
        .setUnknownFields(unknownFieldSet).build()
    val stringList = listOf("string1", "string2")
    val int64List = listOf(123L, 456L)
    val ts0 = JavaTimestamp.newBuilder()
        .setSeconds(System.currentTimeMillis() * 1000).build()
    val ts00 = Timestamp(ts0.seconds, ts0.nanos)
    val ts1 = JavaTimestamp.newBuilder()
        .setSeconds((System.currentTimeMillis() * 1000) - 60).build()
    val ts01 = Timestamp(ts1.seconds, ts1.nanos)

    "kotlin message test" {
        kotlinTest shouldBe Test.deserialize(kotlinTest.serialize())
    }

    "kotlin -> java test" {
        javaTest shouldBe TestOuterClass.Test.parseFrom(kotlinTest.serialize())
    }

    "java -> kotlin test" {
        kotlinTest shouldBe Test.deserialize(javaTest.toByteArray())
    }

    "java -> kotlin unknown field test" {
        kotlinTest3 shouldBe Test.deserialize(javaTest3.toByteArray())
    }

    "kotlin list test" {
        val lt0 = ListTest(listOf(kotlinTest, kotlinTest2))
        val ltb = lt0.serialize()
        val lt = ListTest.deserialize(ltb)
        lt.list.shouldContainAll(kotlinTest, kotlinTest2)
    }

    "kotlin -> java list test" {
        TestOuterClass.ListTest.parseFrom(
            ListTest(listOf(kotlinTest, kotlinTest2))
                .serialize()).listList.shouldContainAll(javaTest, javaTest2)
    }

    "java -> kotlin list test" {
        ListTest.deserialize(
            TestOuterClass.ListTest.parseFrom(
                ListTest(listOf(kotlinTest, kotlinTest2))
                    .serialize()).toByteArray()).list
                    .shouldContainAll(kotlinTest, kotlinTest2)
    }

    "kotlin map test" {
        MapTest.deserialize(MapTest(mapOf(
            kotlinTest.`val`.value.toString() to kotlinTest,
            kotlinTest2.`val`.value.toString() to kotlinTest2)).serialize())
            .map.shouldContainValues(kotlinTest, kotlinTest2)
    }

    "kotlin -> java map test" {
        TestOuterClass.MapTest.parseFrom(MapTest(mapOf(
            kotlinTest.`val`.value.toString() to kotlinTest,
            kotlinTest2.`val`.value.toString() to kotlinTest2)).serialize())
            .mapMap.shouldContainValues(javaTest, javaTest2)
    }

    "java -> kotlin map test" {
        MapTest.deserialize(TestOuterClass.MapTest.newBuilder()
            .putMap("nullTest", TestOuterClass.Test.newBuilder().build())
            .putMap(javaTest.`val`.toStringUtf8(), javaTest)
            .putMap(javaTest2.`val`.toStringUtf8(), javaTest2)
            .build().toByteArray())
            .map.shouldContainValues(kotlinTest, kotlinTest2)
    }

    "repeated test" {
        RepeatedTest.deserialize(
            RepeatedTest(stringList).serialize())
            .list.shouldContainAll(stringList)
    }

    "repeated packed kotlin -> kotlin test" {
        RepeatedPackedTest.deserialize(
            RepeatedPackedTest(int64List).serialize())
            .list.shouldContainAll(int64List)
    }

    "repeated packed java -> kotlin test" {
        RepeatedPackedTest.deserialize(
            TestOuterClass.RepeatedPackedTest.newBuilder()
                .addList(123L)
                .addList(456L)
                .build().toByteArray())
            .list.shouldContainAll(123L, 456L)
    }

    "repeated packed kotlin -> java test" {
        TestOuterClass.RepeatedPackedTest
            .parseFrom(RepeatedPackedTest(listOf(123L, 456L)).serialize())
            .listList.shouldContainAll(123L, 456L)
    }

    "kotlin repeated wkt test" {
        RepeatedWktTest.deserialize(
            RepeatedWktTest(listOf(ts01, ts00)).serialize())
            .list.shouldContainAll(ts01, ts00)
    }

    "kotlin to java repeated wkt test" {
        RepeatedWktTest.deserialize(
            TestOuterClass.RepeatedWktTest.newBuilder()
            .addList(ts1)
            .addList(ts0)
            .build().toByteArray())
            .list.shouldContainAll(ts01, ts00)
    }

    "kotlin repeated any test" {
        RepeatedAnyTest.deserialize(
            RepeatedAnyTest(
                listOf(
                    com.toasttab.protokt.Any.pack(kotlinTest),
                    com.toasttab.protokt.Any.pack(kotlinTest2)
                )
            ).serialize())
            .list.map { it.unpack(Test) }
            .shouldContainAll(kotlinTest, kotlinTest2)
    }

    "java -> kotlin repeated any test" {
        RepeatedAnyTest.deserialize(
            TestOuterClass.RepeatedAnyTest.newBuilder()
            .addList(com.google.protobuf.Any.pack(javaTest))
            .addList(com.google.protobuf.Any.pack(javaTest2))
            .build().toByteArray())
            .list.map { it.unpack(Test) }
            .shouldContainAll(kotlinTest, kotlinTest2)
    }

    "kotlin -> java repeated any test" {
        val tests = TestOuterClass.RepeatedAnyTest.parseFrom(
            RepeatedAnyTest(
                listOf(
                    com.toasttab.protokt.Any.pack(kotlinTest),
                    com.toasttab.protokt.Any.pack(kotlinTest2)
                )
            ).serialize())
            .listList.map { it.unpack(javaTest.javaClass) }
        tests.size shouldBe 2
        tests.shouldContainAll(javaTest, javaTest2)
    }
})
