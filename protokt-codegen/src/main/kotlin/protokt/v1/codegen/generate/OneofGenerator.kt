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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import protokt.v1.Bytes
import protokt.v1.GeneratedProperty
import protokt.v1.LazyReference
import protokt.v1.StringConverter
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Deprecation.renderOptions
import protokt.v1.codegen.generate.Implements.handleSuperInterface
import protokt.v1.codegen.generate.Wrapper.interceptTypeName
import protokt.v1.codegen.generate.Wrapper.wrapped
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.Oneof
import protokt.v1.codegen.util.StandardField
import protokt.v1.reflect.FieldType

internal fun annotateOneofs(msg: Message, ctx: Context) =
    OneofGenerator(msg, ctx).generate()

private class OneofGenerator(
    private val msg: Message,
    private val ctx: Context
) {
    fun generate(): List<TypeSpec> =
        msg.fields.filterIsInstance<Oneof>().map { oneof ->
            val types = oneof.fields.associate { oneof(oneof, it) }
            val implements =
                oneof.options.protokt.implements
                    .takeIf { it.isNotEmpty() }
                    ?.let { inferClassName(it, ctx) }

            TypeSpec.classBuilder(oneof.name)
                .addModifiers(KModifier.SEALED)
                .handleSuperInterface(implements)
                .addTypes(
                    types.map { (k, v) ->
                        if (v.cachingString) {
                            buildCachingStringVariant(k, v, oneof, implements)
                        } else {
                            buildDataClassVariant(k, v, oneof, implements)
                        }
                    }
                )
                .build()
        }

    private fun buildDataClassVariant(
        name: String,
        v: OneofGeneratorInfo,
        oneof: Oneof,
        implements: ClassName?
    ): TypeSpec =
        TypeSpec.classBuilder(name)
            .addModifiers(KModifier.DATA)
            .superclass(oneof.className)
            .apply {
                v.documentation?.let { addKdoc(formatDoc(it)) }
            }
            .apply {
                if (v.deprecation != null) {
                    addAnnotation(
                        AnnotationSpec.builder(Deprecated::class)
                            .apply {
                                if (v.deprecation.message != null) {
                                    addMember("\"" + v.deprecation.message + "\"")
                                } else {
                                    addMember("\"deprecated in proto\"")
                                }
                            }
                            .build()
                    )
                }
            }
            .addProperty(
                PropertySpec.builder(v.fieldName, v.type)
                    .addAnnotation(
                        AnnotationSpec.builder(GeneratedProperty::class)
                            .addMember("${v.number}")
                            .build()
                    )
                    .initializer(v.fieldName)
                    .build()
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(v.fieldName, v.type)
                    .build()
            )
            .handleSuperInterface(implements, v)
            .build()

    private fun buildCachingStringVariant(
        name: String,
        v: OneofGeneratorInfo,
        oneof: Oneof,
        implements: ClassName?
    ): TypeSpec {
        val lazyRefType = LazyReference::class.asTypeName()
            .parameterizedBy(Bytes::class.asTypeName(), String::class.asTypeName())
        val variantClassName = oneof.className.nestedClass(name)
        val backingName = "_${v.fieldName}"

        return TypeSpec.classBuilder(name)
            .superclass(oneof.className)
            .apply {
                v.documentation?.let { addKdoc(formatDoc(it)) }
            }
            .apply {
                if (v.deprecation != null) {
                    addAnnotation(
                        AnnotationSpec.builder(Deprecated::class)
                            .apply {
                                if (v.deprecation.message != null) {
                                    addMember("\"" + v.deprecation.message + "\"")
                                } else {
                                    addMember("\"deprecated in proto\"")
                                }
                            }
                            .build()
                    )
                }
            }
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addModifiers(KModifier.INTERNAL)
                    .addParameter(backingName, lazyRefType)
                    .build()
            )
            .addProperty(
                PropertySpec.builder(backingName, lazyRefType)
                    .addModifiers(KModifier.INTERNAL)
                    .initializer(backingName)
                    .build()
            )
            .addFunction(
                FunSpec.constructorBuilder()
                    .addParameter(v.fieldName, String::class)
                    .callThisConstructor(
                        CodeBlock.of("%T(%N, %T)", LazyReference::class, v.fieldName, StringConverter::class)
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder(v.fieldName, String::class)
                    .addAnnotation(
                        AnnotationSpec.builder(GeneratedProperty::class)
                            .addMember("${v.number}")
                            .build()
                    )
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return %N.value()", backingName)
                            .build()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("component1")
                    .addModifiers(KModifier.OPERATOR)
                    .returns(String::class)
                    .addStatement("return %N", v.fieldName)
                    .build()
            )
            .addFunction(
                FunSpec.builder("copy")
                    .addParameter(
                        ParameterSpec.builder(v.fieldName, String::class)
                            .defaultValue("this.%N", v.fieldName)
                            .build()
                    )
                    .returns(variantClassName)
                    .addStatement("return %T(%N)", variantClassName, v.fieldName)
                    .build()
            )
            .addFunction(
                FunSpec.builder("equals")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("other", Any::class.asTypeName().copy(nullable = true))
                    .returns(Boolean::class)
                    .addStatement("return other is %T && other.%N == %N", variantClassName, v.fieldName, v.fieldName)
                    .build()
            )
            .addFunction(
                FunSpec.builder("hashCode")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(Int::class)
                    .addStatement("return %N.hashCode()", v.fieldName)
                    .build()
            )
            .addFunction(
                FunSpec.builder("toString")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(String::class)
                    .addStatement("return %S + %N + %S", "$name(${v.fieldName}=", v.fieldName, ")")
                    .build()
            )
            .handleSuperInterface(implements, v)
            .build()
    }

    private fun oneof(f: Oneof, ff: StandardField) =
        f.fieldTypeNames.getValue(ff.fieldName).let { oneofFieldTypeName ->
            oneofFieldTypeName to info(ff)
        }

    private fun info(f: StandardField) =
        OneofGeneratorInfo(
            fieldName = f.fieldName,
            number = f.number,
            type = f.interceptTypeName(ctx),
            documentation = annotatePropertyDocumentation(f, ctx),
            deprecation = deprecation(f),
            cachingString = f.type == FieldType.String && !f.wrapped
        )

    private fun deprecation(f: StandardField) =
        if (f.options.default.deprecated) {
            renderOptions(
                f.options.protokt.deprecationMessage
            )
        } else {
            null
        }
}

class OneofGeneratorInfo(
    val fieldName: String,
    val type: TypeName,
    val number: Int,
    val documentation: List<String>?,
    val deprecation: Deprecation.RenderOptions?,
    val cachingString: Boolean = false
)
