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

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.OneOf
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.MessageAnnotator.idealMaxWidth
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptReadFn
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.impl.Wrapper.wrapperName
import com.toasttab.protokt.codegen.snakeToCamel
import com.toasttab.protokt.codegen.template.Deserialize
import com.toasttab.protokt.codegen.template.OneOfDeserialize
import com.toasttab.protokt.codegen.template.ReadFunction
import com.toasttab.protokt.codegen.template.RenderVariable.Builder
import com.toasttab.protokt.codegen.template.RenderVariable.Field
import com.toasttab.protokt.codegen.template.RenderVariable.Lhs
import com.toasttab.protokt.codegen.template.RenderVariable.Name
import com.toasttab.protokt.codegen.template.RenderVariable.Oneof
import com.toasttab.protokt.codegen.template.RenderVariable.Options
import com.toasttab.protokt.codegen.template.RenderVariable.Read
import com.toasttab.protokt.codegen.template.RenderVariable.Type
import com.toasttab.protokt.rt.PType

internal class DeserializerAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateDeserializer(): List<DeserializerParams> =
        msg.flattenedSortedFields().map { (field, oneOf) ->
            DeserializerParams(
                oneOf.isEmpty(),
                field.repeated,
                field.tagList.joinToString(),
                oneOf.fold(
                    {
                        deserializeString(field).let { value ->
                            Assignment(
                                field.fieldName,
                                value,
                                long(field, value)
                            )
                        }
                    },
                    {
                        oneOfDes(it, field).let { value ->
                            Assignment(
                                it.fieldName,
                                value,
                                long(field, value)
                            )
                        }
                    }
                )
            )
        }

    data class DeserializerParams(
        val std: Boolean,
        val repeated: Boolean,
        val tag: String,
        val assignment: Assignment
    )

    data class Assignment(
        val fieldName: String,
        val value: String,
        val long: Boolean
    )

    private fun long(field: StandardField, value: String): Boolean {
        val spaceTaken =
            (ctx.enclosingMessage.size * 4) + // outer indentation
                4 + // companion object
                4 + // fun deserialize
                4 + // while (true)
                4 + // when (...)
                field.tag.toString().length +
                4 + // ` -> `
                field.fieldName.length +
                3 // ` = `

        val spaceLeft = idealMaxWidth - spaceTaken

        return value.length > spaceLeft
    }

    private fun deserializeString(f: StandardField) =
        Deserialize.render(
            Field to f,
            Type to f.unqualifiedNestedTypeName(ctx),
            Read to interceptReadFn(f, f.readFn()),
            Lhs to f.fieldName,
            Options to
                if (f.wrapped) {
                    DeserializerOptions(
                        wrapName = wrapperName(f, ctx).getOrElse { "" },
                        type = f.type.toString(),
                        oneof = true
                    )
                } else {
                    emptyList<Any>()
                }
        )

    private data class DeserializerOptions(
        val wrapName: String,
        val type: String,
        val oneof: Boolean
    )

    private fun MessageType.flattenedSortedFields() =
        fields.flatMap {
            when (it) {
                is StandardField ->
                    listOf(FlattenedField(it, None))
                is OneOf ->
                    it.fields.map { f -> FlattenedField(f, Some(it)) }
            }
        }.sortedBy { it.field.number }

    private data class FlattenedField(
        val field: StandardField,
        val oneOf: Option<OneOf>
    )

    private fun oneOfDes(f: OneOf, ff: StandardField) =
        OneOfDeserialize.render(
            Oneof to snakeToCamel(f.name).capitalize(),
            Name to snakeToCamel(ff.name).capitalize(),
            Read to deserializeString(ff)
        )

    private fun StandardField.readFn() =
        ReadFunction.render(
            Type to type,
            Builder to
                when (type) {
                    PType.ENUM,
                    PType.MESSAGE -> stripQualification(ctx, this)
                    else -> ""
                }
        )

    private fun stripQualification(ctx: Context, f: StandardField) =
        stripEnclosingMessageName(f.typePClass(ctx).renderName(ctx.pkg), ctx)

    private fun stripEnclosingMessageName(s: String, ctx: Context): String {
        var stripped = s
        for (enclosing in ctx.enclosingMessage.reversed()) {
            if (stripped.startsWith(enclosing.name)) {
                stripped = stripped.removePrefix("${enclosing.name}.")
            } else {
                break
            }
        }
        return stripped
    }

    companion object {
        fun annotateDeserializer(msg: MessageType, ctx: Context) =
            DeserializerAnnotator(msg, ctx).annotateDeserializer()
    }
}
