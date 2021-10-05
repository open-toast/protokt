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

import arrow.core.firstOrNone
import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.impl.Annotator.annotate
import com.toasttab.protokt.codegen.impl.Deprecation.enclosingDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.hasDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.DeserializerAnnotator.Companion.annotateDeserializer
import com.toasttab.protokt.codegen.impl.Implements.doesImplement
import com.toasttab.protokt.codegen.impl.Implements.implements
import com.toasttab.protokt.codegen.impl.MapEntryAnnotator.Companion.annotateMapEntry
import com.toasttab.protokt.codegen.impl.MessageDocumentationAnnotator.annotateMessageDocumentation
import com.toasttab.protokt.codegen.impl.OneofAnnotator.Companion.annotateOneofs
import com.toasttab.protokt.codegen.impl.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.impl.SerializerAnnotator.Companion.annotateSerializer
import com.toasttab.protokt.codegen.impl.SizeofAnnotator.Companion.annotateSizeof
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.template.Message.Message.MessageInfo
import com.toasttab.protokt.codegen.template.Message.Message.Options
import com.toasttab.protokt.codegen.template.Message.Message as MessageTemplate

class MessageAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    fun annotateMessage() =
        if (msg.mapEntry) {
            annotateMapEntry(msg, ctx)
        } else {
            MessageTemplate.render(
                message = messageInfo(),
                properties = annotateProperties(msg, ctx),
                oneofs = annotateOneofs(msg, ctx),
                sizeof = annotateSizeof(msg, ctx),
                serialize = annotateSerializer(msg, ctx),
                deserialize = annotateDeserializer(msg, ctx),
                nested = nestedTypes(),
                options = options()
            )
        }

    private fun nestedTypes() =
        msg.nestedTypes.map { annotate(it, ctx) }

    private fun messageInfo() =
        MessageInfo(
            name = msg.name,
            doesImplement = msg.doesImplement,
            implements = msg.implements,
            documentation = annotateMessageDocumentation(ctx),
            deprecation = deprecation(),
            suppressDeprecation = suppressDeprecation(),
            fullTypeName = msg.fullProtobufTypeName
        )

    private fun deprecation() =
        if (msg.options.default.deprecated) {
            renderOptions(
                msg.options.protokt.deprecationMessage
            )
        } else {
            null
        }

    private fun suppressDeprecation() =
        msg.hasDeprecation && (!enclosingDeprecation(ctx) || messageIsTopLevel())

    private fun messageIsTopLevel() =
        ctx.enclosing.firstOrNone().fold({ false }, { it == msg })

    private fun options(): Options {
        val lengthAsOneLine =
            ctx.enclosing.size * 4 +
                4 + // companion indentation
                63 + // `override fun deserialize(deserializer: KtMessageDeserializer): `
                msg.name.length +
                2 // ` {`

        return Options(
            wellKnownType = ctx.pkg == PPackage.PROTOKT,
            longDeserializer = lengthAsOneLine > IDEAL_MAX_WIDTH
        )
    }

    companion object {
        const val IDEAL_MAX_WIDTH = 100

        fun annotateMessage(msg: Message, ctx: Context) =
            MessageAnnotator(msg, ctx).annotateMessage()
    }
}
