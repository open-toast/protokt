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
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.impl.endControlFlowWithoutNewline
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.codegen.protoc.Method
import com.toasttab.protokt.codegen.protoc.Service
import com.toasttab.protokt.codegen.util.decapitalize
import com.toasttab.protokt.grpc.KtMarshaller
import io.grpc.MethodDescriptor
import io.grpc.MethodDescriptor.MethodType
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
                                buildCodeBlock {
                                    beginControlFlow("lazy")
                                    add(
                                        "%M(SERVICE_NAME)\n",
                                        MemberName(ServiceDescriptor::class.asTypeName(), "newBuilder")
                                    )
                                    withIndent { serviceLines(s).forEach(::add) }
                                    endControlFlowWithoutNewline()
                                }
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
                                    buildCodeBlock {
                                        beginControlFlow("lazy")
                                        add(
                                            "%M<%T,·%T>()\n",
                                            MemberName(MethodDescriptor::class.asTypeName(), "newBuilder"),
                                            method.inputType.toTypeName(),
                                            method.outputType.toTypeName()
                                        )
                                        withIndent {
                                            add(
                                                ".setType(%M)\n",
                                                MemberName(MethodType::class.asTypeName(), methodType(method))
                                            )
                                            add(
                                                ".setFullMethodName(%M(SERVICE_NAME,·\"${method.name}\"))\n",
                                                MemberName(MethodDescriptor::class.asTypeName(), "generateFullMethodName")
                                            )
                                            add(".setRequestMarshaller(%L)\n", method.requestMarshaller(ctx))
                                            add(".setResponseMarshaller(%L)\n", method.responseMarshaller(ctx))
                                            add(".build()\n")
                                        }
                                        endControlFlowWithoutNewline()
                                    }
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
                                buildCodeBlock {
                                    beginControlFlow("lazy")
                                    add("${ctx.desc.context.fileDescriptorObjectName}.descriptor.services[${s.index}]\n")
                                    endControlFlowWithoutNewline()
                                }
                            )
                            .build()
                    )
                    .build()
            } else {
                null
            }

        return listOfNotNull(service, descriptor)
    }

    private fun Method.requestMarshaller(ctx: Context): CodeBlock =
        options.protokt.requestMarshaller.takeIf { it.isNotEmpty() }
            ?.let {
                PClass.fromName(options.protokt.requestMarshaller)
                    .possiblyQualify(ctx.desc.kotlinPackage)
                    .toTypeName()
                    .let { CodeBlock.of("%T", it) }
            }
            ?: CodeBlock.of(
                "%T(%T)",
                KtMarshaller::class,
                inputType.toTypeName()
            )

    private fun Method.responseMarshaller(ctx: Context): CodeBlock =
        options.protokt.responseMarshaller.takeIf { it.isNotEmpty() }
            ?.let {
                PClass.fromName(options.protokt.responseMarshaller)
                    .possiblyQualify(ctx.desc.kotlinPackage)
                    .toTypeName()
                    .let { CodeBlock.of("%T", it) }
            }
            ?: CodeBlock.of(
                "%T(%T)",
                KtMarshaller::class,
                outputType.toTypeName()
            )

    private fun serviceLines(s: Service) =
        s.methods.map {
            CodeBlock.of(".addMethod(_${it.name.decapitalize()}Method)\n")
        } + CodeBlock.of(".build()\n")

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
