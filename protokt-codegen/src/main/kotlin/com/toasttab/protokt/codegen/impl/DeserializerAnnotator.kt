/*
 * Copyright (c) 2019. Toast Inc.
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
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.OneOf
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptDeserializedValue
import com.toasttab.protokt.codegen.impl.Wrapper.interceptReadFn
import com.toasttab.protokt.codegen.snakeToCamel
import com.toasttab.protokt.rt.PType

internal class DeserializerAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateDeserializer(): List<DeserializerSt> =
        msg.flattenedSortedFields().map { (field, oneOf) ->
            DeserializerSt(
                oneOf.isEmpty(),
                field.repeated,
                field.tagList.joinToString(),
                oneOf.fold(
                    {
                        AssignmentSt(
                            field.fieldName,
                            deserializeString(field)
                        )
                    },
                    {
                        AssignmentSt(
                            it.fieldName,
                            oneOfDes(it, field)
                        )
                    }
                )
            )
        }

    private fun deserializeString(f: StandardField) =
        interceptDeserializedValue(
            f,
            DeserializeRF.render(
                FieldRenderVar to f,
                TypeRenderVar to f.unqualifiedNestedTypeName(ctx),
                ReadRenderVar to interceptReadFn(f, f.readFn()),
                LhsRenderVar to f.fieldName
            ),
            ctx
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
        OneOfDeserializeRF.render(
            OneOfRenderVar to snakeToCamel(f.name).capitalize(),
            NameRenderVar to snakeToCamel(ff.name).capitalize(),
            ReadRenderVar to deserializeString(ff)
        )

    private fun StandardField.readFn() =
        ReadFunctionRF.render(
            TypeRenderVar to type,
            BuilderRenderVar to
                when (type) {
                    PType.ENUM,
                    PType.MESSAGE -> builder()
                    else -> ""
                }
        )

    private fun StandardField.builder(): String {
        val n = stripQualification(ctx, this)
        return BuilderRF.render(
            ShouldRenderNameRenderVar to (msg.name != n),
            NameRenderVar to n
        )
    }

    private fun stripQualification(ctx: Context, f: StandardField) =
        stripEnclosingMessageName(f.typePClass().unqualify(ctx.pkg), ctx)

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
