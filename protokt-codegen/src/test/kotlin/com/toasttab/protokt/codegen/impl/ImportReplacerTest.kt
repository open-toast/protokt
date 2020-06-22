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

package com.toasttab.protokt.codegen.impl

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.codegen.impl.ImportReplacer.replaceImports
import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.method
import com.toasttab.protokt.codegen.model.pclass
import com.toasttab.protokt.grpc.KtMarshaller
import io.grpc.MethodDescriptor
import io.grpc.MethodDescriptor.MethodType
import org.junit.jupiter.api.Test

class ImportReplacerTest {
    @Test
    fun `method import is replaced`() {
        val code = "com.toasttab.protokt.rt.finishMap(m)"

        assertThat(
            replaceImports(
                code,
                setOf(method("com.toasttab.protokt.rt", "finishMap"))
            )
        ).isEqualTo(
            "finishMap(m)"
        )
    }

    @Test
    fun `regular class import is replaced`() {
        val code = "com.toasttab.protokt.grpc.KtMarshaller(Empty)"

        assertThat(
            replaceImports(code, setOf(pclass(KtMarshaller::class)))
        ).isEqualTo(
            "KtMarshaller(Empty)"
        )
    }

    @Test
    fun `nested class import is replaced`() {
        val code = "io.grpc.MethodDescriptor.MethodType.UNARY"

        assertThat(
            replaceImports(code, setOf(pclass(MethodType::class)))
        ).isEqualTo(
            "MethodType.UNARY"
        )
    }

    @Test
    fun `class method import is replaced`() {
        val code = "io.grpc.MethodDescriptor.generateFullMethodName(xyz)"

        assertThat(
            replaceImports(
                code,
                setOf(
                    Import.ClassMethod(
                        PClass.fromClass(MethodDescriptor::class),
                        "generateFullMethodName"
                    )
                )
            )
        ).isEqualTo(
            "generateFullMethodName(xyz)"
        )
    }

    @Test
    fun `related imports don't clobber each other`() {
        val code =
            """
            io.grpc.MethodDescriptor.generateFullMethodName(xyz)
            io.grpc.MethodDescriptor.MethodType.UNARY
            io.grpc.MethodDescriptor.newBuilder<InT, OutT>()                
            """.trimIndent()

        assertThat(
            replaceImports(
                code,
                setOf(
                    pclass(MethodDescriptor::class),
                    pclass(MethodType::class),
                    Import.ClassMethod(
                        PClass.fromClass(MethodDescriptor::class),
                        "generateFullMethodName"
                    )
                )
            )
        ).isEqualTo(
            """
            generateFullMethodName(xyz)
            MethodType.UNARY
            MethodDescriptor.newBuilder<InT, OutT>()                
            """.trimIndent()
        )
    }
}
