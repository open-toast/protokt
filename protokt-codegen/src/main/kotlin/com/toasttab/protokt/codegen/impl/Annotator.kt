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

import com.github.andrewoma.dexx.kollection.immutableListOf
import com.squareup.kotlinpoet.TypeSpec
import com.toasttab.protokt.codegen.impl.MessageAnnotator.Companion.annotateMessage
import com.toasttab.protokt.codegen.impl.ServiceAnnotator.annotateService
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.AnnotatedType
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.FileDesc
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Protocol
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.Service
import com.toasttab.protokt.codegen.protoc.TopLevelType
import com.toasttab.protokt.codegen.protoc.TypeDesc

/**
 * Annotates an unannotated AST. This effectively converts the protobuf AST to a Kotlin AST.
 */
object Annotator {
    const val rootGoogleProto = "google.protobuf"
    const val googleProto = ".google.protobuf"

    const val protokt = ".protokt"
    const val protoktPkg = "com.toasttab.protokt"
    const val protoktRtPkg = "com.toasttab.protokt.rt"

    data class Context(
        val enclosing: List<Message>,
        val pkg: PPackage,
        val desc: FileDesc
    )

    fun apply(protocol: Protocol) =
        protocol.types.flatMap {
            val annotated =
                annotate(
                    it,
                    Context(
                        immutableListOf(),
                        kotlinPackage(protocol),
                        protocol.desc
                    )
                )

            annotated.map { type ->
                TypeDesc(protocol.desc, AnnotatedType(it, type))
            }
        }

    fun annotate(type: TopLevelType, ctx: Context): Iterable<TypeSpec> =
        when (type) {
            is Message ->
                nonGrpc(ctx) {
                    nonDescriptors(ctx) {
                        listOf(
                            annotateMessage(
                                type,
                                ctx.copy(enclosing = ctx.enclosing + type)
                            )
                        )
                    }
                }
            is Enum ->
                nonGrpc(ctx) {
                    nonDescriptors(ctx) {
                        listOf(EnumBuilder(type, ctx).build())
                    }
                }
            is Service ->
                annotateService(
                    type,
                    ctx,
                    ctx.desc.context.generateGrpc ||
                        ctx.desc.context.onlyGenerateGrpc
                )
        }

    private fun <T> nonDescriptors(ctx: Context, gen: () -> Iterable<T>) =
        nonDescriptors(ctx.desc.context, emptyList(), gen)

    fun <T> nonDescriptors(ctx: ProtocolContext, default: T, gen: () -> T) =
        boolGen(!ctx.onlyGenerateDescriptors, default, gen)

    private fun <T> nonGrpc(ctx: Context, gen: () -> Iterable<T>) =
        nonGrpc(ctx.desc.context, emptyList(), gen)

    fun <T> nonGrpc(ctx: ProtocolContext, default: T, gen: () -> T) =
        boolGen(!ctx.onlyGenerateGrpc, default, gen)

    fun <T> grpc(ctx: ProtocolContext, default: T, gen: () -> T) =
        boolGen(ctx.generateGrpc || ctx.onlyGenerateGrpc, default, gen)

    private fun <T> boolGen(bool: Boolean, default: T, gen: () -> T) =
        if (bool) {
            gen()
        } else {
            default
        }
}
