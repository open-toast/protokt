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
import com.toasttab.protokt.rt.Unknown
import com.toasttab.protokt.rt.VarIntVal
import com.toasttab.protokt.testing.rt.Test as KtTest
import com.toasttab.protokt.testing.rt.TestOuterClass.Test as JavaTest
import org.junit.jupiter.api.Test

class UnknownFieldsInteropTest {
    private val content = "this is a test"
    private val content0 = "this is another test"

    private val bytesContent = Bytes(content.toByteArray())
    private val bytesContent0 = Bytes(content0.toByteArray())

    private val protoktSimple = KtTest { `val` = bytesContent }
    private val protoktSimple0 = KtTest { `val` = bytesContent0 }

    private val javaSimple =
        JavaTest.newBuilder()
            .setVal(ByteString.copyFrom(bytesContent.bytes))
            .build()

    private val javaSimple0 =
        JavaTest.newBuilder()
            .setVal(ByteString.copyFrom(bytesContent0.bytes))
            .build()

    private val unknowns = listOf(Unknown(666, 555), Unknown(777, 444))

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
                            UnknownFieldSet.Field.newBuilder()
                                .addVarint((it.value as VarIntVal).value)
                                .build()
                        )
                    }
                }.build()
            ).build()
        }.build()

    @Test
    fun `java deserialize from kotlin serialize with unknown fields`() {
        assertThat(JavaTest.parseFrom(protoktWithUnknowns.serialize()))
            .isEqualTo(javaWithUnknowns)
    }

    @Test
    fun `kotlin deserialize from java serialize with unknown fields`() {
        assertThat(KtTest.deserialize(javaWithUnknowns.toByteArray()))
            .isEqualTo(protoktWithUnknowns)
    }
}
