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
import com.toasttab.protokt.codegen.impl.PropertyAnnotator.Companion.annotateProperty
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
    private fun annotateMapEntry() =
        resolveMapEntry(msg).let { entryInfo ->
            annotateDeserializer(msg, ctx).let { desInfo ->
                annotateSizeof(msg, ctx).let { sizeInfo ->
                    annotateSerializer(msg, ctx).let { serInfo ->
                        Entry.render(
                            name = msg.name,
                            key =
                                prop(
                                    entryInfo.key,
                                    entryInfo.key.unqualifiedTypeName,
                                    sizeInfo,
                                    serInfo,
                                    desInfo
                                ),
                            value =
                                prop(
                                    entryInfo.value,
                                    entryInfo.value.typePClass.renderName(ctx.pkg),
                                    sizeInfo,
                                    serInfo,
                                    desInfo
                                )
                        )
                    }
                }
            }
        }

    private fun prop(
        f: StandardField,
        type: String,
        sizeofInfo: List<MessageTemplate.SizeofInfo>,
        serializerInfo: List<MessageTemplate.SerializerInfo>,
        deserializerInfo: List<MessageTemplate.DeserializerInfo>
    ) =
        annotateProperty(f, msg, ctx).let { propInfo ->
            Entry.PropertyInfo(
                propertyType = type,
                messageType = f.type.toString(),
                deserializeType = propInfo.deserializeType,
                sizeof = sizeofInfo.consequent(f),
                serialize = serializerInfo.consequent(f),
                defaultValue = propInfo.defaultValue,
                deserialize =
                    deserializerInfo.single {
                        it.assignment.fieldName == f.fieldName
                    }.let {
                        DeserializerInfo(
                            tag = it.tag,
                            assignment = it.assignment.value
                        )
                    }
            )
        }

    private fun List<MessageTemplate.FieldWriteInfo>.consequent(
        f: StandardField
    ) =
        single { it.fieldName == f.fieldName }
            .conditionals
            .single()
            .consequent

    companion object {
        fun annotateMapEntry(msg: Message, ctx: Context) =
            MapEntryAnnotator(msg, ctx).annotateMapEntry()
    }
}
