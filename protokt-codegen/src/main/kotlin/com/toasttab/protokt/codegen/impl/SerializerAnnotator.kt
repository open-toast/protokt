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
import com.toasttab.protokt.codegen.template.ConditionalParams
import com.toasttab.protokt.codegen.template.IterationVar
import com.toasttab.protokt.codegen.template.RenderVariable.Box
import com.toasttab.protokt.codegen.template.RenderVariable.Field
import com.toasttab.protokt.codegen.template.RenderVariable.Name
import com.toasttab.protokt.codegen.template.RenderVariable.Options
import com.toasttab.protokt.codegen.template.RenderVariable.ScopedValue
import com.toasttab.protokt.codegen.template.RenderVariable.Tag
import com.toasttab.protokt.codegen.template.ScopedValueParams
import com.toasttab.protokt.codegen.template.Serialize

internal class SerializerAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateSerializer(): List<SerializerParams> {
        return msg.sortedFields().map {
            when (it) {
                is StandardField ->
                    SerializerParams(
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
                    SerializerParams(
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

    data class SerializerParams(
        val std: Boolean,
        val fieldName: String,
        val skipDefaultValue: Boolean,
        /** A singleton list for standard fields; one per type for enum fields */
        val conditionals: List<ConditionalParams>
    )

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
                        ConcatWithScope.render(
                            ScopedValue to
                                ScopedValueParams(it, f.fieldName)
                        )
                    )
                }
            )
        return Serialize.render(
            Field to f,
            Name to f.fieldName,
            Tag to f.tag,
            Box to
                if (f.map) {
                    f.boxMap(ctx)
                } else {
                    f.box(fieldAccess)
                },
            Options to
                SerializerOptions(
                    fieldAccess = fieldAccess
                )
        )
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
            ConcatWithScope.render(
                ScopedValue to
                    ScopedValueParams(
                        oneOfScope(f, type, ctx),
                        f.fieldTypeNames[ff.name] ?: ""
                    )
            ),
            serializeString(ff, Some(f.fieldName))
        )

    companion object {
        fun annotateSerializer(msg: MessageType, ctx: Context) =
            SerializerAnnotator(msg, ctx).annotateSerializer()
    }
}
