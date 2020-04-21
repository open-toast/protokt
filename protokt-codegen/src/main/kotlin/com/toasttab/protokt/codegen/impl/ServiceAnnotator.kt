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

import com.toasttab.protokt.codegen.Method
import com.toasttab.protokt.codegen.ServiceType
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.impl.STAnnotator.Context

internal object ServiceAnnotator {
    fun annotateService(
        ast: AST<TypeDesc>,
        s: ServiceType,
        ctx: Context
    ): AST<TypeDesc> {
        ast.data.type.template.map {
            STTemplate.addTo(it as STTemplate, ServiceSt) { arg ->
                when (arg) {
                    is NameServiceVar -> s.name
                    is QualifiedNameServiceVar -> renderQualifiedName(s, ctx)
                    is DescriptorServiceVar -> renderDescriptor(s)
                    is MethodsServiceVar -> renderMethods(s, ctx)
                }
            }
        }
        return ast
    }

    private fun renderQualifiedName(s: ServiceType, ctx: Context) =
        if (ctx.pkg.default) {
            s.name
        } else {
            "${ctx.pkg}.${s.name}"
        }

    private fun renderDescriptor(s: ServiceType) =
        DescriptorSt.render(
            MethodsDescriptorVar to s.methods.map { it.name.decapitalize() }
        )

    private data class MethodInfo(
        val name: String,
        val lowerName: String,
        val body: String,
        val `in`: String,
        val `out`: String
    )

    private fun renderMethods(s: ServiceType, ctx: Context) =
        s.methods.map { renderMethod(it, ctx) }

    private fun renderMethod(m: Method, ctx: Context) =
        render(m.inputType, ctx).let { `in` ->
            render(m.outputType, ctx).let { `out` ->
                MethodInfo(
                    m.name,
                    m.name.decapitalize(),
                    MethodSt.render(
                        NameMethodVar to m.name.capitalize(),
                        TypeMethodVar to methodType(m),
                        InMethodVar to `in`,
                        OutMethodVar to `out`
                    ),
                    `in`,
                    `out`
                )
            }
        }

    private fun render(typeName: String, ctx: Context) =
        requalifyProtoType(typeName, ctx.desc.context).renderName(ctx.pkg)

    private fun methodType(m: Method) =
        MethodTypeSt.render(MethodMethodVar to m)
}
