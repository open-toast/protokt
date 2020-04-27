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
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.template.Services.Descriptor
import com.toasttab.protokt.codegen.template.Services.Method as MethodTemplate
import com.toasttab.protokt.codegen.template.Services.MethodType
import com.toasttab.protokt.codegen.template.Services.Service
import com.toasttab.protokt.codegen.template.Services.Service.MethodInfo

internal object ServiceAnnotator {
    fun annotateService(s: ServiceType, ctx: Context) =
        Service.render(
            name = s.name,
            qualifiedName = renderQualifiedName(s, ctx),
            descriptor = renderDescriptor(s),
            methods = renderMethods(s, ctx)
        )

    private fun renderQualifiedName(s: ServiceType, ctx: Context) =
        if (ctx.pkg.default) {
            s.name
        } else {
            "${ctx.pkg}.${s.name}"
        }

    private fun renderDescriptor(s: ServiceType) =
        Descriptor.render(
            methods = s.methods.map { it.name.decapitalize() }
        )

    private fun renderMethods(s: ServiceType, ctx: Context) =
        s.methods.map { renderMethod(it, ctx) }

    private fun renderMethod(m: Method, ctx: Context) =
        render(m.inputType, ctx).let { `in` ->
            render(m.outputType, ctx).let { out ->
                MethodInfo(
                    m.name,
                    m.name.decapitalize(),
                    MethodTemplate.render(
                        name = m.name.capitalize(),
                        type = methodType(m),
                        `in` = `in`,
                        out = out
                    ),
                    `in`,
                    out
                )
            }
        }

    private fun render(typeName: String, ctx: Context) =
        requalifyProtoType(typeName, ctx.desc.context).renderName(ctx.pkg)

    private fun methodType(m: Method) =
        MethodType.render(method = m)
}
