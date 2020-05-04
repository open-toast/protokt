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

import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.Oneof
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.PropertyDocumentationAnnotator.Companion.annotatePropertyDocumentation
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptTypeName
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.codegen.template.Oneof.Oneof as OneofTemplate

internal class OneofAnnotator
private constructor(
    private val msg: MessageType,
    private val ctx: Context
) {
    private fun annotateOneOfs() =
        msg.fields.map {
            when (it) {
                is Oneof ->
                    OneofTemplate.render(
                        name = it.name,
                        types = it.fields.associate { ff -> oneof(it, ff) },
                        options = options(it)
                    )
                else -> ""
            }
        }.filter { it.isNotEmpty() }

    private fun oneof(f: Oneof, ff: StandardField) =
        f.fieldTypeNames.getValue(ff.name).let { oneofFieldTypeName ->
            oneofFieldTypeName to info(ff, oneofFieldTypeName)
        }

    private fun info(
        f: StandardField,
        oneofFieldTypeName: String
    ) =
        OneofTemplate.Info(
            fieldName = f.name,
            type = qualifyWrapperType(
                f,
                PClass.fromName(oneofFieldTypeName),
                interceptTypeName(
                    f,
                    inferOneofFieldTypeName(
                        ctx,
                        f,
                        oneofFieldTypeName
                    ),
                    ctx
                )
            ),
            documentation = annotatePropertyDocumentation(f, ctx),
            deprecation =
                if (f.options.default.deprecated) {
                    renderOptions(
                        f.options.protokt.deprecationMessage
                    )
                } else {
                    null
                }
        )

    private fun qualifyWrapperType(
        f: StandardField,
        unqualifiedType: PClass,
        fieldType: String
    ) =
        if (f.wrapped) {
            PClass.fromName(fieldType).let {
                // If a wrapper type is specified and it shares a name with the
                // oneof, it must be fully qualified.
                if (unqualifiedType.simpleName == it.simpleName) {
                    it.possiblyQualify(ctx.pkg).qualifiedName
                } else {
                    fieldType
                }
            }
        } else {
            fieldType
        }

    private fun inferOneofFieldTypeName(
        ctx: Context,
        f: StandardField,
        oneofFieldTypeName: String
    ): String {
        val pClass = f.typePClass

        // Cannot strip qualifiers for field type in a different package
        val requiresQualifiedTypeName = pClass.ppackage != ctx.pkg

        return if (requiresQualifiedTypeName) {
            pClass.renderName(ctx.pkg)
        } else {
            if (oneofFieldTypeName == pClass.nestedName) {
                // Oneof field name shares name of its type
                if (oneofFieldTypeName == pClass.simpleName) {
                    // Oneof field name shares name of its enclosing type
                    pClass.qualifiedName
                } else {
                    pClass.simpleName
                }
            } else {
                pClass.nestedName
            }
        }
    }

    private fun options(oneof: Oneof) =
        oneof.options.protokt.implements.let {
            OneofTemplate.Options(it.isNotEmpty(), it)
        }

    companion object {
        fun annotateOneOfs(msg: MessageType, ctx: Context) =
            OneofAnnotator(msg, ctx).annotateOneOfs()
    }
}
