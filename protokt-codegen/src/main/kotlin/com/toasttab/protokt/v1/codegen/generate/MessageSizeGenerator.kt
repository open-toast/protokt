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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.v1.codegen.generate.Nullability.hasNonNullOption
import com.toasttab.protokt.v1.codegen.generate.Wrapper.interceptFieldSizeof
import com.toasttab.protokt.v1.codegen.generate.Wrapper.interceptSizeof
import com.toasttab.protokt.v1.codegen.generate.Wrapper.interceptValueAccess
import com.toasttab.protokt.v1.codegen.generate.Wrapper.mapKeyConverter
import com.toasttab.protokt.v1.codegen.generate.Wrapper.mapValueConverter
import com.toasttab.protokt.v1.codegen.util.FieldType
import com.toasttab.protokt.v1.codegen.util.Message
import com.toasttab.protokt.v1.codegen.util.Oneof
import com.toasttab.protokt.v1.codegen.util.StandardField

fun generateMessageSize(msg: Message, ctx: Context) =
    MessageSizeGenerator(msg, ctx).generate()

private class MessageSizeGenerator(
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

    fun generate(): FunSpec {
        val fieldSizes =
            msg.mapFields(
                ctx,
                false,
                { CodeBlock.of("$resultVarName·+=·%L", sizeOf(it, ctx)) },
                { oneof, std -> sizeofOneof(oneof, std) },
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

    private fun sizeofOneof(o: Oneof, f: StandardField) =
        sizeOf(
            f,
            ctx,
            interceptSizeof(
                f,
                CodeBlock.of("%N.%N", o.fieldName, f.fieldName),
                ctx
            )
        ).let { s ->
            if (o.hasNonNullOption) {
                s
            } else {
                CodeBlock.of("$resultVarName·+=·%L", s)
            }
        }
}

fun sizeOf(
    f: StandardField,
    ctx: Context,
    oneOfFieldAccess: CodeBlock? = null
): CodeBlock {
    val name =
        oneOfFieldAccess
            ?: if (f.repeated) {
                CodeBlock.of("%N", f.fieldName)
            } else {
                interceptSizeof(f, CodeBlock.of("%N", f.fieldName), ctx)
            }

    return when {
        f.map -> sizeOfMap(f, name, ctx)
        f.repeated && f.packed -> {
            namedCodeBlock(
                "sizeOf(${f.tag}u) + " +
                    "%elementsSize:L.let·{·it·+·%sizeOf:M(it.toUInt())·}",
                mapOf(
                    "sizeOf" to runtimeFunction("sizeOf"),
                    "elementsSize" to f.elementsSize()
                )
            )
        }
        f.repeated -> {
            namedCodeBlock(
                "(%sizeOf:M(${f.tag}u) * %name:L.size) + %elementsSize:L",
                mapOf(
                    "sizeOf" to runtimeFunction("sizeOf"),
                    "name" to name,
                    "elementsSize" to
                        f.elementsSize(
                            interceptValueAccess(f, ctx, CodeBlock.of("it")),
                            parenthesize = false
                        )
                )
            )
        }
        else -> {
            buildCodeBlock {
                add(
                    "%M(${f.tag}u) + %L",
                    runtimeFunction("sizeOf"),
                    interceptFieldSizeof(f, name, ctx)
                )
            }
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

    val mapEntry = f.mapEntry!!
    val sizeOfCall = sizeOfCall(mapEntry, key, value)

    return buildCodeBlock {
        add(
            "%M($name, ${f.tag}u)·{·%L,·%L·->\n",
            runtimeFunction("sizeOfMap"),
            mapEntry.key.loopVar("k"),
            mapEntry.value.loopVar("v")
        )
        indent()
        add("%T.%L\n", f.className, sizeOfCall)
        endControlFlowWithoutNewline()
    }
}

private fun StandardField.loopVar(name: String) =
    if (type.sizeFn is FieldType.Method) {
        name
    } else {
        "_"
    }

fun StandardField.sizeOf(value: CodeBlock): CodeBlock =
    when (val fn = type.sizeFn) {
        is FieldType.Const -> CodeBlock.of(fn.size.toString())
        is FieldType.Method -> CodeBlock.of("%M(%L)", runtimeFunction(fn.name), value)
    }

fun StandardField.elementsSize(
    fieldAccess: CodeBlock = CodeBlock.of("it"),
    parenthesize: Boolean = true
) =
    when (val sizeFn = type.sizeFn) {
        is FieldType.Const ->
            CodeBlock.of("(%N.size * %L)", fieldName, sizeFn.size)
                .let { if (parenthesize) CodeBlock.of("(%L)", it) else it }
        is FieldType.Method ->
            CodeBlock.of("%N.sumOf·{·%L·}", fieldName, sizeOf(fieldAccess))
    }
