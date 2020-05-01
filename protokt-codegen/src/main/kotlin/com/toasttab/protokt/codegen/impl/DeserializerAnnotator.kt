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
import com.toasttab.protokt.codegen.Oneof
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.MessageAnnotator.idealMaxWidth
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptReadFn
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.impl.Wrapper.wrapperName
import com.toasttab.protokt.codegen.template.Message.Message.DeserializerInfo
import com.toasttab.protokt.codegen.template.Message.Message.DeserializerInfo.Assignment
import com.toasttab.protokt.codegen.template.Oneof as OneofTemplate
import com.toasttab.protokt.codegen.template.Renderers.Deserialize
import com.toasttab.protokt.codegen.template.Renderers.Deserialize.Options
import com.toasttab.protokt.codegen.template.Renderers.Read
import com.toasttab.protokt.rt.PType

internal class DeserializerAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateDeserializer(): List<DeserializerInfo> =
        msg.flattenedSortedFields().map { (field, oneOf) ->
            DeserializerInfo(
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
            field = f,
            type = f.unqualifiedNestedTypeName(ctx),
            read = interceptReadFn(f, f.readFn()),
            lhs = f.fieldName,
            options =
                if (f.wrapped) {
                    Options(
                        wrapName = wrapperName(f, ctx).getOrElse { "" },
                        type = f.type.toString(),
                        oneof = true
                    )
                } else {
                    null
                }
        )

    private fun MessageType.flattenedSortedFields() =
        fields.flatMap {
            when (it) {
                is StandardField ->
                    listOf(FlattenedField(it, None))
                is Oneof ->
                    it.fields.map { f -> FlattenedField(f, Some(it)) }
            }
        }.sortedBy { it.field.number }

    private data class FlattenedField(
        val field: StandardField,
        val oneof: Option<Oneof>
    )

    private fun oneOfDes(f: Oneof, ff: StandardField) =
        OneofTemplate.Deserialize.render(
            oneof = f.name,
            name = ff.name.capitalize(),
            read = deserializeString(ff)
        )

    private fun StandardField.readFn() =
        Read.render(
            type = type,
            builder =
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
