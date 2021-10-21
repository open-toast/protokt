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

package com.toasttab.protokt.codegen.annotators

import com.squareup.kotlinpoet.TypeName
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptMapKeyTypeName
import com.toasttab.protokt.codegen.impl.Wrapper.interceptMapValueTypeName
import com.toasttab.protokt.codegen.impl.resolvePackage
import com.toasttab.protokt.codegen.impl.stripEnclosingMessageNamePrefix
import com.toasttab.protokt.codegen.impl.stripRootMessageNamePrefix
import com.toasttab.protokt.codegen.protoc.FileDesc
import com.toasttab.protokt.codegen.protoc.MapEntry
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.Protocol
import com.toasttab.protokt.codegen.protoc.StandardField

fun resolveMapEntry(m: Message) =
    MapEntry(
        (m.fields[0] as StandardField),
        (m.fields[1] as StandardField)
    )

fun resolveMapEntryTypes(f: StandardField, ctx: Context) =
    f.mapEntry!!.let {
        MapTypeParams(
            interceptMapKeyTypeName(f, it.key.typePClass.toTypeName(), ctx)!!,
            interceptMapValueTypeName(f, it.value.typePClass.toTypeName(), ctx)!!
        )
    }

class MapTypeParams(
    val kType: TypeName,
    val vType: TypeName
)

fun oneOfScope(f: Oneof, type: String, ctx: Context) =
    ctx.stripEnclosingMessageNamePrefix(
        ctx.stripRootMessageNamePrefix("$type.${f.name}")
    )

fun kotlinPackage(protocol: Protocol) =
    kotlinPackage(protocol.desc)

fun kotlinPackage(desc: FileDesc) =
    resolvePackage(
        desc.options,
        desc.protoPackage,
        desc.context.respectJavaPackage
    )
