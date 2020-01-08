/*
 * Copyright (c) 2019. Toast Inc.
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
import com.toasttab.protokt.codegen.EnumType
import com.toasttab.protokt.codegen.FileDesc
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.MethodType
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

/**
 * STAnnotator is an implementation of a side effect free function.
 * The input is an AST<TypeDesc> and its output is a NEW AST<TypeDesc> that has
 * a NEW fully constructed AnnotatedType attached to each AST node.
 */
object STAnnotator : Annotator<AST<TypeDesc>> {
    const val com = "com"
    const val protobuf = "protobuf"

    const val googleProto = ".google.$protobuf"

    const val protokt = ".protokt"
    const val protoktProtobuf = "$protokt.$protobuf"
    const val protoktExt = "$protokt.ext"
    const val protoktExtFqcn = "$com.toasttab$protoktExt"

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
            is ServiceType -> addService(ast, t)
            is MethodType -> addMethod(ast, t)
        }

    private fun addMessage(a: AST<TypeDesc>, msg: MessageType, ctx: Context) =
        annotate(a, Some(STTemplate.toTemplate(MessageSt))).let { ast ->
            annotateMessage(
                Optics.astChildrenLens.set(
                    ast,
                    msg.nestedTypes.map {
                        invoke(
                            Optics.astLens.set(
                                ast,
                                Optics.typeDescTypeLens.set(
                                    ast.data,
                                    Optics.annotatedTypeTemplateLens.set(
                                        Optics.annotatedTypeLens.set(
                                            ast.data.type,
                                            it
                                        ),
                                        Some(STTemplate.toTemplate(MessageSt))
                                    )
                                )
                            ),
                            ctx
                        )
                    }
                ),
                msg,
                ctx
            )
        }

    private fun addEnum(a: AST<TypeDesc>, e: EnumType, ctx: Context) =
        annotateEnum(annotate(a, Some(STTemplate.toTemplate(EnumSt))), e, ctx)

    private fun addService(a: AST<TypeDesc>, s: ServiceType) =
        annotate(a, Some(STTemplate.toTemplate(ServiceSt))).let { ast ->
            annotateService(
                Optics.astChildrenLens.set(
                    ast,
                    s.methods.map {
                        invoke(
                            Optics.astLens.set(
                                ast,
                                Optics.typeDescTypeLens.set(
                                    ast.data,
                                    Optics.annotatedTypeTemplateLens.set(
                                        Optics.annotatedTypeLens.set(
                                            ast.data.type,
                                            it
                                        ),
                                        Some(STTemplate.toTemplate(ServiceSt))
                                    )
                                )
                            )
                        )
                    }
                ),
                s
            )
        }

    private fun addMethod(
        ast: AST<TypeDesc>,
        @Suppress("UNUSED_PARAMETER") m: MethodType
    ): AST<TypeDesc> {
        // TODO: methods are not nested
        return ast
    }
}
