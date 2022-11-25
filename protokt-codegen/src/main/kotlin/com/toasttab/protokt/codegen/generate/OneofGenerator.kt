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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.impl.Deprecation
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.Implements.handleSuperInterface
import com.toasttab.protokt.codegen.impl.Message
import com.toasttab.protokt.codegen.impl.Oneof
import com.toasttab.protokt.codegen.impl.StandardField
import com.toasttab.protokt.codegen.impl.Wrapper.interceptTypeName
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.impl.emptyToNone

fun annotateOneofs(msg: Message, ctx: Context) =
    OneofGenerator(msg, ctx).generate()

private class OneofGenerator(
    private val msg: Message,
    private val ctx: Context
) {
    fun generate(): List<TypeSpec> =
        msg.fields.filterIsInstance<Oneof>().map {
            val options = options(it)
            val types = it.fields.associate { ff -> oneof(it, ff) }

            TypeSpec.classBuilder(it.name)
                .addModifiers(KModifier.SEALED)
                .handleSuperInterface(options)
                .addTypes(
                    types.map { (k, v) ->
                        TypeSpec.classBuilder(k)
                            .addModifiers(KModifier.DATA)
                            .superclass(it.className)
                            .apply {
                                if (v.documentation.isNotEmpty()) {
                                    addKdoc(formatDoc(v.documentation))
                                }
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
                                    .initializer(v.fieldName)
                                    .build()
                            )
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                    .addParameter(v.fieldName, v.type)
                                    .build()
                            )
                            .handleSuperInterface(options, v)
                            .build()
                    }
                )
                .build()
        }

    private fun oneof(f: Oneof, ff: StandardField) =
        f.fieldTypeNames.getValue(ff.fieldName).let { oneofFieldTypeName ->
            oneofFieldTypeName to info(ff)
        }

    private fun info(f: StandardField) =
        OneofGeneratorInfo(
            fieldName = f.fieldName,
            type =
            if (f.wrapped) {
                interceptTypeName(f, f.className, ctx)
            } else {
                f.className
            },
            documentation = annotatePropertyDocumentation(f, ctx),
            deprecation = deprecation(f)
        )

    private fun deprecation(f: StandardField) =
        if (f.options.default.deprecated) {
            renderOptions(
                f.options.protokt.deprecationMessage
            )
        } else {
            null
        }

    private fun options(oneof: Oneof) =
        OneofGeneratorOptions(
            oneof.options.protokt.implements.emptyToNone().fold(
                { null },
                { possiblyQualify(it) }
            )
        )

    private fun possiblyQualify(implements: String): ClassName {
        val bestGuess = ClassName.bestGuess(implements)
        return if (bestGuess.packageName == "" && implements in namespaceNeighbors()) {
            ClassName(ctx.info.kotlinPackage, implements)
        } else {
            bestGuess
        }
    }

    private fun namespaceNeighbors() =
        msg.fields.filterIsInstance<Oneof>().map { it.name }
}

class OneofGeneratorInfo(
    val fieldName: String,
    val type: TypeName,
    val documentation: List<String>,
    val deprecation: Deprecation.RenderOptions?
)

class OneofGeneratorOptions(
    val implements: ClassName?
)
