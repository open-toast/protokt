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
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.impl.Deprecation.enclosingDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.hasDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.DeserializerAnnotator.Companion.annotateDeserializer
import com.toasttab.protokt.codegen.impl.FieldAnnotator.Companion.annotateFields
import com.toasttab.protokt.codegen.impl.Implements.doesImplement
import com.toasttab.protokt.codegen.impl.Implements.implements
import com.toasttab.protokt.codegen.impl.MapEntryAnnotator.annotateMapEntry
import com.toasttab.protokt.codegen.impl.MessageDocumentationAnnotator.annotateMessageDocumentation
import com.toasttab.protokt.codegen.impl.OneOfAnnotator.Companion.annotateOneOfs
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.SerializerAnnotator.Companion.annotateSerializer
import com.toasttab.protokt.codegen.impl.SizeOfAnnotator.Companion.annotateSizeof
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.template.MessageTemplate
import com.toasttab.protokt.codegen.template.MessageVariable.Deserialize
import com.toasttab.protokt.codegen.template.MessageVariable.Entry
import com.toasttab.protokt.codegen.template.MessageVariable.Inner
import com.toasttab.protokt.codegen.template.MessageVariable.Message
import com.toasttab.protokt.codegen.template.MessageVariable.Oneofs
import com.toasttab.protokt.codegen.template.MessageVariable.Options
import com.toasttab.protokt.codegen.template.MessageVariable.Params
import com.toasttab.protokt.codegen.template.MessageVariable.Serialize
import com.toasttab.protokt.codegen.template.MessageVariable.Sizeof

internal object MessageAnnotator {
    val idealMaxWidth = 100

    fun annotateMessage(
        ast: AST<TypeDesc>,
        msg: MessageType,
        ctx: Context
    ): AST<TypeDesc> {
        ast.data.type.template.map {
            STTemplate.addTo(it as STTemplate, MessageTemplate) { f ->
                when (f) {
                    is Message -> annotateMessage(msg, ctx)
                    is Entry -> annotateMapEntry(msg, ctx)
                    is Params -> annotateFields(msg, ctx)
                    is Oneofs -> annotateOneOfs(msg, ctx)
                    is Sizeof -> annotateSizeof(msg, ctx)
                    is Serialize -> annotateSerializer(msg, ctx)
                    is Deserialize -> annotateDeserializer(msg, ctx)
                    is Options -> options(msg, ctx)
                    is Inner -> null
                }
            }
        }
        return ast
    }

    private fun annotateMessage(msg: MessageType, ctx: Context) =
        MessageParams(
            name = msg.name,
            doesImplement = msg.doesImplement,
            implements = msg.implements,
            documentation = annotateMessageDocumentation(ctx),
            deprecation =
                if (msg.options.default.deprecated) {
                    renderOptions(
                        msg.options.protokt.deprecationMessage
                    )
                } else {
                    null
                },
            suppressDeprecation = msg.hasDeprecation &&
                (!enclosingDeprecation(ctx) ||
                        ctx.enclosingMessage.firstOption()
                            .fold({ false }, { it == msg })),
            fullTypeName = msg.fullProtobufTypeName
        )

    private data class MessageParams(
        val name: String,
        val doesImplement: Boolean,
        val implements: String,
        val documentation: List<String>,
        val deprecation: Deprecation.RenderOptions?,
        val suppressDeprecation: Boolean,
        val fullTypeName: String
    )

    private fun options(msg: MessageType, ctx: Context): Any {
        val lengthAsOneLine =
            ctx.enclosingMessage.size * 4 +
                4 + // companion indentation
                63 + // `override fun deserialize(deserializer: KtMessageDeserializer): `
                msg.name.length +
                2 // ` {`

        return MessageOptions(
            wellKnownType = ctx.pkg == PPackage.PROTOKT,
            longDeserializer = lengthAsOneLine > idealMaxWidth
        )
    }

    private data class MessageOptions(
        val wellKnownType: Boolean,
        val longDeserializer: Boolean
    )
}
