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

import arrow.core.Some
import com.github.andrewoma.dexx.kollection.immutableListOf
import com.toasttab.protokt.codegen.impl.EnumAnnotator.Companion.annotateEnum
import com.toasttab.protokt.codegen.impl.MessageAnnotator.Companion.annotateMessage
import com.toasttab.protokt.codegen.impl.ServiceAnnotator.annotateService
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.AnnotatedType
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.FileDesc
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.Service
import com.toasttab.protokt.codegen.protoc.TopLevelType
import com.toasttab.protokt.codegen.protoc.TypeDesc

/**
 * STAnnotator is an implementation of a side effect free function.
 * The input is an AST<TypeDesc> and its output is a NEW AST<TypeDesc> that has
 * a NEW fully constructed AnnotatedType attached to each AST node.
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

    fun apply(data: TypeDesc) =
        TypeDesc(
            data.desc,
            AnnotatedType(
                data.type.rawType,
                Some(
                    annotate(
                        data.type.rawType,
                        Context(
                            immutableListOf(),
                            kotlinPackage(data),
                            data.desc
                        )
                    )
                )
            )
        )

    fun annotate(type: TopLevelType, ctx: Context): String =
        when (type) {
            is Message ->
                nonGrpc(ctx) {
                    annotateMessage(
                        type,
                        ctx.copy(enclosing = ctx.enclosing + type)
                    )
                }
            is Enum ->
                nonGrpc(ctx) {
                    annotateEnum(type, ctx)
                }
            is Service ->
                annotateService(
                    type,
                    ctx,
                    ctx.desc.context.generateGrpc ||
                        ctx.desc.context.onlyGenerateGrpc
                )
        }

    private fun nonGrpc(ctx: Context, gen: () -> String) =
        nonGrpc(ctx.desc.context, "", gen)

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
