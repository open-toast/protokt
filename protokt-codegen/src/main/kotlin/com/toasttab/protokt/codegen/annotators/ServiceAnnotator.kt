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

package com.toasttab.protokt.codegen.annotators

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.impl.bindMargin
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.codegen.protoc.Method
import com.toasttab.protokt.codegen.protoc.Service
import com.toasttab.protokt.codegen.util.decapitalize
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
                        PropertySpec.builder("_serviceDescriptor", ServiceDescriptor::class)
                            .addModifiers(KModifier.PRIVATE)
                            .delegate(
                                """
                                    |lazy {
                                    |    ServiceDescriptor.newBuilder(SERVICE_NAME)
                                    |${serviceLines(s)}
                                    |}
                                """.bindMargin()
                            )
                            .build()
                    )
                    .addProperties(
                        s.methods.map { method ->
                            PropertySpec.builder(
                                "_" + method.name.decapitalize() + "Method",
                                MethodDescriptor::class
                                    .asTypeName()
                                    .parameterizedBy(
                                        method.inputType.toTypeName(),
                                        method.outputType.toTypeName()
                                    )
                            )
                                .addModifiers(KModifier.PRIVATE)
                                .delegate(
                                    """
                                        |lazy {
                                        |    MethodDescriptor.newBuilder<${method.inputType.renderName(ctx.desc.kotlinPackage)}, ${method.outputType.renderName(ctx.desc.kotlinPackage)}>()
                                        |        .setType(MethodDescriptor.MethodType.${methodType(method)})
                                        |        .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, "${method.name}"))
                                        |        .setRequestMarshaller(${method.qualifiedRequestMarshaller(ctx)})
                                        |        .setResponseMarshaller(${method.qualifiedResponseMarshaller(ctx)})
                                        |        .build()
                                        |}
                                    """.bindMargin()
                                )
                                .build()
                        }
                    )
                    .addFunction(
                        FunSpec.builder("getServiceDescriptor")
                            .addCode("return _serviceDescriptor")
                            .addAnnotation(JvmStatic::class)
                            .build()
                    )
                    .addFunctions(
                        s.methods.map { method ->
                            FunSpec.builder("get" + method.name + "Method")
                                .addCode("return _" + method.name.decapitalize() + "Method")
                                .addAnnotation(JvmStatic::class)
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
                                    |    ${ctx.desc.context.fileDescriptorObjectName}.descriptor.services[${s.index}]
                                    |}
                                """.bindMargin()
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
                    .possiblyQualify(ctx.desc.kotlinPackage)
                    .qualifiedName
            } ?: "com.toasttab.protokt.grpc.KtMarshaller(${inputType.renderName(ctx.desc.kotlinPackage)})"

    private fun Method.qualifiedResponseMarshaller(ctx: Context) =
        options.protokt.responseMarshaller.takeIf { it.isNotEmpty() }
            ?.let {
                PClass.fromName(options.protokt.responseMarshaller)
                    .possiblyQualify(ctx.desc.kotlinPackage)
                    .qualifiedName
            } ?: "com.toasttab.protokt.grpc.KtMarshaller(${outputType.renderName(ctx.desc.kotlinPackage)})"

    private fun serviceLines(s: Service) =
        s.methods.joinToString("\n") { method ->
            "      .addMethod(_${method.name.decapitalize()}Method)"
        } + "\n        .build()"

    private fun renderQualifiedName(s: Service, ctx: Context) =
        if (ctx.desc.kotlinPackage.default) {
            s.name
        } else {
            "${ctx.desc.protoPackage}.${s.name}"
        }

    private fun methodType(m: Method) = when {
        m.clientStreaming && m.serverStreaming -> "BIDI_STREAMING"
        m.clientStreaming -> "CLIENT_STREAMING"
        m.serverStreaming -> "SERVER_STREAMING"
        else -> "UNARY"
    }
}
