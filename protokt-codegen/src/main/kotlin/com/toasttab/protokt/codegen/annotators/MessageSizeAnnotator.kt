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
import com.toasttab.protokt.codegen.impl.add
import com.toasttab.protokt.codegen.impl.addStatement
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
            msg.fields.map {
                when (it) {
                    is StandardField -> {
                        val addFieldSize = CodeBlockComponents("$resultVarName +=") + sizeOf(it, ctx)
                        if (it.hasNonNullOption) {
                            addFieldSize.toCodeBlock()
                        } else {
                            buildCodeBlock {
                                beginControlFlow("if·${it.nonDefault(ctx)}")
                                add(addFieldSize)
                                endControlFlow()
                            }
                        }
                    }
                    is Oneof -> {
                        buildCodeBlock {
                            if (it.hasNonNullOption) {
                                add("$resultVarName += ")
                            }
                            beginControlFlow("when·(${it.fieldName})")
                            conditionals(it).forEach(::add)
                            endControlFlow()
                        }
                    }
                }
            }

        return FunSpec.builder("messageSize")
            .addModifiers(KModifier.PRIVATE)
            .returns(Int::class)
            .addCode(
                if (fieldSizes.isEmpty()) {
                    CodeBlock.of("return·unknownFields.size()")
                } else {
                    buildCodeBlock {
                        addStatement("var·$resultVarName·=·0")
                        fieldSizes.forEach { fs -> addStatement(fs) }
                        addStatement("$resultVarName·+=·unknownFields.size()")
                        addStatement("return·$resultVarName")
                    }
                }
            )
            .build()
    }

    private fun conditionals(f: Oneof) =
        f.fields
            .sortedBy { it.number }
            .map {
                buildCodeBlock {
                    beginControlFlow("is·${condition(f, it, msg.name)}·->")
                    add(oneofSizeOfString(f, it))
                    endControlFlow()
                }
            }
            .let {
                if (f.hasNonNullOption) {
                    it
                } else {
                    it + CodeBlock.of("null·-> Unit")
                }
            }

    private fun condition(f: Oneof, ff: StandardField, type: String) =
        "${oneOfScope(f, type)}.${f.fieldTypeNames.getValue(ff.fieldName)}"

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
            if (!o.hasNonNullOption) {
                s.prepend("$resultVarName·+=")
            } else {
                s
            }
        }.toCodeBlock()

    companion object {
        fun annotateMessageSize(msg: Message, ctx: Context) =
            MessageSizeAnnotator(msg, ctx).annotateMessageSize()

        fun sizeOf(
            f: StandardField,
            ctx: Context,
            oneOfFieldAccess: String? = null
        ): CodeBlockComponents {
            val name =
                oneOfFieldAccess
                    ?: if (f.repeated) {
                        f.fieldName
                    } else {
                        interceptSizeof(f, f.fieldName, ctx)
                    }

            // allow results to be combined without arg collisions
            fun suffix(arg: String) = "${arg}_${f.fieldName.replace("`", "")}"
            val sizeof = suffix("sizeof")
            val tag = suffix("tag")
            val uInt32 = suffix("uInt32")
            val boxedAccess = suffix("boxedAccess")
            val access = suffix("access")

            return when {
                f.map -> sizeOfMap(f, name, ctx)
                f.repeated && f.packed -> {
                    CodeBlockComponents(
                        "%$sizeof:M(%$tag:T(${f.number})) + " +
                            "$name.sumOf·{ %$sizeof:M(${f.box("it")}) }.let·{ it + %$sizeof:M(%$uInt32:T(it)) }",
                        mapOf(
                            sizeof to runtimeFunction("sizeof"),
                            tag to Tag::class,
                            uInt32 to UInt32::class
                        )
                    )
                }
                f.repeated -> {
                    CodeBlockComponents(
                        "(%$sizeof:M(%$tag:T(${f.number})) * $name.size) + " +
                            "$name.sumOf·{ %$sizeof:M(%$boxedAccess:L) }",
                        mapOf(
                            sizeof to runtimeFunction("sizeof"),
                            tag to Tag::class,
                            boxedAccess to f.box(interceptValueAccess(f, ctx, "it"))
                        )
                    )
                }
                else -> {
                    CodeBlockComponents(
                        "%$sizeof:M(%$tag:T(${f.number})) + %$access:L",
                        mapOf(
                            sizeof to runtimeFunction("sizeof"),
                            tag to Tag::class,
                            access to interceptFieldSizeof(f, name, ctx)
                        )
                    )
                }
            }
        }

        private fun sizeOfMap(
            f: StandardField,
            name: String,
            ctx: Context
        ): CodeBlockComponents {
            val key = mapKeyConverter(f, ctx)?.let { "$it.unwrap(k)" } ?: "k"
            val value = mapValueConverter(f, ctx)?.let { CodeBlock.of("$it.unwrap(v)") } ?: "v"
            return CodeBlockComponents(
                "%arg1:M($name, %arg2:T(${f.number})) { k, v -> %arg3:T.sizeof(%arg4:L, %arg5:L)}",
                mapOf(
                    "arg1" to runtimeFunction("sizeofMap"),
                    "arg2" to Tag::class,
                    "arg3" to f.typePClass.toTypeName(),
                    "arg4" to key,
                    "arg5" to value
                )
            )
        }
    }
}
