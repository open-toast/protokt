/*
 * Copyright (c) 2019 Toast, Inc.
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

package com.toasttab.protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.v1.KtMessageSerializer
import com.toasttab.protokt.v1.UInt32
import com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.v1.codegen.generate.Wrapper.interceptValueAccess
import com.toasttab.protokt.v1.codegen.generate.Wrapper.mapKeyConverter
import com.toasttab.protokt.v1.codegen.generate.Wrapper.mapValueConverter
import com.toasttab.protokt.v1.codegen.util.FieldType.MESSAGE
import com.toasttab.protokt.v1.codegen.util.Message
import com.toasttab.protokt.v1.codegen.util.Oneof
import com.toasttab.protokt.v1.codegen.util.StandardField

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
                true,
                { serialize(it, ctx) },
                { oneof, std -> serialize(std, ctx, oneof) }
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
    o: Oneof? = null
): CodeBlock {
    val fieldAccess =
        if (o == null) {
            interceptValueAccess(
                f,
                ctx,
                if (f.repeated) { CodeBlock.of("it") } else { CodeBlock.of("%N", f.fieldName) }
            )
        } else {
            interceptValueAccess(f, ctx, CodeBlock.of("%N.%N", o.fieldName, f.fieldName))
        }

    val map = mutableMapOf(
        "uInt32" to UInt32::class,
        "name" to f.fieldName,
        "sizeof" to runtimeFunction("sizeof")
    )
    return when {
        f.repeated && f.packed -> buildCodeBlock {
            map += "boxed" to f.box(CodeBlock.of("it"))
            addNamed(
                "serializer.writeTag(${f.tag.value}u)" +
                    ".write(%uInt32:T(%name:N.sumOf{%sizeof:M(%boxed:L)}.toUInt()))\n",
                map
            )
            addNamed("%name:N.forEach·{·serializer.write(%boxed:L)·}", map)
        }
        f.map -> buildCodeBlock {
            beginControlFlow("${f.fieldName}.entries.forEach")
            add(
                "serializer.writeTag(${f.tag.value}u).write(%L)\n",
                f.boxMap(ctx)
            )
            endControlFlowWithoutNewline()
        }
        f.repeated -> buildCodeBlock {
            map += "boxed" to f.box(fieldAccess)
            addNamed(
                "%name:N.forEach·{·" +
                    "serializer.writeTag(${f.tag.value}u).write(%boxed:L)·}",
                map
            )
        }

        else -> buildCodeBlock {
            add(
                "serializer.writeTag(${f.tag.value}u).write(%L)",
                f.box(fieldAccess)
            )
        }
    }
}

private fun StandardField.boxMap(ctx: Context): CodeBlock {
    if (type != MESSAGE) {
        return CodeBlock.of("")
    }
    val keyParam =
        mapKeyConverter(this, ctx)
            ?.let { CodeBlock.of("%T.unwrap(it.key)", it) }
            ?: CodeBlock.of("it.key")

    val valParam =
        mapValueConverter(this, ctx)
            ?.let { CodeBlock.of("%T.unwrap(it.value)", it) }
            ?: CodeBlock.of("it.value")

    return CodeBlock.of("%T(%L, %L)", className, keyParam, valParam)
}
