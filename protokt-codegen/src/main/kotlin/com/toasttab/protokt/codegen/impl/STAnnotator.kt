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

import arrow.core.None
import arrow.core.Some
import com.github.andrewoma.dexx.kollection.immutableListOf
import com.toasttab.protokt.codegen.EnumType
import com.toasttab.protokt.codegen.FileDesc
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.Optics
import com.toasttab.protokt.codegen.Optics.annotate
import com.toasttab.protokt.codegen.ServiceType
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.algebra.Annotator
import com.toasttab.protokt.codegen.impl.EnumAnnotator.annotateEnum
import com.toasttab.protokt.codegen.impl.MessageAnnotator.annotateMessage
import com.toasttab.protokt.codegen.impl.ServiceAnnotator.annotateService
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.template.prepare

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
        val enclosingMessage: List<MessageType>,
        val pkg: PPackage,
        val desc: FileDesc
    )

    override fun invoke(ast: AST<TypeDesc>) =
        invoke(
            ast,
            Context(
                immutableListOf(),
                kotlinPackage(ast),
                ast.data.desc
            )
        )

    private fun invoke(ast: AST<TypeDesc>, ctx: Context): AST<TypeDesc> =
        when (val t = ast.data.type.rawType) {
            is MessageType ->
                addMessage(
                    ast,
                    t,
                    ctx.copy(enclosingMessage = ctx.enclosingMessage + t)
                )
            is EnumType -> addEnum(ast, t, ctx)
            is ServiceType -> addService(ast, t, ctx)
        }

    private fun addMessage(a: AST<TypeDesc>, msg: MessageType, ctx: Context) =
        annotate(
            Optics.astChildrenLens.set(
                a,
                msg.nestedTypes.map {
                    invoke(
                        Optics.astLens.set(
                            a,
                            Optics.typeDescTypeLens.set(
                                a.data,
                                Optics.annotatedTypeRenderableLens.set(
                                    Optics.annotatedTypeLens.set(
                                        a.data.type,
                                        it
                                    ),
                                    None
                                )
                            )
                        ),
                        ctx
                    )
                }
            ),
            Some(annotateMessage(msg, ctx).prepare())
        )

    private fun addEnum(a: AST<TypeDesc>, e: EnumType, ctx: Context) =
        annotate(a, Some(annotateEnum(e, ctx).prepare()))

    private fun addService(a: AST<TypeDesc>, s: ServiceType, ctx: Context) =
        annotate(a, Some(annotateService(s, ctx).prepare()))
}
