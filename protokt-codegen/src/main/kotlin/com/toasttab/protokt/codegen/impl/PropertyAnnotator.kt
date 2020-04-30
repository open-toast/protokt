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
import com.toasttab.protokt.codegen.Oneof
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
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo
import com.toasttab.protokt.codegen.template.Oneof as OneofTemplate
import com.toasttab.protokt.codegen.template.Renderers.DefaultValue
import com.toasttab.protokt.codegen.template.Renderers.Standard
import com.toasttab.protokt.codegen.template.Renderers.Type
import com.toasttab.protokt.rt.PType

internal class PropertyAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateProperties(): List<PropertyInfo> {
        return msg.fields.map {
            val documentation = annotateFieldDocumentation(it, ctx)
            val nullable = it.nullable

            when (it) {
                is StandardField -> {
                    PropertyInfo(
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
                is Oneof ->
                    PropertyInfo(
                        name = it.fieldName,
                        type =
                            Type.render(
                                oneof = true,
                                nullable = nullable,
                                any = it.nativeTypeName
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

    private fun annotateStandard(f: StandardField) =
        Standard.render(
            field = f,
            nullable = f.nullable,
            any =
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
                        field = this,
                        type = type,
                        name =
                            if (type == PType.ENUM) {
                                unqualifiedNestedTypeName(ctx)
                            } else {
                                ""
                            }
                    ),
                    ctx
                )
            is Oneof ->
                OneofTemplate.DefaultValue.render()
        }

    companion object {
        fun annotateProperties(msg: MessageType, ctx: Context) =
            PropertyAnnotator(msg, ctx).annotateProperties()
    }
}
