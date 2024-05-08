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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Nullability.hasNonNullOption
import protokt.v1.codegen.generate.Wrapper.interceptFieldSizeof
import protokt.v1.codegen.generate.Wrapper.interceptSizeof
import protokt.v1.codegen.generate.Wrapper.interceptValueAccess
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.Oneof
import protokt.v1.codegen.util.SizeFn
import protokt.v1.codegen.util.StandardField
import protokt.v1.codegen.util.sizeFn

internal const val MESSAGE_SIZE = "`\$messageSize`"

internal fun generateMessageSize(msg: Message, properties: List<PropertySpec>, ctx: Context) =
    MessageSizeGenerator(msg, properties, ctx).generate()

private class MessageSizeGenerator(
    private val msg: Message,
    private val properties: List<PropertySpec>,
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

    fun generate(): PropertySpec {
        val fieldSizes =
            msg.mapFields(
                ctx,
                properties,
                false,
                { std, _ -> CodeBlock.of("$resultVarName·+=·%L", sizeOf(std, ctx)) },
                { oneof, std, _ -> sizeofOneof(oneof, std) },
                {
                    if (it.hasNonNullOption) {
                        add("$resultVarName·+=·")
                    }
                }
            )

        return PropertySpec.builder(MESSAGE_SIZE, Int::class)
            .addModifiers(KModifier.PRIVATE)
            .delegate(
                buildCodeBlock {
                    beginControlFlow("lazy")
                    add(
                        if (fieldSizes.isEmpty()) {
                            CodeBlock.of("unknownFields.size()")
                        } else {
                            buildCodeBlock {
                                addStatement("var·$resultVarName·=·0")
                                fieldSizes.forEach { fs -> add(fs) }
                                addStatement("$resultVarName·+=·unknownFields.size()")
                                addStatement(resultVarName)
                            }
                        }
                    )
                    endControlFlow()
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

internal fun sizeOf(
    f: StandardField,
    ctx: Context,
    oneOfFieldAccess: CodeBlock? = null
): CodeBlock {
    val fieldAccess =
        oneOfFieldAccess
            ?: if (f.repeated) {
                CodeBlock.of("%N", f.fieldName)
            } else {
                interceptSizeof(f, CodeBlock.of("%N", f.fieldName), ctx)
            }

    return when {
        f.isMap -> sizeOfMap(f, fieldAccess)
        f.repeated && f.packed -> {
            namedCodeBlock(
                "sizeOf(${f.tag}u) + " +
                    "%elementsSize:L.let·{·it·+·%sizeOf:M(it.toUInt())·}",
                mapOf(
                    "sizeOf" to sizeOf,
                    "elementsSize" to f.elementsSize()
                )
            )
        }
        f.repeated -> {
            namedCodeBlock(
                "(%sizeOf:M(${f.tag}u) * %name:L.size) + %elementsSize:L",
                mapOf(
                    "sizeOf" to sizeOf,
                    "name" to fieldAccess,
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
                    sizeOf,
                    interceptFieldSizeof(f, f.sizeOf(fieldAccess), fieldAccess, ctx)
                )
            }
        }
    }
}

private fun sizeOfMap(f: StandardField, name: CodeBlock): CodeBlock {
    val sizeOfCall = sizeOfCall(f.mapKey, f.mapValue, CodeBlock.of("k"), CodeBlock.of("v"))

    return buildCodeBlock {
        add(
            "%M($name, ${f.tag}u)·{·%L,·%L·->\n",
            sizeOf,
            f.mapKey.loopVar("k"),
            f.mapValue.loopVar("v")
        )
        indent()
        add("%T.%L\n", f.className, sizeOfCall)
        endControlFlowWithoutNewline()
    }
}

private fun StandardField.loopVar(name: String) =
    if (type.sizeFn is SizeFn.Method) {
        name
    } else {
        "_"
    }

private fun StandardField.sizeOf(value: CodeBlock): CodeBlock =
    when (val fn = type.sizeFn) {
        is SizeFn.Const -> CodeBlock.of(fn.size.toString())
        is SizeFn.Method -> CodeBlock.of("%M(%L)", fn.method, value)
    }

internal fun StandardField.elementsSize(
    fieldAccess: CodeBlock = CodeBlock.of("it"),
    parenthesize: Boolean = true
) =
    when (val sizeFn = type.sizeFn) {
        is SizeFn.Const ->
            CodeBlock.of("(%N.size * %L)", fieldName, sizeFn.size)
                .let { if (parenthesize) CodeBlock.of("(%L)", it) else it }
        is SizeFn.Method ->
            CodeBlock.of("%N.sumOf·{·%L·}", fieldName, sizeOf(fieldAccess))
    }
