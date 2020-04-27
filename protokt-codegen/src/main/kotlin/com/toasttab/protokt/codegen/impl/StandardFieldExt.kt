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
import com.toasttab.protokt.codegen.impl.STAnnotator.rootGoogleProto
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.template.Box
import com.toasttab.protokt.codegen.template.BoxMap
import com.toasttab.protokt.codegen.template.NonDefaultValue
import com.toasttab.protokt.codegen.template.RenderVariable.Box as BoxVar
import com.toasttab.protokt.codegen.template.RenderVariable.Def
import com.toasttab.protokt.codegen.template.RenderVariable.Field
import com.toasttab.protokt.codegen.template.RenderVariable.Name
import com.toasttab.protokt.codegen.template.RenderVariable.Type
import com.toasttab.protokt.codegen.template.TypeToNative
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
    NonDefaultValue.render(
        Field to this,
        Name to interceptValueAccess(this, ctx)
    )

internal fun StandardField.boxMap(ctx: Context) =
    BoxMap.render(
        Type to type,
        BoxVar to unqualifiedNestedTypeName(ctx)
    )

internal fun StandardField.box(s: String) =
    Box.render(
        Type to type,
        Def to s
    )

internal fun StandardField.unqualifiedTypeName(ctx: Context) =
    typePClass(ctx).nestedName

internal fun StandardField.unqualifiedNestedTypeName(ctx: Context) =
    ctx.stripRootMessageNamePrefix(unqualifiedTypeName(ctx))

internal fun StandardField.typePClass(ctx: Context) =
    typePClass(ctx.desc.context)

internal fun StandardField.typePClass(ctx: PluginContext) =
    nativeTypeName.fold(
        { requalifyProtoType(typeName, ctx) },
        {
            PClass.fromName(
                if (it.isEmpty()) {
                    TypeToNative.render(
                        Type to type
                    )
                } else {
                    it
                }
            )
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
