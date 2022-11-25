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

package com.toasttab.protokt.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.generate.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.util.Message
import com.toasttab.protokt.codegen.util.StandardField
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UInt32

fun generateSerializer(msg: Message, ctx: Context) =
    SerializerGenerator(msg, ctx).generate()

private class SerializerGenerator(
    private val msg: Message,
    private val ctx: Context
) {
    fun generate(): FunSpec {
        val fieldSerializations =
            msg.mapFields(
                ctx,
                { serialize(it, ctx) },
                { oneof, std -> serialize(std, ctx, oneof.fieldName) }
            )

        return buildFunSpec("serialize") {
            addModifiers(KModifier.OVERRIDE)
            addParameter("serializer", KtMessageSerializer::class)
            fieldSerializations.forEach(::addCode)
            addCode("serializer.writeUnknown(unknownFields)")
        }
    }
}

fun serialize(
    f: StandardField,
    ctx: Context,
    t: String? = null
): CodeBlock {
    val fieldAccess =
        if (t == null) {
            interceptValueAccess(
                f,
                ctx,
                if (f.repeated) { "it" } else { f.fieldName }
            )
        } else {
            interceptValueAccess(f, ctx, "$t.${f.fieldName}")
        }

    val map = mutableMapOf(
        "tag" to Tag::class,
        "uInt32" to UInt32::class,
        "name" to f.fieldName,
        "sizeof" to runtimeFunction("sizeof")
    )
    return when {
        f.repeated && f.packed -> buildCodeBlock {
            map += "boxed" to f.box(CodeBlock.of("it"))
            addNamed(
                "serializer.write(%tag:T(${f.tag.value}))" +
                    ".write(%uInt32:T(%name:L.sumOf{%sizeof:M(%boxed:L)}))\n",
                map
            )
            addNamed("%name:L.forEach·{·serializer.write(%boxed:L)·}", map)
        }
        f.map -> buildCodeBlock {
            beginControlFlow("${f.fieldName}.entries.forEach")
            add(
                "serializer.write(%T(${f.tag.value})).write(%L)\n",
                Tag::class,
                f.boxMap(ctx)
            )
            endControlFlowWithoutNewline()
        }
        f.repeated -> buildCodeBlock {
            map += "boxed" to f.box(fieldAccess)
            addNamed(
                "%name:L.forEach·{·" +
                    "serializer.write(%tag:T(${f.tag.value})).write(%boxed:L)·}",
                map
            )
        }

        else -> buildCodeBlock {
            add(
                "serializer.write(%T(${f.tag.value})).write(%L)",
                Tag::class,
                f.box(fieldAccess)
            )
        }
    }
}
