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
import com.toasttab.protokt.codegen.template.Message.Message as MessageTemplate
import com.toasttab.protokt.codegen.template.Message.Message.MessageInfo
import com.toasttab.protokt.codegen.template.Message.Message.Options
import com.toasttab.protokt.codegen.template.Message.Message.ReflectInfo

internal object MessageAnnotator {
    val idealMaxWidth = 100

    fun annotateMessage(
        msg: Message,
        ctx: Context
    ) =
        if (msg.mapEntry) {
            annotateMapEntry(msg, ctx)
        } else {
            MessageTemplate.render(
                message = messageInfo(msg, ctx),
                properties = annotateProperties(msg, ctx),
                oneofs = annotateOneofs(msg, ctx),
                sizeof = annotateSizeof(msg, ctx),
                serialize = annotateSerializer(msg, ctx),
                deserialize = annotateDeserializer(msg, ctx),
                nested = nestedTypes(msg, ctx),
                reflect = reflectInfo(msg, ctx),
                options = options(msg, ctx)
            )
        }

    private fun nestedTypes(msg: Message, ctx: Context) =
        msg.nestedTypes.map { annotate(it, ctx) }

    private fun messageInfo(msg: Message, ctx: Context) =
        MessageInfo(
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
                    ctx.enclosing.firstOrNone().fold({ false }, { it == msg }))
        )

    private fun options(msg: Message, ctx: Context): Options {
        val lengthAsOneLine =
            ctx.enclosing.size * 4 +
                4 + // companion indentation
                63 + // `override fun deserialize(deserializer: KtMessageDeserializer): `
                msg.name.length +
                2 // ` {`

        return Options(
            wellKnownType = ctx.pkg == PPackage.PROTOKT,
            longDeserializer = lengthAsOneLine > idealMaxWidth
        )
    }

    private fun reflectInfo(msg: Message, ctx: Context) =
        if (ctx.desc.context.lite) {
            null
        } else {
            ReflectInfo(
                fileDescriptorObjectName = ctx.desc.context.fileDescriptorObjectName,
                index = msg.index,
                parentName = msg.parentName
            )
        }
}
