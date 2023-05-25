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
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
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
import io.grpc.BindableService
import io.grpc.MethodDescriptor
import io.grpc.MethodDescriptor.MethodType
import io.grpc.ServerMethodDefinition
import io.grpc.ServerServiceDefinition
import io.grpc.ServiceDescriptor
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.kotlin.AbstractCoroutineServerImpl
import io.grpc.kotlin.ServerCalls
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction3

fun generateService(s: Service, ctx: Context, generateService: Boolean, kotlinPlugin: KotlinPlugin?) =
    ServiceGenerator(s, ctx, generateService, kotlinPlugin).generate()

private class ServiceGenerator(
    private val s: Service,
    private val ctx: Context,
    private val generateService: Boolean,
    private val kotlinPlugin: KotlinPlugin?
) {
    fun generate(): List<TypeSpec> =
        grpcImplementations() + listOfNotNull(serviceDescriptor())

    private fun grpcImplementations(): List<TypeSpec> =
        if (generateService && supportedPlugin()) {
            val getMethodFunctions =
                s.methods.map { method ->
                    FunSpec.builder("get" + method.name + "Method")
                        .addCode("return _" + method.name.decapitalize() + "Method")
                        .staticIfAppropriate()
                        .build()
                }

            val getServiceDescriptorFunction =
                buildFunSpec("getServiceDescriptor") {
                    addCode("return _serviceDescriptor")
                    staticIfAppropriate()
                }

            val grpcServiceObjectClassName = ClassName(ctx.info.kotlinPackage, s.name + "Grpc")
            val grpcServiceObject =
                TypeSpec.objectBuilder(grpcServiceObjectClassName)
                    .addProperty(
                        PropertySpec.builder("SERVICE_NAME", String::class)
                            .addModifiers(KModifier.CONST)
                            .initializer("\"" + renderQualifiedName() + "\"")
                            .build()
                    )
                    .addServiceDescriptor()
                    .addMethodProperties()
                    .addFunction(getServiceDescriptorFunction)
                    .addFunctions(getMethodFunctions)
                    .build()

            val coroutineServerBase =
                coroutineServerBase(
                    grpcServiceObjectClassName,
                    getServiceDescriptorFunction,
                    getMethodFunctions
                )

            listOfNotNull(grpcServiceObject, coroutineServerBase)
        } else {
            emptyList()
        }

    private fun TypeSpec.Builder.addServiceDescriptor() =
        apply {
            val serviceDescriptor = pivotClassName(ServiceDescriptor::class)
            addProperty(
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
        }

    private fun TypeSpec.Builder.addMethodProperties() =
        apply {
            val methodDescriptor = pivotClassName(MethodDescriptor::class)
            val methodType = pivotClassName(MethodType::class)
            addProperties(
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
                                        MemberName(methodType, methodType(method).name)
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
        }

    private fun coroutineServerBase(
        grpcServiceObjectClassName: ClassName,
        getServerDescriptorFunction: FunSpec,
        getMethodFunctions: List<FunSpec>
    ): TypeSpec? =
        if (kotlinPlugin == KotlinPlugin.JS) {
            val implementations = implementations()
            val coroutineServerClassName = ClassName(ctx.info.kotlinPackage, s.name + "CoroutineImplBase")
            TypeSpec.classBuilder(coroutineServerClassName)
                .addModifiers(KModifier.ABSTRACT)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(
                            ParameterSpec.builder("coroutineContext", CoroutineContext::class)
                                .defaultValue("%L", EmptyCoroutineContext::class.asClassName())
                                .build()
                        )
                        .build()
                )
                .addSuperclassConstructorParameter("coroutineContext")
                .superclass(pivotClassName(AbstractCoroutineServerImpl::class))
                .addFunctions(implementations)
                .addBindService(
                    grpcServiceObjectClassName,
                    getServerDescriptorFunction,
                    getMethodFunctions,
                    implementations
                )
                .build()
        } else {
            null
        }

    private fun implementations() =
        s.methods.map { method ->
            buildFunSpec(method.name.replaceFirstChar { it.lowercase() }) {
                addModifiers(KModifier.OPEN)
                when (methodType(method)) {
                    MethodType.CLIENT_STREAMING, MethodType.UNARY ->
                        addModifiers(KModifier.SUSPEND)
                    else -> Unit
                }
                when (methodType(method)) {
                    MethodType.UNARY, MethodType.SERVER_STREAMING ->
                        addParameter("request", method.inputType)
                    MethodType.CLIENT_STREAMING, MethodType.BIDI_STREAMING ->
                        addParameter("requests", Flow::class.asClassName().parameterizedBy(method.inputType))
                    MethodType.UNKNOWN -> error("unsupported method type")
                }
                when (methodType(method)) {
                    MethodType.UNARY, MethodType.CLIENT_STREAMING ->
                        returns(method.outputType)
                    MethodType.SERVER_STREAMING, MethodType.BIDI_STREAMING ->
                        returns(Flow::class.asClassName().parameterizedBy(method.outputType))
                    MethodType.UNKNOWN -> error("unsupported method type")
                }
                addCode(
                    "throw %L(%L.%L(\"Method·%L.%L·is·unimplemented\"))",
                    pivotClassName(StatusException::class),
                    MemberName(pivotClassName(Status::class), Status::UNIMPLEMENTED.name),
                    Status.UNIMPLEMENTED::withDescription.name,
                    renderQualifiedName(),
                    method.name
                )
            }
        }

    private fun TypeSpec.Builder.addBindService(
        grpcServiceObjectClassName: ClassName,
        getServiceDescriptorFunction: FunSpec,
        getMethodFunctions: List<FunSpec>,
        implementations: List<FunSpec>
    ) =
        apply {
            addFunction(
                buildFunSpec(BindableService::bindService.name) {
                    addModifiers(KModifier.OVERRIDE, KModifier.FINAL)
                    returns(pivotClassName(ServerServiceDefinition::class))

                    val builder: KFunction1<String, ServerServiceDefinition.Builder> = ServerServiceDefinition::builder
                    addCode(
                        "return %M(%M())\n",
                        MemberName(pivotClassName(ServerServiceDefinition::class).nestedClass("Companion"), builder.name),
                        grpcServiceObjectClassName.member(getServiceDescriptorFunction.name)
                    )

                    s.methods.forEachIndexed { idx, method ->
                        addCode(
                            ".%L(%M(this.context, %M(), ::%L))\n",
                            ServiceDescriptor.Builder::addMethod.name,
                            MemberName(pivotClassName(ServerCalls::class), methodDefinitionForMethod(method)),
                            grpcServiceObjectClassName.member(getMethodFunctions[idx].name),
                            implementations[idx].name
                        )
                    }
                    addCode(".build()")
                }
            )
        }

    private fun methodDefinitionForMethod(method: Method) =
        when (methodType(method)) {
            MethodType.UNARY -> {
                val def: KFunction3<CoroutineContext, MethodDescriptor<Any, Any>, suspend (Any) -> Any, ServerMethodDefinition<Any, Any>> =
                    ServerCalls::unaryServerMethodDefinition
                def.name
            }
            MethodType.CLIENT_STREAMING -> {
                val def: KFunction3<CoroutineContext, MethodDescriptor<Any, Any>, suspend (Flow<Any>) -> Any, ServerMethodDefinition<Any, Any>> =
                    ServerCalls::clientStreamingServerMethodDefinition
                def.name
            }
            MethodType.SERVER_STREAMING -> {
                val def: KFunction3<CoroutineContext, MethodDescriptor<Any, Any>, (Any) -> Flow<Any>, ServerMethodDefinition<Any, Any>> =
                    ServerCalls::serverStreamingServerMethodDefinition
                def.name
            }
            MethodType.BIDI_STREAMING -> {
                val def: KFunction3<CoroutineContext, MethodDescriptor<Any, Any>, (Flow<Any>) -> Flow<Any>, ServerMethodDefinition<Any, Any>> =
                    ServerCalls::bidiStreamingServerMethodDefinition
                def.name
            }
            MethodType.UNKNOWN -> error("unsupported method type")
        }

    private fun serviceDescriptor() =
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
    m.clientStreaming && m.serverStreaming -> MethodType.BIDI_STREAMING
    m.clientStreaming -> MethodType.CLIENT_STREAMING
    m.serverStreaming -> MethodType.SERVER_STREAMING
    else -> MethodType.UNARY
}

private fun String.decapitalize() =
    replaceFirstChar { it.lowercaseChar() }
