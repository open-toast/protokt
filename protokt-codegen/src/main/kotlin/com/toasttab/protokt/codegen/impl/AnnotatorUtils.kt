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
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptMapKeyTypeName
import com.toasttab.protokt.codegen.impl.Wrapper.interceptTypeName
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.protoc.TypeDesc
import com.toasttab.protokt.codegen.template.Renderers.ConcatWithScope

fun resolveMapEntry(m: Message) =
    MapEntryInfo(
        (m.fields[0] as StandardField),
        (m.fields[1] as StandardField)
    )

fun resolveMapEntryTypes(f: StandardField, m: Message, ctx: Context) =
    resolveMapEntry(m).let {
        MapTypeParams(
            interceptMapKeyTypeName(f, it.key.unqualifiedTypeName, ctx),
            interceptTypeName(f, it.value.typePClass.renderName(ctx.pkg), ctx)
        )
    }

class MapTypeParams(
    val kType: String,
    val vType: String
)

class MapEntryInfo(
    val key: StandardField,
    val value: StandardField
)

fun findMapEntryMessage(
    tn: String,
    msg: Message
): Message =
    findMapEntryMessageInternal(tn, msg)!!

private fun findMapEntryMessageInternal(
    tn: String,
    msg: Message
): Message? {
    val n = tn.split(".").let { if (it.isEmpty()) tn else it.last() }
    return msg.nestedTypes.find {
        it is Message &&
            (it.name == n || findMapEntryMessageInternal(n, it) != null)
    } as? Message
}

internal fun oneOfScope(f: Oneof, type: String, ctx: Context) =
    ctx.stripEnclosingMessageNamePrefix(
        ctx.stripRootMessageNamePrefix(
            ConcatWithScope.render(
                scope = type,
                value = f.name
            )
        )
    )

fun String.emptyToNone() =
    if (isEmpty()) {
        None
    } else {
        Some(this)
    }

fun kotlinPackage(ast: AST<TypeDesc>) =
    resolvePackage(
        ast.data.desc.options,
        ast.data.desc.packageName,
        ast.data.desc.context.respectJavaPackage
    )
