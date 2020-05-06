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
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.algebra.Annotator
import com.toasttab.protokt.codegen.impl.EnumAnnotator.annotateEnum
import com.toasttab.protokt.codegen.impl.MessageAnnotator.annotateMessage
import com.toasttab.protokt.codegen.impl.ServiceAnnotator.annotateService
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.AnnotatedType
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.FileDesc
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Service
import com.toasttab.protokt.codegen.protoc.TopLevelType
import com.toasttab.protokt.codegen.protoc.TypeDesc

/**
 * STAnnotator is an implementation of a side effect free function.
 * The input is an AST<TypeDesc> and its output is a NEW AST<TypeDesc> that has
 * a NEW fully constructed AnnotatedType attached to each AST node.
 */
object STAnnotator : Annotator<AST<TypeDesc>> {
    const val rootGoogleProto = "google.protobuf"
    const val googleProto = ".google.protobuf"

    const val protokt = ".protokt"
    const val protoktPkg = "com.toasttab.protokt"
    const val protoktRtPkg = "com.toasttab.protokt.rt"

    data class Context(
        val enclosingMessage: List<Message>,
        val pkg: PPackage,
        val desc: FileDesc
    )

    override fun invoke(ast: AST<TypeDesc>) =
        AST(
            TypeDesc(
                ast.data.desc,
                AnnotatedType(
                    ast.data.type.rawType,
                    Some(
                        annotate(
                            ast.data.type.rawType,
                            Context(
                                immutableListOf(),
                                kotlinPackage(ast),
                                ast.data.desc
                            )
                        )
                    )
                )
            )
        )

    fun annotate(type: TopLevelType, ctx: Context): String =
        when (type) {
            is Message ->
                annotateMessage(
                    type,
                    ctx.copy(enclosingMessage = ctx.enclosingMessage + type)
                )
            is Enum -> annotateEnum(type, ctx)
            is Service -> annotateService(type, ctx)
        }
}
