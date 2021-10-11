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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.codegen.protoc.Method
import com.toasttab.protokt.codegen.protoc.Service
import com.toasttab.protokt.codegen.template.Services.MethodType
import io.grpc.MethodDescriptor
import io.grpc.ServiceDescriptor

internal object ServiceAnnotator {
    fun annotateService(s: Service, ctx: Context, generateService: Boolean): List<TypeSpec> {
        val service =
            if (generateService) {
                TypeSpec.objectBuilder(s.name + "Grpc")
                    .addProperty(
                        PropertySpec.builder("SERVICE_NAME", String::class)
                            .addModifiers(KModifier.CONST)
                            .initializer("\"" + renderQualifiedName(s, ctx) + "\"")
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("serviceDescriptor", ServiceDescriptor::class)
                            .delegate(
                                """
                            |lazy {
                            |  ServiceDescriptor.newBuilder(SERVICE_NAME)
                            |${serviceLines(s)}
                            |}
                        """.trimMargin()
                            )
                            .build()
                    )
                    .addProperties(
                        s.methods.map {
                            PropertySpec.builder(
                                it.name.decapitalize() + "Method",
                                MethodDescriptor::class
                                    .asTypeName()
                                    .parameterizedBy(
                                        TypeVariableName(it.inputType.renderName(ctx.pkg)),
                                        TypeVariableName(it.outputType.renderName(ctx.pkg))
                                    )
                            )
                                .delegate(
                                    """
                                |lazy {
                                |  MethodDescriptor.newBuilder<${it.inputType.renderName(ctx.pkg)}, ${
                                        it.outputType.renderName(
                                            ctx.pkg
                                        )
                                    }>()
                                |    .setType(MethodDescriptor.MethodType.${methodType(it)})
                                |    .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, "${it.name}"))
                                |    .setRequestMarshaller(${it.qualifiedRequestMarshaller(ctx)})
                                |    .setResponseMarshaller(${it.qualifiedResponseMarshaller(ctx)})
                                |    .build()
                                |}
                            """.trimMargin()
                                )
                                .build()
                        }
                    )
                    .build()
            } else {
                null
            }

        val descriptor =
            if (!ctx.desc.context.onlyGenerateGrpc && !ctx.desc.context.lite) {
                TypeSpec.objectBuilder(s.name)
                    .addProperty(
                        PropertySpec.builder("descriptor", ClassName("com.toasttab.protokt", "ServiceDescriptor"))
                            .delegate(
                                """
                                    |lazy {
                                    |  ${ctx.desc.context.fileDescriptorObjectName}.descriptor.services[${s.index}]
                                    |}
                                """.trimMargin()
                            )
                            .build()
                    )
                    .build()
            } else {
                null
            }

        return listOfNotNull(service, descriptor)
    }

    private fun Method.qualifiedRequestMarshaller(ctx: Context) =
        options.protokt.requestMarshaller.takeIf { it.isNotEmpty() }
            ?.let {
                PClass.fromName(options.protokt.requestMarshaller)
                    .possiblyQualify(ctx.pkg)
                    .qualifiedName
            } ?: "com.toasttab.protokt.grpc.KtMarshaller(${inputType.renderName(ctx.pkg)})"

    private fun Method.qualifiedResponseMarshaller(ctx: Context) =
        options.protokt.responseMarshaller.takeIf { it.isNotEmpty() }
            ?.let {
                PClass.fromName(options.protokt.responseMarshaller)
                    .possiblyQualify(ctx.pkg)
                    .qualifiedName
            } ?: "com.toasttab.protokt.grpc.KtMarshaller(${outputType.renderName(ctx.pkg)})"

    private fun serviceLines(s: Service) =
        s.methods.joinToString("\n") {
            "    .addMethod(${it.name.decapitalize()}Method)"
        } + "\n    .build()"

    private fun renderQualifiedName(s: Service, ctx: Context) =
        if (ctx.pkg.default) {
            s.name
        } else {
            "${ctx.desc.packageName}.${s.name}"
        }

    private fun methodType(m: Method) =
        MethodType.render(method = m)
}
