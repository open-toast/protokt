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

import com.toasttab.protokt.codegen.PluginContext
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.STAnnotator.protokt
import com.toasttab.protokt.codegen.impl.STAnnotator.protoktExt
import com.toasttab.protokt.codegen.impl.STAnnotator.protoktExtFqcn
import com.toasttab.protokt.codegen.impl.STAnnotator.protoktProtobuf
import com.toasttab.protokt.codegen.impl.STAnnotator.rootGoogleProto
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.wireFormat

internal val StandardField.tag
    get() = (number shl 3) or if (repeated && packed) 2 else wireFormat

internal val StandardField.tagList
    get() =
        tag.let {
            if (repeated) {
                // (via pb-and-k) As a special case for repeateds, we have
                // to also catch the other (packed or non-packed) versions.
                setOf(
                    it,
                    (number shl 3) or (if (packed) wireFormat else 2)
                )
            } else {
                setOf(it)
            }
        }.sorted()

internal val StandardField.deprecated
    get() = options.default.deprecated

internal fun StandardField.nonDefault(ctx: Context) =
    NonDefaultValueRF.render(
        FieldRenderVar to this,
        NameRenderVar to interceptValueAccess(this, ctx)
    )

internal fun StandardField.boxMap(ctx: Context) =
    BoxMapRF.render(
        TypeRenderVar to type,
        BoxRenderVar to unqualifiedNestedTypeName(ctx)
    )

internal fun StandardField.box(s: String) =
    BoxRF.render(
        TypeRenderVar to type,
        DefRenderVar to s
    )

internal fun StandardField.unqualifiedTypeName(ctx: Context) =
    typePClass(ctx).unqualify(ctx.pkg)

internal fun StandardField.typePClass(ctx: Context) =
    typePClass(ctx.desc.context)

internal fun StandardField.typePClass(pctx: PluginContext) =
    PClass.fromName(fullyQualifiedTypeName(pctx))

internal fun StandardField.unqualifiedNestedTypeName(ctx: Context) =
    ctx.stripRootMessageNamePrefix(unqualifiedTypeName(ctx))

private fun StandardField.fullyQualifiedTypeName(ctx: PluginContext) =
    nativeTypeName.fold(
        {
            when {
                typeName.startsWith(protoktExt) ->
                    protoktExtFqcn + typeName.removePrefix(protoktExt)
                typeName.startsWith(protokt) ->
                    protoktExtFqcn + typeName.removePrefix(protoktProtobuf)
                else ->
                    requalifyProtoType(typeName, ctx).renderName
            }
        },
        {
            if (it.isEmpty()) {
                ConvertTypeRF.render(
                    TypeRenderVar to type
                )
            } else {
                it
            }
        }
    )

internal fun requalifyProtoType(typeName: String, ctx: PluginContext) =
    PClass.fromName(
        overrideGoogleProtobuf(typeName.removePrefix("."), rootGoogleProto)
    ).let {
        if (ctx.respectJavaPackage) {
            PClass.fromName(
                it.nestedName
            ).qualify(ctx.ppackage(typeName))
        } else {
            it
        }
    }
