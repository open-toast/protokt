/*
 * Copyright (c) 2020 Toast Inc.
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

import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.OneOf
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.FieldDocumentationAnnotator.Companion.annotateFieldDocumentation
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptTypeName
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.codegen.snakeToCamel

internal class OneOfAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateOneOfs() =
        msg.fields.map {
            when (it) {
                is OneOf ->
                    OneOfSt.render(
                        NameOneOfVar to it.nativeTypeName,
                        TypesOneOfVar to it.fields.associate(::oneOfValue),
                        OptionsOneOfVar to options(it)
                    )
                else -> ""
            }
        }.filter { it.isNotEmpty() }

    private fun oneOfValue(f: StandardField) =
        snakeToCamel(f.name).let { fieldName ->
            fieldName.capitalize().let { oneofFieldTypeName ->
                oneofFieldTypeName to info(f, fieldName, oneofFieldTypeName)
            }
        }

    private fun info(
        f: StandardField,
        fieldName: String,
        oneofFieldTypeName: String
    ) =
        OneOfInfo(
            fieldName = fieldName,
            type = qualifyWrapperType(
                PClass.fromName(oneofFieldTypeName),
                PClass.fromName(
                    interceptTypeName(
                        f,
                        inferOneofFieldTypeName(
                            ctx,
                            f,
                            oneofFieldTypeName
                        ),
                        ctx
                    )
                )
            ),
            documentation = annotateFieldDocumentation(f, ctx),
            deprecation =
                if (f.options.default.deprecated) {
                    renderOptions(
                        f.options.protokt.deprecationMessage
                    )
                } else {
                    null
                }
        )

    private fun qualifyWrapperType(unqualifiedType: PClass, fieldType: PClass) =
        // If a wrapper type is specified and it shares a name with the oneof,
        // it must be fully qualified.
        if (unqualifiedType.simpleName == fieldType.simpleName) {
            fieldType.possiblyQualify(ctx.pkg).qualifiedName
        } else {
            fieldType.unqualify(ctx.pkg)
        }

    private fun inferOneofFieldTypeName(
        ctx: Context,
        f: StandardField,
        oneofFieldTypeName: String
    ): String {
        val pClass = f.typePClass()

        // Cannot strip qualifiers for field type in a different package
        val requiresQualifiedTypeName = !pClass.isInPackage(ctx.pkg)

        return if (requiresQualifiedTypeName) {
            pClass.qualifiedName
        } else {
            if (oneofFieldTypeName == pClass.nestedName) {
                // Oneof field name shares name of its type
                if (oneofFieldTypeName == pClass.simpleName) {
                    // Oneof field is the same as its enclosing type
                    pClass.qualifiedName
                } else {
                    pClass.simpleName
                }
            } else {
                pClass.nestedName
            }
        }
    }

    private data class OneOfInfo(
        val fieldName: String,
        val type: String,
        val documentation: List<String>,
        val deprecation: Deprecation.RenderOptions?
    )

    private fun options(oneof: OneOf) =
        oneof.options.protokt.implements.let {
            OneOfOptions(it.isNotEmpty(), it)
        }

    private data class OneOfOptions(
        val doesImplement: Boolean,
        val implements: String
    )

    companion object {
        fun annotateOneOfs(msg: MessageType, ctx: Context) =
            OneOfAnnotator(msg, ctx).annotateOneOfs()
    }
}
