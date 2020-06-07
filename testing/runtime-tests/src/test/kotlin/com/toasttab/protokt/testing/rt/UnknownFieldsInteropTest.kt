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
import com.google.protobuf.UnknownFieldSet
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.Fixed32
import com.toasttab.protokt.rt.Fixed32Val
import com.toasttab.protokt.rt.Fixed64
import com.toasttab.protokt.rt.Fixed64Val
import com.toasttab.protokt.rt.LengthDelimitedVal
import com.toasttab.protokt.rt.Unknown
import com.toasttab.protokt.rt.VarIntVal
import com.toasttab.protokt.testing.rt.Test as KtTest
import com.toasttab.protokt.testing.rt.TestOuterClass.Test as JavaTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class UnknownFieldsInteropTest {
    private val content = "this is a test".toByteArray()
    private val protoktSimple = KtTest { `val` = Bytes(content) }

    private val javaSimple =
        JavaTest.newBuilder()
            .setVal(ByteString.copyFrom(content))
            .build()

    private val unknowns = listOf(
        Unknown(111, VarIntVal(111)),
        Unknown(222, Fixed32Val(Fixed32(222))),
        Unknown(333, Fixed64Val(Fixed64(333))),
        Unknown(444, LengthDelimitedVal("some string".toByteArray()))
    )

    private val protoktWithUnknowns =
        protoktSimple.copy {
            unknown = unknowns.associateBy { it.fieldNum }
        }

    private val javaWithUnknowns =
        javaSimple.toBuilder().apply {
            setUnknownFields(
                UnknownFieldSet.newBuilder().apply {
                    unknowns.forEach {
                        addField(
                            it.fieldNum,
                            UnknownFieldSet.Field.newBuilder().apply {
                                when (val v = it.value) {
                                    is VarIntVal -> addVarint(v.value)
                                    is Fixed32Val -> addFixed32(v.value.value)
                                    is Fixed64Val -> addFixed64(v.value.value)
                                    is LengthDelimitedVal ->
                                        addLengthDelimited(ByteString.copyFrom(v.value))
                                }
                            }.build()
                        )
                    }
                }.build()
            ).build()
        }.build()

    @Test
    @Disabled("Broken UnknownValue equals implementation (https://github.com/open-toast/protokt/pull/64)")
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
    @Disabled("Broken UnknownValue equals implementation (https://github.com/open-toast/protokt/pull/64)")
    fun `kotlin from java`() {
        assertThat(KtTest.deserialize(javaWithUnknowns.toByteArray()))
            .isEqualTo(protoktWithUnknowns)
    }
}
