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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.impl.Deprecation.enclosingDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.hasDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.EnumDocumentationAnnotator.Companion.annotateEnumDocumentation
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer

class EnumAnnotator(
    val e: Enum,
    val ctx: Context
) {
    fun annotateEnum(): TypeSpec {
        return TypeSpec.classBuilder(e.name)
            .addModifiers(KModifier.SEALED)
            .superclass(KtEnum::class)
            .apply {
                val documentation = annotateEnumDocumentation(e, ctx)
                if (documentation.isNotEmpty()) {
                    addKdoc(formatDoc(documentation))
                }
            }
            .apply {
                val deprecation = enumDeprecation()
                if (deprecation != null) {
                    addAnnotation(
                        AnnotationSpec.builder(Deprecated::class)
                            .apply {
                                if (deprecation.message != null) {
                                    addMember("\"" + deprecation.message + "\"")
                                } else {
                                    addMember("\"deprecated in proto\"")
                                }
                            }
                            .build()
                    )
                }
            }
            .apply {
                if (e.hasDeprecation && !enclosingDeprecation(ctx)) {
                    addAnnotation(
                        AnnotationSpec.builder(Suppress::class)
                            .addMember("\"DEPRECATION\"")
                            .build()
                    )
                }
            }
            .addProperty(
                PropertySpec.builder("value", Int::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("value")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("name", String::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("name")
                    .build()
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("value", Int::class)
                    .addParameter("name", String::class)
                    .build()
            )
            .addTypes(
                e.values.map {
                    TypeSpec.objectBuilder(it.valueName)
                        .superclass(TypeVariableName(e.name))
                        .addSuperclassConstructorParameter(it.number.toString())
                        .addSuperclassConstructorParameter("\"${it.name}\"")
                        .apply {
                            if (it.options.default.deprecated) {
                                addAnnotation(
                                    AnnotationSpec.builder(Deprecated::class)
                                        .apply {
                                            if (it.options.protokt.deprecationMessage.isNotEmpty()) {
                                                addMember("\"" + it.options.protokt.deprecationMessage + "\"")
                                            } else {
                                                addMember("\"deprecated in proto\"")
                                            }
                                        }
                                        .build()
                                )
                            }
                        }
                        .build()
                }
            )
            .addType(
                TypeSpec.classBuilder("UNRECOGNIZED")
                    .superclass(TypeVariableName(e.name))
                    .addSuperclassConstructorParameter("value")
                    .addSuperclassConstructorParameter("\"UNRECOGNIZED\"")
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("value", Int::class)
                            .build()
                    )
                    .build()
            )
            .addType(
                TypeSpec.companionObjectBuilder("Deserializer")
                    .addSuperinterface(
                        KtEnumDeserializer::class
                            .asTypeName()
                            .parameterizedBy(TypeVariableName(e.name))
                    )
                    .addFunction(
                        FunSpec.builder("from")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(TypeVariableName(e.name))
                            .addParameter("value", Int::class)
                            .addCode(
                                """
                                    |return when (value) {
                                    |${enumLines()}
                                    |  else -> UNRECOGNIZED(value)
                                    |}
                                """.trimMargin()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun enumLines() =
        e.values.distinctBy { it.number }.joinToString("\n") {
            "  " + it.number + " -> " + it.valueName
        }

    private fun enumDeprecation() =
        if (e.options.default.deprecated) {
            renderOptions(
                e.options.protokt.deprecationMessage
            )
        } else {
            null
        }
}
