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
import com.toasttab.protokt.codegen.impl.Wrapper.interceptFieldSizeof
import com.toasttab.protokt.codegen.impl.Wrapper.interceptSizeof
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.template.ConcatWithScope
import com.toasttab.protokt.codegen.template.ConditionalParams
import com.toasttab.protokt.codegen.template.IterationVar
import com.toasttab.protokt.codegen.template.RenderVariable.Field
import com.toasttab.protokt.codegen.template.RenderVariable.Name
import com.toasttab.protokt.codegen.template.RenderVariable.Options
import com.toasttab.protokt.codegen.template.RenderVariable.ScopedValue
import com.toasttab.protokt.codegen.template.RenderVariable.Type
import com.toasttab.protokt.codegen.template.ScopedValueParams
import com.toasttab.protokt.codegen.template.Sizeof

internal class SizeOfAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateSizeof(): List<SizeofParams> {
        return msg.fields.map {
            when (it) {
                is StandardField ->
                    SizeofParams(
                        true,
                        "",
                        !it.hasNonNullOption,
                        listOf(
                            ConditionalParams(
                                it.nonDefault(ctx),
                                sizeOfString(it, None)
                            )
                        )
                    )
                is OneOf ->
                    SizeofParams(
                        false,
                        it.fieldName,
                        !it.hasNonNullOption,
                        oneOfSize(it, msg.type)
                    )
            }
        }
    }

    data class SizeofParams(
        val std: Boolean,
        val fieldName: String,
        val skipDefaultValue: Boolean,
        /** A singleton list for standard fields; one per type for enum fields */
        val conditionals: List<ConditionalParams>
    )

    private fun sizeOfString(
        f: StandardField,
        oneOfFieldAccess: Option<String>
    ): String {
        val name =
            oneOfFieldAccess.fold(
                {
                    if (f.repeated) {
                        f.fieldName
                    } else {
                        interceptSizeof(f, f.fieldName, ctx)
                    }
                },
                { it }
            )
        return Sizeof.render(
            Name to name,
            Field to f,
            Type to f.unqualifiedNestedTypeName(ctx),
            Options to
                SizeOfOptions(
                    fieldSizeof = interceptFieldSizeof(f, name, ctx),
                    fieldAccess =
                        interceptValueAccess(f, ctx, IterationVar.render())
                )
        )
    }

    private data class SizeOfOptions(
        val fieldSizeof: String,
        val fieldAccess: Any
    )

    private fun oneOfSize(f: OneOf, type: String) =
        f.fields.map {
            ConditionalParams(
                ConcatWithScope.render(
                    ScopedValue to
                        ScopedValueParams(
                            oneOfScope(f, type, ctx),
                            f.fieldTypeNames[it.name] ?: ""
                        )
                ),
                sizeOfString(
                    it,
                    Some(
                        interceptSizeof(
                            it,
                            ConcatWithScope.render(
                                ScopedValue to
                                    ScopedValueParams(
                                        f.fieldName,
                                        it.fieldName
                                    )
                            ),
                            ctx
                        )
                    )
                )
            )
        }

    companion object {
        fun annotateSizeof(msg: MessageType, ctx: Context) =
            SizeOfAnnotator(msg, ctx).annotateSizeof()
    }
}
