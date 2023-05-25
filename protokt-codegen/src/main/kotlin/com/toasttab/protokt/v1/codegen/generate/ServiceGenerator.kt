/*
 * Copyright (c) 2020 Toast, Inc.
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

package com.toasttab.protokt.v1.codegen.generate

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
import com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.v1.codegen.util.KotlinPlugin
import com.toasttab.protokt.v1.codegen.util.Method
import com.toasttab.protokt.v1.codegen.util.Service
import com.toasttab.protokt.v1.codegen.util.comToasttabProtoktV1
import com.toasttab.protokt.v1.grpc.KtMarshaller
import com.toasttab.protokt.v1.grpc.SchemaDescriptor
import io.grpc.MethodDescriptor
import io.grpc.MethodDescriptor.MethodType
import io.grpc.ServiceDescriptor
import kotlin.reflect.KClass

fun generateService(s: Service, ctx: Context, generateService: Boolean, kotlinPlugin: KotlinPlugin?) =
    ServiceGenerator(s, ctx, generateService, kotlinPlugin).generate()

private class ServiceGenerator(
    private val s: Service,
    private val ctx: Context,
    private val generateService: Boolean,
    private val kotlinPlugin: KotlinPlugin?
) {
    fun generate(): List<TypeSpec> {
        val service =
            if (generateService && supportedPlugin()) {
                val serviceDescriptor = pivotClassName(ServiceDescriptor::class)
                val methodDescriptor = pivotClassName(MethodDescriptor::class)
                val methodType = pivotClassName(MethodType::class)

                TypeSpec.objectBuilder(s.name + "Grpc")
                    .addProperty(
                        PropertySpec.builder("SERVICE_NAME", String::class)
                            .addModifiers(KModifier.CONST)
                            .initializer("\"" + renderQualifiedName() + "\"")
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("_serviceDescriptor", serviceDescriptor)
                            .addModifiers(KModifier.PRIVATE)
                            .delegate(
                                buildCodeBlock {
                                    beginControlFlow("lazy")
                                    add(
                                        "%M(SERVICE_NAME)\n",
                                        MemberName(staticOrCompanionMethod(serviceDescriptor), "newBuilder")
                                    )
                                    withIndent { serviceLines(ctx).filterNotNull().forEach(::add) }
                                    endControlFlowWithoutNewline()
                                }
                            )
                            .build()
                    )
                    .addProperties(
                        s.methods.map { method ->
                            PropertySpec.builder(
                                "_" + method.name.decapitalize() + "Method",
                                methodDescriptor.parameterizedBy(method.inputType, method.outputType)
                            )
                                .addModifiers(KModifier.PRIVATE)
                                .delegate(
                                    buildCodeBlock {
                                        beginControlFlow("lazy")
                                        add(
                                            "%M<%T,·%T>()\n",
                                            MemberName(staticOrCompanionMethod(methodDescriptor), "newBuilder"),
                                            method.inputType,
                                            method.outputType
                                        )
                                        withIndent {
                                            add(
                                                ".setType(%M)\n",
                                                MemberName(methodType, methodType(method))
                                            )
                                            add(
                                                ".setFullMethodName(%M(SERVICE_NAME,·\"${method.name}\"))\n",
                                                MemberName(staticOrCompanionMethod(methodDescriptor), "generateFullMethodName")
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
                            .staticIfAppropriate()
                            .build()
                    )
                    .addFunctions(
                        s.methods.map { method ->
                            FunSpec.builder("get" + method.name + "Method")
                                .addCode("return _" + method.name.decapitalize() + "Method")
                                .staticIfAppropriate()
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
                        PropertySpec.builder("descriptor", ClassName(comToasttabProtoktV1, "ServiceDescriptor"))
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

    private fun supportedPlugin() =
        try {
            pivotClassName(Unit::class)
            true
        } catch (ex: IllegalStateException) {
            false
        }

    private fun pivotClassName(jvmClass: KClass<*>) =
        when (kotlinPlugin) {
            KotlinPlugin.JS -> ClassName(KtMarshaller::class.java.`package`!!.name, jvmClass.asTypeName().simpleNames)
            KotlinPlugin.MULTIPLATFORM -> error("unsupported plugin for service generation: $kotlinPlugin")
            else -> jvmClass.asTypeName()
        }

    private fun staticOrCompanionMethod(type: ClassName) =
        pivotPlugin(type, ClassName(type.packageName, type.simpleNames + "Companion"))

    private fun <T> pivotPlugin(jvm: T, js: T) =
        when (kotlinPlugin) {
            KotlinPlugin.JS -> js
            KotlinPlugin.MULTIPLATFORM -> error("unsupported plugin for service generation: $kotlinPlugin")
            else -> jvm
        }

    private fun FunSpec.Builder.staticIfAppropriate() =
        apply {
            if (kotlinPlugin != KotlinPlugin.JS) {
                addAnnotation(JvmStatic::class)
            }
        }

    private fun serviceLines(ctx: Context) =
        s.methods.map {
            CodeBlock.of(".addMethod(_${it.name.decapitalize()}Method)\n")
        } +
            if (pivotPlugin(jvm = true, js = false)) {
                CodeBlock.of(
                    ".setSchemaDescriptor(%T(className = %S, fileDescriptorClassName = %S))\n",
                    SchemaDescriptor::class,
                    "${ctx.info.kotlinPackage}.${s.name}",
                    "${ctx.info.kotlinPackage}.${ctx.info.context.fileDescriptorObjectName}"
                )
            } else {
                null
            } +
            CodeBlock.of(".build()\n")

    private fun renderQualifiedName() =
        if (ctx.info.protoPackage == "") {
            s.name
        } else {
            "${ctx.info.protoPackage}.${s.name}"
        }
}

private fun Method.requestMarshaller(): CodeBlock =
    marshaller(options.protokt.requestMarshaller, inputType)

private fun Method.responseMarshaller(): CodeBlock =
    marshaller(options.protokt.responseMarshaller, outputType)

private fun marshaller(string: String, type: ClassName) =
    string.takeIf { it.isNotEmpty() }
        ?.let { CodeBlock.of("%L", it) }
        ?: CodeBlock.of("%T(%T)", KtMarshaller::class, type)

private fun methodType(m: Method) = when {
    m.clientStreaming && m.serverStreaming -> "BIDI_STREAMING"
    m.clientStreaming -> "CLIENT_STREAMING"
    m.serverStreaming -> "SERVER_STREAMING"
    else -> "UNARY"
}

private fun String.decapitalize() =
    replaceFirstChar { it.lowercaseChar() }
