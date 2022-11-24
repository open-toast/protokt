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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.impl.runtimeFunction
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UInt32

internal class SerializerAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateSerializer(): FunSpec {
        val fieldSerializations =
            msg.mapFields(
                ctx,
                { serialize(it, ctx) },
                { oneof, std -> serialize(std, ctx, oneof.fieldName) }
            )

        return FunSpec.builder("serialize")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("serializer", KtMessageSerializer::class)
            .apply {
                fieldSerializations.forEach(::addCode)
                addCode("serializer.writeUnknown(unknownFields)")
            }
            .build()
    }

    companion object {
        fun annotateSerializer(msg: Message, ctx: Context) =
            SerializerAnnotator(msg, ctx).annotateSerializer()

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
                    interceptValueAccess(
                        f,
                        ctx,
                        "$t.${f.fieldName}"
                    )
                }

            return when {
                f.repeated && f.packed -> buildCodeBlock {
                    add(
                        "serializer.write(%T(${f.tag.value}))" +
                            ".write(%T(${f.fieldName}.sumOf{%M(",
                        Tag::class,
                        UInt32::class,
                        runtimeFunction("sizeof")
                    )
                    add(f.box("it"))
                    add(")}))\n")
                    add("${f.fieldName}.forEach·{\nserializer.write(")
                    add(f.box("it"))
                    add(")\n}\n")
                }
                f.map -> buildCodeBlock {
                    add(
                        "${f.fieldName}.entries.forEach·{ " +
                            "serializer.write(%T(${f.tag.value}))" +
                            ".write(",
                        Tag::class
                    )
                    add(f.boxMap(ctx))
                    add(") }\n")
                }
                f.repeated -> buildCodeBlock {
                    add(
                        "${f.fieldName}.forEach·{ serializer.write(%T(${f.tag.value})).write(",
                        Tag::class
                    )
                    add(f.box(fieldAccess))
                    add(") }\n")
                }

                else -> buildCodeBlock {
                    add(
                        "serializer.write(%T(${f.tag.value})).write(",
                        Tag::class
                    )
                    add(f.box(fieldAccess))
                    add(")\n")
                }
            }
        }
    }
}
