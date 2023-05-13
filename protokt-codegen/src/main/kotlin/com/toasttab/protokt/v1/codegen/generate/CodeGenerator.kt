/*
 * Copyright (c) 2019 Toast, Inc.
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

package com.toasttab.protokt.v1.codegen.generate

import com.squareup.kotlinpoet.TypeSpec
import com.toasttab.protokt.v1.codegen.util.Enum
import com.toasttab.protokt.v1.codegen.util.ErrorContext.withEnumName
import com.toasttab.protokt.v1.codegen.util.ErrorContext.withMessageName
import com.toasttab.protokt.v1.codegen.util.ErrorContext.withServiceName
import com.toasttab.protokt.v1.codegen.util.GeneratedType
import com.toasttab.protokt.v1.codegen.util.GeneratorContext
import com.toasttab.protokt.v1.codegen.util.Message
import com.toasttab.protokt.v1.codegen.util.ProtoFileContents
import com.toasttab.protokt.v1.codegen.util.ProtoFileInfo
import com.toasttab.protokt.v1.codegen.util.Service
import com.toasttab.protokt.v1.codegen.util.TopLevelType

object CodeGenerator {
    data class Context(
        val enclosing: List<Message>,
        val info: ProtoFileInfo
    )

    fun generate(contents: ProtoFileContents) =
        contents.types.flatMap {
            com.toasttab.protokt.v1.codegen.generate.CodeGenerator.generate(
                it,
                com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context(emptyList(), contents.info)
            )
                .map { type -> GeneratedType(it, type) }
        }

    fun generate(type: TopLevelType, ctx: com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context): Iterable<TypeSpec> =
        when (type) {
            is Message ->
                withMessageName(type.className) {
                    com.toasttab.protokt.v1.codegen.generate.CodeGenerator.nonGrpc(ctx) {
                        com.toasttab.protokt.v1.codegen.generate.CodeGenerator.nonDescriptors(ctx) {
                            listOf(
                                com.toasttab.protokt.v1.codegen.generate.generateMessage(
                                    type,
                                    ctx.copy(enclosing = ctx.enclosing + type)
                                )
                            )
                        }
                    }
                }
            is Enum ->
                withEnumName(type.className) {
                    com.toasttab.protokt.v1.codegen.generate.CodeGenerator.nonGrpc(ctx) {
                        com.toasttab.protokt.v1.codegen.generate.CodeGenerator.nonDescriptors(ctx) {
                            listOf(com.toasttab.protokt.v1.codegen.generate.generateEnum(type, ctx))
                        }
                    }
                }
            is Service ->
                withServiceName(type.name) {
                    com.toasttab.protokt.v1.codegen.generate.generateService(
                        type,
                        ctx,
                        ctx.info.context.generateGrpc ||
                            ctx.info.context.onlyGenerateGrpc
                    )
                }
        }

    private fun <T> nonDescriptors(ctx: com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context, gen: () -> Iterable<T>) =
        com.toasttab.protokt.v1.codegen.generate.CodeGenerator.nonDescriptors(ctx.info.context, emptyList(), gen)

    private fun <T> nonDescriptors(ctx: GeneratorContext, default: T, gen: () -> T) =
        com.toasttab.protokt.v1.codegen.generate.CodeGenerator.boolGen(!ctx.onlyGenerateDescriptors, default, gen)

    private fun <T> nonGrpc(ctx: com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context, gen: () -> Iterable<T>) =
        com.toasttab.protokt.v1.codegen.generate.CodeGenerator.nonGrpc(ctx.info.context, emptyList(), gen)

    private fun <T> nonGrpc(ctx: GeneratorContext, default: T, gen: () -> T) =
        com.toasttab.protokt.v1.codegen.generate.CodeGenerator.boolGen(!ctx.onlyGenerateGrpc, default, gen)

    private fun <T> boolGen(bool: Boolean, default: T, gen: () -> T) =
        if (bool) {
            gen()
        } else {
            default
        }
}
