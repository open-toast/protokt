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

package com.toasttab.protokt.codegen.annotators

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.impl.Nullability.hasNonNullOption
import com.toasttab.protokt.codegen.impl.Wrapper.interceptFieldSizeof
import com.toasttab.protokt.codegen.impl.Wrapper.interceptSizeof
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.impl.Wrapper.mapKeyConverter
import com.toasttab.protokt.codegen.impl.Wrapper.mapValueConverter
import com.toasttab.protokt.codegen.impl.namedCodeBlock
import com.toasttab.protokt.codegen.impl.runtimeFunction
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UInt32

internal class MessageSizeAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private val resultVarName =
        run {
            var name = "result"
            while (msg.fields.any { it.fieldName == name }) {
                name += "_"
            }
            name
        }

    private fun annotateMessageSize(): FunSpec {
        val fieldSizes =
            msg.mapFields(
                ctx,
                { CodeBlock.of("$resultVarName·+=·%L", sizeOf(it, ctx)) },
                { oneof, std -> oneofSizeOfString(oneof, std) },
                {
                    if (it.hasNonNullOption) {
                        add("$resultVarName·+=·")
                    }
                }
            )

        return FunSpec.builder("messageSize")
            .addModifiers(KModifier.PRIVATE)
            .returns(Int::class)
            .addCode(
                if (fieldSizes.isEmpty()) {
                    CodeBlock.of("return·unknownFields.size()")
                } else {
                    buildCodeBlock {
                        addStatement("var·$resultVarName·=·0")
                        fieldSizes.forEach { fs -> add(fs) }
                        addStatement("$resultVarName·+=·unknownFields.size()")
                        addStatement("return·$resultVarName")
                    }
                }
            )
            .build()
    }

    private fun oneofSizeOfString(o: Oneof, f: StandardField) =
        sizeOf(
            f,
            ctx,
            interceptSizeof(
                f,
                "${o.fieldName}.${f.fieldName}",
                ctx
            )
        ).let { s ->
            if (o.hasNonNullOption) {
                s
            } else {
                CodeBlock.of("$resultVarName·+=·%L", s)
            }
        }

    companion object {
        fun annotateMessageSize(msg: Message, ctx: Context) =
            MessageSizeAnnotator(msg, ctx).annotateMessageSize()

        fun sizeOf(
            f: StandardField,
            ctx: Context,
            oneOfFieldAccess: CodeBlock? = null
        ): CodeBlock {
            val name =
                oneOfFieldAccess
                    ?: if (f.repeated) {
                        CodeBlock.of(f.fieldName)
                    } else {
                        interceptSizeof(f, f.fieldName, ctx)
                    }

            return when {
                f.map -> sizeOfMap(f, name, ctx)
                f.repeated && f.packed -> {
                    namedCodeBlock(
                        "%sizeof:M(%tag:T(${f.number})) + " +
                            "%name:L.sumOf·{ %sizeof:M(%box:L) }.let·{ it + %sizeof:M(%uInt32:T(it)) }",
                        mapOf(
                            "sizeof" to runtimeFunction("sizeof"),
                            "tag" to Tag::class,
                            "uInt32" to UInt32::class,
                            "box" to f.box(CodeBlock.of("it")),
                            "name" to name
                        )
                    )
                }
                f.repeated -> {
                    namedCodeBlock(
                        "(%sizeof:M(%tag:T(${f.number})) * %name:L.size) + " +
                            "%name:L.sumOf·{ %sizeof:M(%boxedAccess:L) }",
                        mapOf(
                            "sizeof" to runtimeFunction("sizeof"),
                            "tag" to Tag::class,
                            "boxedAccess" to f.box(interceptValueAccess(f, ctx, "it")),
                            "name" to name
                        )
                    )
                }
                else -> {
                    namedCodeBlock(
                        "%sizeof:M(%tag:T(${f.number})) + %access:L",
                        mapOf(
                            "sizeof" to runtimeFunction("sizeof"),
                            "tag" to Tag::class,
                            "access" to interceptFieldSizeof(f, name, ctx)
                        )
                    )
                }
            }
        }

        private fun sizeOfMap(
            f: StandardField,
            name: CodeBlock,
            ctx: Context
        ): CodeBlock {
            val key =
                mapKeyConverter(f, ctx)
                    ?.let { CodeBlock.of("%T.unwrap(k)", it) }
                    ?: CodeBlock.of("k")

            val value =
                mapValueConverter(f, ctx)
                    ?.let { CodeBlock.of("%T.unwrap(v)", it) }
                    ?: CodeBlock.of("v")

            return CodeBlock.of(
                "%M($name, %T(${f.number})) { k, v -> %T.sizeof(%L, %L)}",
                runtimeFunction("sizeofMap"),
                Tag::class,
                f.typePClass.toTypeName(),
                key,
                value
            )
        }
    }
}
