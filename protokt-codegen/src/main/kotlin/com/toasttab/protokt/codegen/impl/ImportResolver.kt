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

package com.toasttab.protokt.codegen.impl

import com.github.andrewoma.dexx.kollection.ImmutableSet
import com.github.andrewoma.dexx.kollection.immutableSetOf
import com.github.andrewoma.dexx.kollection.toImmutableSet
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.impl.ClassLookup.getClassOrNone
import com.toasttab.protokt.codegen.impl.STAnnotator.grpc
import com.toasttab.protokt.codegen.impl.STAnnotator.nonGrpc
import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.model.pclass
import com.toasttab.protokt.codegen.model.rtMethod
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.Field
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.Service
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.protoc.TopLevelType
import com.toasttab.protokt.codegen.protoc.TypeDesc
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.sizeof
import kotlin.reflect.KCallable

class ImportResolver(
    private val ctx: ProtocolContext,
    private val pkg: PPackage
) {
    private val enumImports: ImmutableSet<Import> =
        immutableSetOf(pclass(KtEnum::class), pclass(KtEnumDeserializer::class))

    private val messageImports: ImmutableSet<Import> =
        setOf(
            KtMessage::class,
            KtDeserializer::class,
            KtMessageDeserializer::class,
            KtMessageSerializer::class,
            UnknownFieldSet::class,
            KtGeneratedMessage::class
        ).map { pclass(it) }.toImmutableSet()

    fun resolveImports(astList: List<AST<TypeDesc>>) =
        astList.flatMapToSet { imports(it.data.type.rawType) }
            .asSequence()
            .filterNot { it.pkg == PPackage.KOTLIN }
            .filterNot { it is Import.Class && it.pClass.simpleName == "Any" }
            .filterClassesWithSamePackageName(pkg)
            .filterClassesWithSameNameAsMessageIn(astList)
            .filterClassesWithSameNameAsOneofFieldTypeIn(astList)
            .filterNestedClassesDefinedLocally(astList)
            .filterDuplicateSimpleNames(pkg) { getClassOrNone(it, ctx) }

    private fun imports(t: TopLevelType): ImmutableSet<Import> =
        when (t) {
            is Message -> nonGrpc(ctx, immutableSetOf()) { imports(t) }
            is Enum -> nonGrpc(ctx, immutableSetOf()) { enumImports }
            is Service -> grpc(ctx, immutableSetOf()) { serviceImports(t) }
        }

    private fun serviceImports(s: Service) =
        ServiceImportResolver(s).imports()

    private fun imports(m: Message): ImmutableSet<Import> =
        messageImports +
            m.nestedTypes.flatMapToSet { imports(it) } +
            m.fields.flatMapToSet { imports(it) }

    private fun imports(f: Field): ImmutableSet<Import> =
        immutableSetOf(pclass(Tag::class), rtMethod(SIZEOF)) +
            when (f) {
                is StandardField -> StandardFieldImportResolver(f, ctx, pkg).imports()
                is Oneof -> f.fields.flatMapToSet { imports(it) }
            }

    private inline fun <T, R : Any> Iterable<T>.flatMapToSet(
        transform: (T) -> Iterable<R>
    ): ImmutableSet<R> =
        fold(immutableSetOf()) { s, e -> s + transform(e) }
}

private val SIZEOF = sizeof()

private fun sizeof(): KCallable<*> {
    val sizeof: (String) -> Int = ::sizeof
    return sizeof as KCallable<*>
}
