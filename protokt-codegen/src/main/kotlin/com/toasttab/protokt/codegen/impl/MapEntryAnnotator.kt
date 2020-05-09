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

import com.toasttab.protokt.codegen.impl.DeserializerAnnotator.Companion.annotateDeserializer
import com.toasttab.protokt.codegen.impl.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.SerializerAnnotator.Companion.annotateSerializer
import com.toasttab.protokt.codegen.impl.SizeofAnnotator.Companion.annotateSizeof
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.template.Entry.Entry
import com.toasttab.protokt.codegen.template.Entry.Entry.DeserializerInfo
import com.toasttab.protokt.codegen.template.Message.Message as MessageTemplate

class MapEntryAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateMapEntry(): String {
        val entryInfo = resolveMapEntry(msg)
        val desInfo = annotateDeserializer(msg, ctx)
        val sizeInfo = annotateSizeof(msg, ctx)
        val serInfo = annotateSerializer(msg, ctx)
        val propInfo = annotateProperties(msg, ctx)

        return Entry.render(
            name = msg.name,
            key =
                prop(
                    entryInfo.key,
                    entryInfo.key.unqualifiedTypeName,
                    sizeInfo,
                    serInfo,
                    desInfo,
                    propInfo
                ),
            value =
                prop(
                    entryInfo.value,
                    entryInfo.value.typePClass.renderName(ctx.pkg),
                    sizeInfo,
                    serInfo,
                    desInfo,
                    propInfo
                )
        )
    }

    private fun prop(
        f: StandardField,
        type: String,
        sizeofInfo: List<MessageTemplate.SizeofInfo>,
        serializerInfo: List<MessageTemplate.SerializerInfo>,
        deserializerInfo: List<MessageTemplate.DeserializerInfo>,
        propInfo: List<MessageTemplate.PropertyInfo>
    ) =
        Entry.PropertyInfo(
            propertyType = type,
            messageType = f.type.toString(),
            deserializeType = propInfo.single(f).deserializeType,
            sizeof = sizeofInfo.consequent(f),
            serialize = serializerInfo.consequent(f),
            defaultValue = propInfo.single(f).defaultValue,
            deserialize =
                deserializerInfo.single(f).let {
                    DeserializerInfo(
                        tag = it.tag,
                        assignment = it.assignment.value
                    )
                }
        )

    private fun <T : MessageTemplate.FieldInfo> List<T>.single(
        f: StandardField
    ) =
        single { it.name == f.fieldName }

    private fun List<MessageTemplate.FieldWriteInfo>.consequent(
        f: StandardField
    ) =
        single(f).conditionals.single().consequent

    companion object {
        fun annotateMapEntry(msg: Message, ctx: Context) =
            MapEntryAnnotator(msg, ctx).annotateMapEntry()
    }
}
