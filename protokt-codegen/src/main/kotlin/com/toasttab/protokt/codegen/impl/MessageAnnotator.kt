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
import com.toasttab.protokt.codegen.template.Message
import com.toasttab.protokt.codegen.template.Message.MessageInfo
import com.toasttab.protokt.codegen.template.Message.Options

internal object MessageAnnotator {
    val idealMaxWidth = 100

    fun annotateMessage(
        msg: MessageType,
        ctx: Context
    ) =
        Message.prepare(
            message = annotateMessage2(msg, ctx),
            entry = annotateMapEntry(msg, ctx),
            params = annotateFields(msg, ctx),
            oneofs = annotateOneOfs(msg, ctx),
            sizeof = annotateSizeof(msg, ctx),
            serialize = annotateSerializer(msg, ctx),
            deserialize = annotateDeserializer(msg, ctx),
            options = options(msg, ctx)
        )

    private fun annotateMessage2(msg: MessageType, ctx: Context) =
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
                    ctx.enclosingMessage.firstOption()
                        .fold({ false }, { it == msg })),
            fullTypeName = msg.fullProtobufTypeName
        )

    private fun options(msg: MessageType, ctx: Context): Options {
        val lengthAsOneLine =
            ctx.enclosingMessage.size * 4 +
                4 + // companion indentation
                63 + // `override fun deserialize(deserializer: KtMessageDeserializer): `
                msg.name.length +
                2 // ` {`

        return Options(
            wellKnownType = ctx.pkg == PPackage.PROTOKT,
            longDeserializer = lengthAsOneLine > idealMaxWidth
        )
    }
}
