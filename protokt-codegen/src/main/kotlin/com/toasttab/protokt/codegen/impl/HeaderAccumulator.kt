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

import arrow.core.extensions.list.foldable.firstOption
import arrow.syntax.collections.flatten
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.OneOf
import com.toasttab.protokt.codegen.PluginContext
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.Type
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.impl.ClassLookup.converters
import com.toasttab.protokt.codegen.impl.STAnnotator.protoktExtFqcn
import com.toasttab.protokt.codegen.impl.STAnnotator.protoktFqcn
import com.toasttab.protokt.codegen.impl.Wrapper.converterPkg
import com.toasttab.protokt.codegen.model.PPackage

internal object HeaderAccumulator {
    fun writeHeader(astList: List<AST<TypeDesc>>, acc: (String) -> Unit) {
        astList.firstOption().map { f ->
            acc(
                HeaderSt.render(
                    listOf(
                        kotlinPackage(f).toString().emptyToNone()
                            .map { PackageHeaderVar to it }
                    ).flatten() +
                        listOf(
                            extImport(astList, f.data.desc.context),
                            RuntimePackageHeaderVar to protoktFqcn
                        )
                )
            )
        }
    }

    private fun extImport(
        astList: List<AST<TypeDesc>>,
        ctx: PluginContext
    ): Pair<HeaderVar, Any?> =
        ExtPackageHeaderVar to
            if (needsExtPackage(astList, ctx) && !inExtPackage(astList)) {
                converterPkg
            } else {
                null
            }

    private fun inExtPackage(astList: List<AST<TypeDesc>>) =
        astList.isNotEmpty() &&
            astList.any { kotlinPackage(it) == converterPkg }

    private fun needsExtPackage(
        astList: List<AST<TypeDesc>>,
        ctx: PluginContext
    ) =
        usesWellKnownWrappers(astList, ctx) || usesWellKnownValues(astList, ctx)

    private fun usesWellKnownWrappers(
        astList: List<AST<TypeDesc>>,
        ctx: PluginContext
    ) =
        wrappers(astList).intersect(wellKnownWrappers(ctx)).isNotEmpty()

    private fun wellKnownWrappers(params: PluginContext) =
        converters(params.classpath)
            .filter { it::class.java.`package`.name == converterPkg.toString() }
            .map { it.wrapper.qualifiedName!! }

    private fun wrappers(astList: List<AST<TypeDesc>>) =
        applyToAstList(astList) { it.options.protokt.wrap }
            .filterNot { it.isEmpty() }
            .toSet()

    private fun usesWellKnownValues(
        astList: List<AST<TypeDesc>>,
        ctx: PluginContext
    ): Boolean =
        fieldPackageNames(astList, ctx)
            .any { !it.default && it == PPackage.fromString(protoktExtFqcn) }

    private fun fieldPackageNames(
        astList: List<AST<TypeDesc>>,
        ctx: PluginContext
    ) =
        applyToAstList(astList) { it.typePClass(ctx).ppackage }

    private fun <T> applyToAstList(
        astList: List<AST<TypeDesc>>,
        fn: (StandardField) -> T
    ) =
        astList.flatMap { applyToType(it.data.type.rawType, fn) }

    private fun <T> applyToType(t: Type, fn: (StandardField) -> T): Set<T> =
        when (t) {
            is MessageType ->
                applyToFieldsOfMessage(t, fn) +
                    t.nestedTypes.flatMap { applyToType(it, fn) }
            else -> emptySet()
        }

    private fun <T> applyToFieldsOfMessage(
        msg: MessageType,
        fn: (StandardField) -> T
    ) =
        msg.fields.flatMap {
            when (it) {
                is StandardField -> setOf(fn(it))
                is OneOf -> it.fields.map(fn)
            }
        }.toSet()
}
