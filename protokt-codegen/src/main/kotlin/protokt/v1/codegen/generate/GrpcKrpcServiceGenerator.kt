/*
 * Copyright (c) 2026 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.coroutines.flow.Flow
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.util.Method
import protokt.v1.codegen.util.Service

private val GRPC_ANNOTATION = ClassName("kotlinx.rpc.grpc.annotations", "Grpc")

internal fun generateGrpcKrpcService(s: Service, ctx: Context): TypeSpec? =
    if (ctx.info.context.generateGrpcKrpc && ctx.info.context.kotlinTarget.isPrimaryTarget) {
        GrpcKrpcServiceGenerator(s, ctx).generate()
    } else {
        null
    }

private class GrpcKrpcServiceGenerator(
    private val s: Service,
    private val ctx: Context
) {
    fun generate(): TypeSpec {
        val builder = TypeSpec.interfaceBuilder(s.name)
            .addAnnotation(grpcAnnotation())

        s.methods.forEach { method ->
            builder.addFunction(methodFunction(method))
        }

        return builder.build()
    }

    private fun grpcAnnotation(): AnnotationSpec {
        val builder = AnnotationSpec.builder(GRPC_ANNOTATION)
        val protoPackage = ctx.info.protoPackage
        val kotlinPackage = ctx.info.kotlinPackage
        if (protoPackage.isNotEmpty() && protoPackage != kotlinPackage) {
            builder.addMember("protoPackage = %S", protoPackage)
        }
        return builder.build()
    }

    private fun methodFunction(method: Method): FunSpec {
        val builder = buildFunSpec(method.name.toString()) {
            addModifiers(KModifier.ABSTRACT)

            if (!method.serverStreaming) {
                addModifiers(KModifier.SUSPEND)
            }

            val inputType = if (method.clientStreaming) {
                Flow::class.asClassName().parameterizedBy(method.inputType)
            } else {
                method.inputType
            }
            addParameter(ParameterSpec("message", inputType))

            val returnType = if (method.serverStreaming) {
                Flow::class.asClassName().parameterizedBy(method.outputType)
            } else {
                method.outputType
            }
            returns(returnType)
        }

        return builder
    }
}
