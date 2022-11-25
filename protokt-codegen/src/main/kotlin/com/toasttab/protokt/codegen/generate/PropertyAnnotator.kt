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

package com.toasttab.protokt.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.generate.Deprecation.renderOptions
import com.toasttab.protokt.codegen.generate.Implements.overrides
import com.toasttab.protokt.codegen.generate.Nullability.deserializeType
import com.toasttab.protokt.codegen.generate.Nullability.dslPropertyType
import com.toasttab.protokt.codegen.generate.Nullability.hasNonNullOption
import com.toasttab.protokt.codegen.generate.Nullability.nullable
import com.toasttab.protokt.codegen.generate.Nullability.propertyType
import com.toasttab.protokt.codegen.generate.Wrapper.interceptDefaultValue
import com.toasttab.protokt.codegen.generate.Wrapper.interceptTypeName
import com.toasttab.protokt.codegen.generate.Wrapper.wrapped
import com.toasttab.protokt.codegen.util.Field
import com.toasttab.protokt.codegen.util.FieldType
import com.toasttab.protokt.codegen.util.Message
import com.toasttab.protokt.codegen.util.Oneof
import com.toasttab.protokt.codegen.util.StandardField

fun annotateProperties(msg: Message, ctx: Context) =
    PropertyAnnotator(msg, ctx).annotate()

private class PropertyAnnotator(
    private val msg: Message,
    private val ctx: Context
) {
    fun annotate(): List<PropertyInfo> {
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
                            nullable = it.nullable || it.optional,
                            nonNullOption = it.hasNonNullOption,
                            overrides = it.overrides(ctx, msg),
                            wrapped = it.wrapped,
                            documentation = documentation,
                            deprecation = deprecation(it)
                        )
                    }
                }
                is Oneof ->
                    PropertyInfo(
                        name = it.fieldName,
                        propertyType = propertyType(it),
                        deserializeType = it.className.copy(nullable = true),
                        dslPropertyType = it.className.copy(nullable = true),
                        defaultValue = it.defaultValue(ctx),
                        oneof = true,
                        nullable = it.nullable,
                        nonNullOption = it.hasNonNullOption,
                        documentation = documentation
                    )
            }
        }
    }

    private fun deprecation(f: StandardField) =
        if (f.options.default.deprecated) {
            renderOptions(
                f.options.protokt.deprecationMessage
            )
        } else {
            null
        }

    private fun annotateStandard(f: StandardField): TypeName =
        if (f.map) {
            val mapTypes = resolveMapEntryTypes(f, ctx)
            Map::class
                .asTypeName()
                .parameterizedBy(mapTypes.kType, mapTypes.vType)
        } else {
            val parameter = interceptTypeName(f, f.className, ctx)

            if (f.repeated) {
                List::class.asTypeName().parameterizedBy(parameter)
            } else {
                parameter
            }
        }

    private fun Field.defaultValue(ctx: Context) =
        when (this) {
            is StandardField ->
                interceptDefaultValue(
                    this,
                    when {
                        map -> CodeBlock.of("emptyMap()")
                        repeated -> CodeBlock.of("emptyList()")
                        type == FieldType.MESSAGE -> CodeBlock.of("null")
                        type == FieldType.ENUM -> CodeBlock.of("%T.from(0)", className)
                        nullable -> CodeBlock.of("null")
                        else -> type.defaultValue
                    },
                    ctx
                )
            is Oneof -> CodeBlock.of("null")
        }
}

class PropertyInfo(
    val name: String,
    val propertyType: TypeName,
    val deserializeType: TypeName,
    val dslPropertyType: TypeName,
    val defaultValue: CodeBlock,
    val nullable: Boolean,
    val nonNullOption: Boolean,
    val fieldType: String = "",
    val repeated: Boolean = false,
    val map: Boolean = false,
    val oneof: Boolean = false,
    val wrapped: Boolean = false,
    val overrides: Boolean = false,
    val documentation: List<String>,
    val deprecation: Deprecation.RenderOptions? = null
)
