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

package com.toasttab.protokt.codegen.annotators

import arrow.core.None
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.annotators.PropertyDocumentationAnnotator.Companion.annotatePropertyDocumentation
import com.toasttab.protokt.codegen.impl.Deprecation
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.Implements.handleSuperInterface
import com.toasttab.protokt.codegen.impl.Wrapper.interceptTypeName
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.impl.emptyToNone
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField

internal class OneofAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateOneofs(): List<TypeSpec> {

        return msg.fields.filterIsInstance<Oneof>().map {
            val options = options(it)
            val types = it.fields.associate { ff -> oneof(it, ff) }

            TypeSpec.classBuilder(it.name)
                .addModifiers(KModifier.SEALED)
                .handleSuperInterface(options)
                .addTypes(
                    types.map { (k, v) ->
                        TypeSpec.classBuilder(k)
                            .addModifiers(KModifier.DATA)
                            .superclass(it.typeName)
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
                                PropertySpec.builder(v.fieldName.removePrefix("`").removeSuffix("`"), v.type)
                                    .initializer(v.fieldName.removePrefix("`").removeSuffix("`"))
                                    .build()
                            )
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                    .addParameter(v.fieldName.removePrefix("`").removeSuffix("`"), v.type)
                                    .build()
                            )
                            .handleSuperInterface(options, v)
                            .build()
                    }
                )
                .build()
        }
    }

    private fun oneof(f: Oneof, ff: StandardField) =
        f.fieldTypeNames.getValue(ff.fieldName).let { oneofFieldTypeName ->
            oneofFieldTypeName to info(ff, oneofFieldTypeName)
        }

    private fun info(
        f: StandardField,
        oneofFieldTypeName: String
    ) =
        Info(
            fieldName = f.fieldName,
            type =
            if (f.wrapped) {
                interceptTypeName(
                    f,
                    TypeVariableName(
                        inferOneofFieldTypeName(
                            ctx,
                            f,
                            oneofFieldTypeName
                        )
                    ),
                    ctx
                )
            } else {
                f.typePClass.toTypeName()
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

    private fun inferOneofFieldTypeName(
        ctx: Context,
        f: StandardField,
        oneofFieldTypeName: String
    ): String {
        val pClass = f.typePClass

        // Cannot strip qualifiers for field type in a different package
        // See testing/runtime-tests/src/main/proto/com/toasttab/protokt/testing/rt/oneof/oneof_packages.proto
        val requiresQualifiedTypeName = pClass.ppackage != ctx.desc.kotlinPackage

        return if (requiresQualifiedTypeName) {
            pClass.renderName(ctx.desc.kotlinPackage)
        } else {
            // See testing/runtime-tests/src/main/proto/com/toasttab/protokt/testing/rt/oneof/oneof_exercises.proto
            if (oneofFieldTypeName == pClass.simpleName) {
                if (oneofFieldTypeName == pClass.nestedName) {
                    pClass.qualifiedName
                } else {
                    pClass.nestedName
                }
            } else {
                pClass.simpleName
            }
        }
    }

    private fun options(oneof: Oneof) =
        Options(
            oneof.options.protokt.implements.emptyToNone().fold(
                { null },
                { possiblyQualify(it) }
            )
        )

    private fun possiblyQualify(implements: String) =
        if (PClass.fromName(implements).ppackage == PPackage.DEFAULT) {
            if (implements in namespaceNeighbors()) {
                PClass(implements, ctx.desc.kotlinPackage, None).qualifiedName
            } else {
                implements
            }
        } else {
            implements
        }

    private fun namespaceNeighbors() =
        msg.fields.filterIsInstance<Oneof>().map { it.name }

    companion object {
        fun annotateOneofs(msg: Message, ctx: Context) =
            OneofAnnotator(msg, ctx).annotateOneofs()
    }

    internal class Info(
        val fieldName: String,
        val type: TypeName,
        val documentation: List<String>,
        val deprecation: Deprecation.RenderOptions?
    )

    internal class Options(
        val implements: String?
    )
}
