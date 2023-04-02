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

package com.toasttab.protokt.codegen.generate

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
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.util.Method
import com.toasttab.protokt.codegen.util.Service
import com.toasttab.protokt.codegen.util.comToasttabProtokt
import com.toasttab.protokt.grpc.KtMarshaller
import io.grpc.MethodDescriptor
import io.grpc.MethodDescriptor.MethodType
import io.grpc.ServiceDescriptor

fun generateService(s: Service, ctx: Context, generateService: Boolean) =
    ServiceGenerator(s, ctx, generateService).generate()

private class ServiceGenerator(
    private val s: Service,
    private val ctx: Context,
    private val generateService: Boolean
) {
    fun generate(): List<TypeSpec> {
        val service =
            if (generateService) {
                TypeSpec.objectBuilder(s.name + "Grpc")
                    .addProperty(
                        PropertySpec.builder("SERVICE_NAME", String::class)
                            .addModifiers(KModifier.CONST)
                            .initializer("\"" + renderQualifiedName() + "\"")
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
                                    withIndent { serviceLines().forEach(::add) }
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
                                    .parameterizedBy(method.inputType, method.outputType)
                            )
                                .addModifiers(KModifier.PRIVATE)
                                .delegate(
                                    buildCodeBlock {
                                        beginControlFlow("lazy")
                                        add(
                                            "%M<%T,·%T>()\n",
                                            MemberName(MethodDescriptor::class.asTypeName(), "newBuilder"),
                                            method.inputType,
                                            method.outputType
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
                                            add(".setRequestMarshaller(%L)\n", method.requestMarshaller())
                                            add(".setResponseMarshaller(%L)\n", method.responseMarshaller())
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
            if (!ctx.info.context.onlyGenerateGrpc && !ctx.info.context.lite) {
                TypeSpec.objectBuilder(s.name)
                    .addProperty(
                        PropertySpec.builder("descriptor", ClassName(comToasttabProtokt, "ServiceDescriptor"))
                            .delegate(
                                buildCodeBlock {
                                    beginControlFlow("lazy")
                                    add("${ctx.info.context.fileDescriptorObjectName}.descriptor.services[${s.index}]\n")
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

    private fun Method.requestMarshaller(): CodeBlock =
        marshaller(options.protokt.requestMarshaller, inputType)

    private fun Method.responseMarshaller(): CodeBlock =
        marshaller(options.protokt.responseMarshaller, outputType)

    private fun marshaller(string: String, type: ClassName) =
        string.takeIf { it.isNotEmpty() }
            ?.let { CodeBlock.of("%L", it) }
            ?: CodeBlock.of("%T(%T)", KtMarshaller::class, type)

    private fun serviceLines() =
        s.methods.map {
            CodeBlock.of(".addMethod(_${it.name.decapitalize()}Method)\n")
        } + CodeBlock.of(".build()\n")

    private fun renderQualifiedName() =
        if (ctx.info.protoPackage == "") {
            s.name
        } else {
            "${ctx.info.protoPackage}.${s.name}"
        }

    private fun methodType(m: Method) = when {
        m.clientStreaming && m.serverStreaming -> "BIDI_STREAMING"
        m.clientStreaming -> "CLIENT_STREAMING"
        m.serverStreaming -> "SERVER_STREAMING"
        else -> "UNARY"
    }
}

private fun String.decapitalize() =
    replaceFirstChar { it.lowercaseChar() }