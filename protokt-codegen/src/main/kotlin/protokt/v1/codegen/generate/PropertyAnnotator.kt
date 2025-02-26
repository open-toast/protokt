/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Deprecation.renderOptions
import protokt.v1.codegen.generate.Implements.overrides
import protokt.v1.codegen.generate.Nullability.deserializeType
import protokt.v1.codegen.generate.Nullability.dslPropertyType
import protokt.v1.codegen.generate.Nullability.generateNonNullAccessor
import protokt.v1.codegen.generate.Nullability.nullable
import protokt.v1.codegen.generate.Nullability.propertyType
import protokt.v1.codegen.generate.Wrapper.interceptDefaultValue
import protokt.v1.codegen.generate.Wrapper.interceptTypeName
import protokt.v1.codegen.generate.Wrapper.wrapped
import protokt.v1.codegen.generate.Wrapper.wrapperRequiresNullability
import protokt.v1.codegen.util.ErrorContext.withFieldName
import protokt.v1.codegen.util.Field
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.Oneof
import protokt.v1.codegen.util.StandardField
import protokt.v1.codegen.util.defaultValue
import protokt.v1.reflect.FieldType

internal fun annotateProperties(msg: Message, ctx: Context) =
    PropertyAnnotator(msg, ctx).annotate()

private class PropertyAnnotator(
    private val msg: Message,
    private val ctx: Context
) {
    fun annotate(): List<PropertyInfo> =
        msg.fields.map { withFieldName(it.fieldName) { annotate(it) } }

    private fun annotate(field: Field): PropertyInfo {
        val documentation = annotatePropertyDocumentation(field, ctx)

        return when (field) {
            is StandardField -> {
                annotateStandard(field).let { type ->
                    val wrapperRequiresNullability = field.wrapperRequiresNullability(ctx)
                    PropertyInfo(
                        name = field.fieldName,
                        number = field.number,
                        propertyType = propertyType(field, type, wrapperRequiresNullability),
                        generateNullableBackingProperty = field.generateNonNullAccessor,
                        deserializeType = deserializeType(field, type),
                        builderPropertyType = dslPropertyType(field, type),
                        defaultValue = field.defaultValue(ctx, msg.mapEntry),
                        fieldType = field.type,
                        repeated = field.repeated,
                        mapEntry = field.mapEntry,
                        nullable = field.nullable || field.optional || wrapperRequiresNullability,
                        overrides = field.overrides(ctx, msg),
                        wrapped = field.wrapped,
                        documentation = documentation,
                        deprecation = deprecation(field)
                    )
                }
            }
            is Oneof ->
                PropertyInfo(
                    name = field.fieldName,
                    propertyType = propertyType(field),
                    deserializeType = field.className.copy(nullable = true),
                    builderPropertyType = field.className.copy(nullable = true),
                    defaultValue = field.defaultValue(ctx, false),
                    oneof = true,
                    nullable = field.nullable,
                    documentation = documentation
                )
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
        if (f.isMap) {
            Map::class
                .asTypeName()
                .parameterizedBy(f.mapKey.interceptTypeName(ctx), f.mapValue.interceptTypeName(ctx))
        } else {
            val parameter = f.interceptTypeName(ctx)

            if (f.repeated) {
                List::class.asTypeName().parameterizedBy(parameter)
            } else {
                parameter
            }
        }

    private fun Field.defaultValue(ctx: Context, mapEntry: Boolean) =
        when (this) {
            is StandardField ->
                interceptDefaultValue(
                    this,
                    when {
                        isMap -> CodeBlock.of("emptyMap()")
                        repeated -> CodeBlock.of("emptyList()")
                        type == FieldType.Message -> CodeBlock.of("null")
                        type == FieldType.Enum -> CodeBlock.of("%T.from(0)", className)
                        nullable && !mapEntry -> CodeBlock.of("null")
                        else -> type.defaultValue
                    },
                    ctx
                )
            is Oneof -> CodeBlock.of("null")
        }
}

internal class PropertyInfo(
    val name: String,
    val number: Int? = null,
    val propertyType: TypeName,
    val generateNullableBackingProperty: Boolean = false,
    val deserializeType: TypeName,
    val builderPropertyType: TypeName,
    val defaultValue: CodeBlock,
    val nullable: Boolean,
    val fieldType: FieldType? = null,
    val repeated: Boolean = false,
    val mapEntry: Message? = null,
    val oneof: Boolean = false,
    val wrapped: Boolean = false,
    val overrides: Boolean = false,
    val documentation: List<String>?,
    val deprecation: Deprecation.RenderOptions? = null
) {
    val isMap
        get() = mapEntry != null
}
