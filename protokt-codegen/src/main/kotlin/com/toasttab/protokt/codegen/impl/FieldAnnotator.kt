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
import arrow.core.toOption
import com.toasttab.protokt.codegen.Field
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.OneOf
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.FieldDocumentationAnnotator.Companion.annotateFieldDocumentation
import com.toasttab.protokt.codegen.impl.Implements.overrides
import com.toasttab.protokt.codegen.impl.NonNullable.hasNonNullOption
import com.toasttab.protokt.codegen.impl.NonNullable.nullable
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptDefaultValue
import com.toasttab.protokt.codegen.impl.Wrapper.interceptTypeName
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.template.DefaultValue
import com.toasttab.protokt.codegen.template.OneOfDefaultValue
import com.toasttab.protokt.codegen.template.RenderVariable.Any
import com.toasttab.protokt.codegen.template.RenderVariable.Field as FieldVar
import com.toasttab.protokt.codegen.template.RenderVariable.Name
import com.toasttab.protokt.codegen.template.RenderVariable.Nullable
import com.toasttab.protokt.codegen.template.RenderVariable.Oneof
import com.toasttab.protokt.codegen.template.RenderVariable.Type as TypeVar
import com.toasttab.protokt.codegen.template.Standard
import com.toasttab.protokt.codegen.template.Type
import com.toasttab.protokt.rt.PType

internal class FieldAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateFields(): List<ParameterParams> {
        return msg.fields.map {
            val documentation = annotateFieldDocumentation(it, ctx)
            val nullable = it.nullable

            when (it) {
                is StandardField -> {
                    ParameterParams(
                        name = it.fieldName,
                        type = annotateStandard(it),
                        defaultValue = it.defaultValue(),
                        messageType = it.type.toString(),
                        repeated = it.repeated,
                        map = it.map,
                        nullable = nullable,
                        overrides = it.overrides(ctx, msg),
                        wrapped = it.wrapped,
                        nonNullOption = it.hasNonNullOption,
                        documentation = documentation,
                        deprecation =
                            if (it.options.default.deprecated) {
                                renderOptions(
                                    it.options.protokt.deprecationMessage
                                )
                            } else {
                                null
                            }
                    )
                }
                is OneOf ->
                    ParameterParams(
                        name = it.fieldName,
                        type = Type.render(
                            Oneof to true,
                            Nullable to nullable,
                            Any to it.nativeTypeName
                        ),
                        defaultValue = it.defaultValue(),
                        oneOf = true,
                        nullable = nullable,
                        nonNullOption = it.hasNonNullOption,
                        documentation = documentation
                    )
            }
        }
    }

    data class ParameterParams(
        val name: String,
        val type: String,
        val defaultValue: String,
        val messageType: String = "",
        val repeated: Boolean = false,
        val map: Boolean = false,
        val oneOf: Boolean = false,
        val nullable: Boolean = true,
        val wrapped: Boolean = false,
        val nonNullOption: Boolean,
        val overrides: Boolean = false,
        val documentation: List<String>,
        val deprecation: Deprecation.RenderOptions? = null
    )

    private fun annotateStandard(f: StandardField) =
        Standard.render(
            FieldVar to f,
            Nullable to f.nullable,
            Any to
                if (f.map) {
                    typeParams(f.typeName)
                } else {
                    interceptTypeName(
                        f,
                        f.typePClass(ctx).renderName(ctx.pkg),
                        ctx
                    )
                }
        )

    private fun typeParams(n: String) =
        findType(n, msg)
            .map { resolveMapEntry(it, ctx) }
            .getOrElse { error("missing type params") }

    private fun findType(
        tn: String,
        msg: MessageType
    ): Option<MessageType> {
        val n = tn.split(".").let { if (it.isEmpty()) tn else it.last() }
        return msg.nestedTypes.find {
            when (it) {
                is MessageType ->
                    if (it.name == n) Some(it) else findType(n, it)
                else -> None
            }.isDefined()
        }.toOption().map { f -> f as MessageType }
    }

    private fun Field.defaultValue() =
        when (this) {
            is StandardField ->
                interceptDefaultValue(
                    this,
                    DefaultValue.render(
                        FieldVar to this,
                        TypeVar to type,
                        Name to
                            if (type == PType.ENUM) {
                                unqualifiedNestedTypeName(ctx)
                            } else {
                                ""
                            }
                    ),
                    ctx
                )
            is OneOf ->
                OneOfDefaultValue.render()
        }

    companion object {
        fun annotateFields(msg: MessageType, ctx: Context) =
            FieldAnnotator(msg, ctx).annotateFields()
    }
}
