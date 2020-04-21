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
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.OneOf
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.impl.STAnnotator.Context

internal fun resolveMapEntry(m: MessageType, ctx: Context) =
    MapTypeParams(
        (m.fields[0] as StandardField).unqualifiedTypeName(ctx),
        (m.fields[1] as StandardField).unqualifiedNestedTypeName(ctx)
    )

internal data class MapTypeParams(
    val kType: String,
    val vType: String
)

internal fun oneOfScope(f: OneOf, type: String, ctx: Context) =
    ctx.stripEnclosingMessageNamePrefix(
        ctx.stripRootMessageNamePrefix(
            ConcatWithScopeRF.render(
                ScopedValueRenderVar to
                    ScopedValueSt(
                        type,
                        f.nativeTypeName
                    )
            )
        )
    )

internal fun String.emptyToNone() =
    if (isEmpty()) {
        None
    } else {
        Some(this)
    }

internal fun kotlinPackage(ast: AST<TypeDesc>) =
    resolvePackage(
        ast.data.desc.options,
        ast.data.desc.packageName,
        ast.data.desc.context.respectJavaPackage
    )
