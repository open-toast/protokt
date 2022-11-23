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
import com.toasttab.protokt.codegen.template.ConditionalParams
import com.toasttab.protokt.codegen.template.Message.Message.SizeofInfo
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

    private fun annotateMessageSizeNew(): FunSpec {
        val fieldSizes =
            msg.fields.map {
                when (it) {
                    is StandardField -> {
                        val addFieldSize =
                            sizeOfString(it)
                                .prepend("$resultVarName +=")
                                .append("\n") // TODO: Not sure why this is needed
                                .toCodeBlock()
                        if (it.hasNonNullOption) {
                            addFieldSize
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
                        fieldSizes.forEach { fs -> add(fs) }
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
                    it + buildCodeBlock {
                        addStatement("null·-> Unit")
                    }
                }
            }

    private fun condition(f: Oneof, ff: StandardField, type: String) =
        "${oneOfScope(f, type)}.${f.fieldTypeNames.getValue(ff.fieldName)}"

    private fun oneofSizeOfString(o: Oneof, f: StandardField) =
        sizeOfString(
            f,
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

    private fun annotateMessageSizeOld(): List<SizeofInfo> {
        return msg.fields.map {
            when (it) {
                is StandardField ->
                    SizeofInfo(
                        it.fieldName,
                        listOf(
                            ConditionalParams(
                                it.nonDefault(ctx),
                                sizeOfString(it).toCodeBlock()
                            )
                        )
                    )
                is Oneof ->
                    SizeofInfo(
                        it.fieldName,
                        oneofSize(it, msg.name)
                    )
            }
        }
    }

    private class CodeBlockComponents(
        val formatWithNamedArgs: String,
        val args: Map<String, Any> = emptyMap()
    ) {
        fun prepend(
            formatWithNamedArgs: String,
            args: Map<String, Any> = emptyMap()
        ): CodeBlockComponents {
            val intersect = args.keys.intersect(this.args.keys)
            check(intersect.isEmpty()) {
                "duplicate keys in args: $intersect"
            }
            return CodeBlockComponents(
                formatWithNamedArgs + " " + this.formatWithNamedArgs,
                args + this.args
            )
        }

        fun append(
            formatWithNamedArgs: String,
            args: Map<String, Any> = emptyMap()
        ): CodeBlockComponents {
            val intersect = args.keys.intersect(this.args.keys)
            check(intersect.isEmpty()) {
                "duplicate keys in args: $intersect"
            }
            return CodeBlockComponents(
                this.formatWithNamedArgs + " " + formatWithNamedArgs,
                args + this.args
            )
        }

        fun toCodeBlock() =
            namedCodeBlock(formatWithNamedArgs, args)
    }

    private fun sizeOfString(
        f: StandardField,
        oneOfFieldAccess: String? = null
    ): CodeBlockComponents {
        val name =
            oneOfFieldAccess
                ?: if (f.repeated) {
                    f.fieldName
                } else {
                    interceptSizeof(f, f.fieldName, ctx)
                }

        return when {
            f.map -> sizeOfMap(f, name)
            f.repeated && f.packed -> {
                CodeBlockComponents(
                    "%sizeof:M(%tag:T(${f.number})) + " +
                        "$name.sumOf·{ %sizeof:M(${f.box("it")}) }.let·{ it + %sizeof:M(%uInt32:T(it)) }",
                    mapOf(
                        "sizeof" to runtimeFunction("sizeof"),
                        "tag" to Tag::class,
                        "uInt32" to UInt32::class
                    )
                )
            }
            f.repeated -> {
                CodeBlockComponents(
                    "(%sizeof:M(%tag:T(${f.number})) * $name.size) + " +
                        "$name.sumOf·{ %sizeof:M(%boxedAccess:L) }",
                    mapOf(
                        "sizeof" to runtimeFunction("sizeof"),
                        "tag" to Tag::class,
                        "boxedAccess" to f.box(interceptValueAccess(f, ctx, "it"))
                    )
                )
            }
            else -> {
                CodeBlockComponents(
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

    private fun sizeOfMap(f: StandardField, name: String): CodeBlockComponents {
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

    private fun oneofSize(f: Oneof, type: String) =
        f.fields.map {
            ConditionalParams(
                CodeBlock.of("%L.%L", oneOfScope(f, type), f.fieldTypeNames.getValue(it.fieldName)),
                sizeOfString(
                    it,
                    interceptSizeof(
                        it,
                        "${f.fieldName}.${it.fieldName}",
                        ctx
                    )
                ).toCodeBlock()
            )
        }

    companion object {
        fun annotateMessageSizeOld(msg: Message, ctx: Context) =
            MessageSizeAnnotator(msg, ctx).annotateMessageSizeOld()

        fun annotateMessageSizeNew(msg: Message, ctx: Context) =
            MessageSizeAnnotator(msg, ctx).annotateMessageSizeNew()
    }
}
