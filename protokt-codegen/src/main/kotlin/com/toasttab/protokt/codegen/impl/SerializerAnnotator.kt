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
import com.toasttab.protokt.codegen.impl.Nullability.hasNonNullOption
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.template.ConditionalParams
import com.toasttab.protokt.codegen.template.Message.Message.SerializerInfo
import com.toasttab.protokt.codegen.template.Renderers.ConcatWithScope
import com.toasttab.protokt.codegen.template.Renderers.IterationVar
import com.toasttab.protokt.codegen.template.Renderers.Serialize
import com.toasttab.protokt.codegen.template.Renderers.Serialize.Options

internal class SerializerAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateSerializer(): List<SerializerInfo> {
        return msg.fields.map {
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
                is Oneof ->
                    SerializerInfo(
                        false,
                        it.fieldName,
                        !it.hasNonNullOption,
                        it.fields
                            .sortedBy { f -> f.number }
                            .map { f -> oneOfSer(it, f, msg.name) }
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
                        ConcatWithScope.render(
                            scope = it,
                            value = f.fieldName
                        )
                    )
                }
            )

        return Serialize.render(
            field = f,
            name = f.fieldName,
            tag = f.tag.value,
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
        )
    }

    private fun oneOfSer(f: Oneof, ff: StandardField, type: String) =
        ConditionalParams(
            ConcatWithScope.render(
                scope = oneOfScope(f, type, ctx),
                value = f.fieldTypeNames.getValue(ff.name)
            ),
            serializeString(ff, Some(f.fieldName))
        )

    companion object {
        fun annotateSerializer(msg: Message, ctx: Context) =
            SerializerAnnotator(msg, ctx).annotateSerializer()
    }
}
