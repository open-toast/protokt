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
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.Implements.overrides
import com.toasttab.protokt.codegen.impl.Nullability.deserializeType
import com.toasttab.protokt.codegen.impl.Nullability.dslPropertyType
import com.toasttab.protokt.codegen.impl.Nullability.hasNonNullOption
import com.toasttab.protokt.codegen.impl.Nullability.nullable
import com.toasttab.protokt.codegen.impl.Nullability.propertyType
import com.toasttab.protokt.codegen.impl.Nullability.renderNullableType
import com.toasttab.protokt.codegen.impl.PropertyDocumentationAnnotator.Companion.annotatePropertyDocumentation
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptDefaultValue
import com.toasttab.protokt.codegen.impl.Wrapper.interceptTypeName
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Field
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo
import com.toasttab.protokt.codegen.template.Oneof as OneofTemplate
import com.toasttab.protokt.codegen.template.Renderers.DefaultValue
import com.toasttab.protokt.codegen.template.Renderers.Standard

internal class PropertyAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateProperties(): List<PropertyInfo> {
        return msg.fields.map {
            val documentation = annotatePropertyDocumentation(it, ctx)

            when (it) {
                is StandardField -> {
                    annotateStandard(it).let { type ->
                        PropertyInfo(
                            name = it.fieldName,
                            propertyType = propertyType(it, type),
                            deserializeType = deserializeType(it, type),
                            dslPropertyType = dslPropertyType(it, type),
                            defaultValue = it.defaultValue(ctx),
                            fieldType = it.type.toString(),
                            repeated = it.repeated,
                            map = it.map,
                            nullable = it.nullable,
                            nonNullOption = it.hasNonNullOption,
                            overrides = it.overrides(ctx, msg),
                            wrapped = it.wrapped,
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
                }
                is Oneof ->
                    PropertyInfo(
                        name = it.fieldName,
                        propertyType = propertyType(it),
                        deserializeType = it.renderNullableType(),
                        dslPropertyType = it.renderNullableType(),
                        defaultValue = it.defaultValue(ctx),
                        oneOf = true,
                        nullable = it.nullable,
                        nonNullOption = it.hasNonNullOption,
                        documentation = documentation
                    )
            }
        }
    }

    private fun annotateStandard(f: StandardField) =
        Standard.render(
            field = f,
            any =
                if (f.map) {
                    typeParams(f.protoTypeName)
                } else {
                    interceptTypeName(
                        f,
                        f.typePClass.renderName(ctx.pkg),
                        ctx
                    )
                }
        )

    private fun typeParams(n: String) =
        findType(n, msg)
            .map { resolveMapEntryTypes(it, ctx) }
            .getOrElse { error("missing type params") }

    private fun findType(
        tn: String,
        msg: Message
    ): Option<Message> {
        val n = tn.split(".").let { if (it.isEmpty()) tn else it.last() }
        return msg.nestedTypes.find {
            when (it) {
                is Message ->
                    if (it.name == n) Some(it) else findType(n, it)
                else -> None
            }.isDefined()
        }.toOption().map { f -> f as Message }
    }

    private fun Field.defaultValue(ctx: Context) =
        when (this) {
            is StandardField ->
                interceptDefaultValue(
                    this,
                    DefaultValue.render(
                        field = this,
                        type = type,
                        name =
                            if (type == FieldType.ENUM) {
                                typePClass.renderName(ctx.pkg)
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
        fun annotateProperties(msg: Message, ctx: Context) =
            PropertyAnnotator(msg, ctx).annotateProperties()

        fun annotateProperty(f: StandardField, msg: Message, ctx: Context) =
            PropertyAnnotator(msg, ctx).annotateProperties()
                .single { it.name == f.fieldName }
    }
}
