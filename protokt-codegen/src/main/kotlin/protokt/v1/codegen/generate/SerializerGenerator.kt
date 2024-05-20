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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.Writer
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Wrapper.interceptValueAccess
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.Oneof
import protokt.v1.codegen.util.StandardField

internal val WRITER = Writer::class.simpleName!!.lowercase()

internal fun generateSerializer(msg: Message, properties: List<PropertySpec>, ctx: Context) =
    SerializerGenerator(msg, properties, ctx).generate()

private class SerializerGenerator(
    private val msg: Message,
    private val properties: List<PropertySpec>,
    private val ctx: Context
) {
    fun generate(): FunSpec {
        val fieldSerializations =
            msg.mapFields(
                ctx,
                properties,
                true,
                { f, p -> serialize(f, ctx, p) },
                { oneof, std, p -> serialize(std, ctx, p, oneof) }
            )

        return buildFunSpec("serialize") {
            addModifiers(KModifier.OVERRIDE)
            addParameter(WRITER, Writer::class)
            fieldSerializations.forEach(::addCode)
            addCode("$WRITER.writeUnknown(unknownFields)")
        }
    }
}

internal fun serialize(
    f: StandardField,
    ctx: Context,
    p: PropertySpec,
    o: Oneof? = null
): CodeBlock {
    val fieldAccess =
        if (o == null) {
            interceptValueAccess(
                f,
                ctx,
                if (f.repeated) {
                    CodeBlock.of("it")
                } else {
                    CodeBlock.of("%N", p)
                }
            )
        } else {
            interceptValueAccess(f, ctx, CodeBlock.of("%N.%N", o.fieldName, f.fieldName))
        }

    return when {
        f.repeated && f.packed -> buildCodeBlock {
            addNamed(
                "$WRITER.writeTag(${f.tag.value}u)" +
                    ".%writeUInt32:L(%elementsSize:L.toUInt())\n",
                mapOf(
                    "writeUInt32" to Writer::writeUInt32.name,
                    "elementsSize" to f.elementsSize()
                )
            )
            add("%N.forEach·{·$WRITER.%L·}", p, f.write(CodeBlock.of("it")))
        }
        f.isMap -> buildCodeBlock {
            beginControlFlow("%N.entries.forEach", p)
            add(
                "$WRITER.writeTag(${f.tag.value}u).write(%L)\n",
                f.boxMap()
            )
            endControlFlowWithoutNewline()
        }
        f.repeated -> buildCodeBlock {
            addNamed(
                "%name:N.forEach·{·" +
                    "$WRITER.writeTag(${f.tag.value}u).%write:L·}",
                mapOf(
                    "name" to p,
                    "write" to f.write(fieldAccess)
                )
            )
        }

        else -> buildCodeBlock {
            add(
                "$WRITER.writeTag(${f.tag.value}u).%L",
                f.write(fieldAccess)
            )
        }
    }
}

private fun StandardField.boxMap() =
    CodeBlock.of(
        "%T(%L, %L)",
        className,
        CodeBlock.of("it.key"),
        CodeBlock.of("it.value")
    )

private fun StandardField.write(value: CodeBlock) =
    CodeBlock.of("%L(%L)", type.writeFn, value)
