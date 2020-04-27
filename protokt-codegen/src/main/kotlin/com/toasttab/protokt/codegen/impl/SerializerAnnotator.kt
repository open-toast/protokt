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
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.OneOf
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.NonNullable.hasNonNullOption
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.template.ConcatWithScope
import com.toasttab.protokt.codegen.template.ConcatWithScope.Params
import com.toasttab.protokt.codegen.template.ConditionalParams
import com.toasttab.protokt.codegen.template.IterationVar
import com.toasttab.protokt.codegen.template.Message.SerializerInfo
import com.toasttab.protokt.codegen.template.Serialize
import com.toasttab.protokt.codegen.template.Serialize.Options
import com.toasttab.protokt.codegen.template.render

internal class SerializerAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateSerializer(): List<SerializerInfo> {
        return msg.sortedFields().map {
            when (it) {
                is StandardField ->
                    SerializerInfo(
                        true,
                        it.fieldName,
                        !it.hasNonNullOption,
                        listOf(
                            ConditionalParams(
                                it.nonDefault(ctx),
                                serializeString(it)
                            )
                        )
                    )
                is OneOf ->
                    SerializerInfo(
                        false,
                        it.fieldName,
                        !it.hasNonNullOption,
                        it.fields
                            .sortedBy { f -> f.number }
                            .map { f -> oneOfSer(it, f, msg.type) }
                    )
            }
        }
    }

    private fun serializeString(
        f: StandardField,
        t: Option<String> = None
    ): String {
        val fieldAccess =
            t.fold(
                {
                    interceptValueAccess(
                        f,
                        ctx,
                        if (f.repeated) {
                            IterationVar.render()
                        } else {
                            f.fieldName
                        }
                    )
                },
                {
                    interceptValueAccess(
                        f,
                        ctx,
                        ConcatWithScope.prepare(
                            scopedValue = Params(it, f.fieldName)
                        ).render()
                    )
                }
            )

        return Serialize.prepare(
            field = f,
            name = f.fieldName,
            tag = f.tag,
            box =
                if (f.map) {
                    f.boxMap(ctx)
                } else {
                    f.box(fieldAccess)
                },
            options =
                Options(
                    fieldAccess = fieldAccess
                )
        ).render()
    }

    private data class SerializerOptions(
        val fieldAccess: String
    )

    private fun MessageType.sortedFields() =
        fields.sortedBy {
            when (it) {
                is StandardField -> it
                is OneOf -> it.fields.first()
            }.number
        }

    private fun oneOfSer(f: OneOf, ff: StandardField, type: String) =
        ConditionalParams(
            ConcatWithScope.prepare(
                scopedValue =
                    Params(
                        oneOfScope(f, type, ctx),
                        f.fieldTypeNames[ff.name] ?: ""
                    )
            ).render(),
            serializeString(ff, Some(f.fieldName))
        )

    companion object {
        fun annotateSerializer(msg: MessageType, ctx: Context) =
            SerializerAnnotator(msg, ctx).annotateSerializer()
    }
}
