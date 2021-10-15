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

package com.toasttab.protokt.codegen.impl

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.impl.Nullability.hasNonNullOption
import com.toasttab.protokt.codegen.impl.Wrapper.interceptFieldSizeof
import com.toasttab.protokt.codegen.impl.Wrapper.interceptSizeof
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.impl.Wrapper.mapKeyConverter
import com.toasttab.protokt.codegen.impl.Wrapper.mapValueConverter
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.template.ConditionalParams
import com.toasttab.protokt.codegen.template.Message.Message.SizeofInfo
import com.toasttab.protokt.codegen.template.Renderers.ConcatWithScope
import com.toasttab.protokt.codegen.template.Renderers.IterationVar
import com.toasttab.protokt.codegen.template.Renderers.Sizeof
import com.toasttab.protokt.codegen.template.Renderers.Sizeof.Options

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
                    is StandardField ->
                        if (!it.hasNonNullOption) {
                            """
                                |if ${it.nonDefault(ctx)} {
                                |    $resultVarName += ${sizeOfString(it)}
                                |}
                            """.trimMargin()
                        } else {
                            "$resultVarName += ${sizeOfString(it)}"
                        }.replace(" ", "·")
                    is Oneof ->
                        if (it.hasNonNullOption) {
                            // TODO: verify indentation is correct for this case
                            "$resultVarName +=\n"
                        } else {
                            ""
                        } +
                            """
                            |when (${it.fieldName}) {
                            |${conditionals(it)}
                            |}
                        """.trimMargin()
                }
            }

        return FunSpec.builder("messageSize")
            .addModifiers(KModifier.PRIVATE)
            .returns(Int::class)
            .addCode(
                if (fieldSizes.isEmpty()) {
                    "return unknownFields.size()"
                } else {
                    """
                        |var $resultVarName = 0
                        |${fieldSizes.joinToString("\n")}
                        |$resultVarName += unknownFields.size()
                        |return $resultVarName
                    """.trimMargin()
                }
            )
            .build()
    }

    private fun conditionals(f: Oneof) =
        f.fields
            .sortedBy { it.number }.joinToString("\n") {
                """
                    |    is ${condition(f, it, msg.name)} ->
                    |        ${oneofSizeOfString(f, it)}
                """.trimMargin()
            }

    private fun condition(f: Oneof, ff: StandardField, type: String) =
        ConcatWithScope.render(
            scope = oneOfScope(f, type, ctx),
            value = f.fieldTypeNames.getValue(ff.name)
        )

    private fun oneofSizeOfString(o: Oneof, f: StandardField) =
        if (!o.hasNonNullOption) {
            "$resultVarName += "
        } else {
            ""
        } +
            sizeOfString(
                f,
                interceptSizeof(
                    f,
                    ConcatWithScope.render(
                        scope = o.fieldName,
                        value = f.fieldName
                    ),
                    ctx
                )
            )

    private fun annotateMessageSizeOld(): List<SizeofInfo> {
        return msg.fields.map {
            when (it) {
                is StandardField ->
                    SizeofInfo(
                        true,
                        it.fieldName,
                        !it.hasNonNullOption,
                        listOf(
                            ConditionalParams(
                                it.nonDefault(ctx),
                                sizeOfString(it)
                            )
                        )
                    )
                is Oneof ->
                    SizeofInfo(
                        false,
                        it.fieldName,
                        !it.hasNonNullOption,
                        oneofSize(it, msg.name)
                    )
            }
        }
    }

    private fun sizeOfString(
        f: StandardField,
        oneOfFieldAccess: String? = null
    ): String {
        val name =
            oneOfFieldAccess
                ?: if (f.repeated) {
                    f.fieldName
                } else {
                    interceptSizeof(f, f.fieldName, ctx)
                }
        return Sizeof.render(
            name = name,
            field = f,
            type = f.unqualifiedNestedTypeName(ctx),
            options = Options(
                fieldSizeof = interceptFieldSizeof(f, name, ctx),
                fieldAccess = interceptValueAccess(f, ctx, IterationVar.render()),
                keyAccess = mapKeyConverter(f, ctx),
                valueAccess = mapValueConverter(f, ctx),
                valueType = f.mapEntry?.value?.type
            )
        )
    }

    private fun oneofSize(f: Oneof, type: String) =
        f.fields.map {
            ConditionalParams(
                ConcatWithScope.render(
                    scope = oneOfScope(f, type, ctx),
                    value = f.fieldTypeNames.getValue(it.name)
                ),
                sizeOfString(
                    it,
                    interceptSizeof(
                        it,
                        ConcatWithScope.render(
                            scope = f.fieldName,
                            value = it.fieldName
                        ),
                        ctx
                    )
                )
            )
        }

    companion object {
        fun annotateMessageSizeOld(msg: Message, ctx: Context) =
            MessageSizeAnnotator(msg, ctx).annotateMessageSizeOld()

        fun annotateMessageSizeNew(msg: Message, ctx: Context) =
            MessageSizeAnnotator(msg, ctx).annotateMessageSizeNew()
    }
}
