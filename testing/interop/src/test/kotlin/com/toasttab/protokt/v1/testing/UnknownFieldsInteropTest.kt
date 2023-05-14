/*
 * Copyright (c) 2020 Toast, Inc.
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

package com.toasttab.protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.ByteString
import com.toasttab.protokt.v1.Bytes
import com.toasttab.protokt.v1.Fixed32Val
import com.toasttab.protokt.v1.Fixed64Val
import com.toasttab.protokt.v1.LengthDelimitedVal
import com.toasttab.protokt.v1.UnknownField
import com.toasttab.protokt.v1.UnknownFieldSet
import com.toasttab.protokt.v1.VarintVal
import org.junit.jupiter.api.Test
import com.google.protobuf.UnknownFieldSet as JavaUnknownFieldSet
import com.toasttab.protokt.v1.testing.TestOuterClass.Test as JavaTest
import toasttab.protokt.v1.testing.Test as KtTest

class UnknownFieldsInteropTest {
    private val content = "this is a test".toByteArray()
    private val protoktSimple = KtTest { `val` = Bytes(content) }

    private val javaSimple =
        JavaTest.newBuilder()
            .setVal(ByteString.copyFrom(content))
            .build()

    private val unknowns = listOf(
        UnknownField.varint(111u, 111),
        UnknownField.fixed32(222u, 222u),
        UnknownField.fixed64(333u, 333u),
        UnknownField.lengthDelimited(444u, "some string".toByteArray())
    )

    private val protoktWithUnknowns =
        protoktSimple.copy {
            unknownFields =
                UnknownFieldSet.Builder()
                    .apply { unknowns.forEach { add(it) } }
                    .build()
        }

    private val javaWithUnknowns =
        javaSimple.toBuilder().apply {
            setUnknownFields(
                JavaUnknownFieldSet.newBuilder().apply {
                    unknowns.forEach {
                        addField(
                            it.fieldNumber.toInt(),
                            JavaUnknownFieldSet.Field.newBuilder().apply {
                                when (val v = it.value) {
                                    is VarintVal -> addVarint(v.value.value.toLong())
                                    is Fixed32Val -> addFixed32(v.value.value.toInt())
                                    is Fixed64Val -> addFixed64(v.value.value.toLong())
                                    is LengthDelimitedVal ->
                                        addLengthDelimited(
                                            ByteString.copyFrom(v.value.bytes)
                                        )
                                }
                            }.build()
                        )
                    }
                }.build()
            ).build()
        }.build()

    @Test
    fun `kotlin smoke test`() {
        assertThat(KtTest.deserialize(protoktWithUnknowns.serialize()))
            .isEqualTo(protoktWithUnknowns)
    }

    @Test
    fun `java from kotlin`() {
        assertThat(JavaTest.parseFrom(protoktWithUnknowns.serialize()))
            .isEqualTo(javaWithUnknowns)
    }

    @Test
    fun `kotlin from java`() {
        assertThat(KtTest.deserialize(javaWithUnknowns.toByteArray()))
            .isEqualTo(protoktWithUnknowns)
    }
}
