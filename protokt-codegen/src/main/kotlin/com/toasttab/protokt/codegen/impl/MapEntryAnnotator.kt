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

import arrow.core.None
import com.toasttab.protokt.codegen.impl.DeserializerAnnotator.Companion.deserializeString
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.SerializerAnnotator.Companion.serializeString
import com.toasttab.protokt.codegen.impl.SizeofAnnotator.Companion.sizeOfString
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.template.Entry.Entry
import com.toasttab.protokt.codegen.template.Entry.Entry.DeserializerInfo
import com.toasttab.protokt.codegen.template.Entry.Entry.EntryInfo

class MapEntryAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateMapEntry() =
        resolveMapEntry(msg, ctx).let { (k, v) ->
            Entry.render(
                name = msg.name,
                entry = EntryInfo(k, v),
                properties = annotateProperties(),
                serialize = annotateSerializer(),
                deserialize = annotateDeserializer(),
                sizeof = annotateSizeof()
            )
        }

    private fun annotateProperties() =
        PropertyAnnotator(msg, ctx).let { annotator ->
            msg.fields
                .map { it as StandardField }
                .map {
                    Entry.PropertyInfo(
                        name = it.fieldName,
                        type = annotator.annotateStandard(it),
                        messageType = it.type.toString(),
                        defaultValue = annotator.defaultValue(it)
                    )
                }
        }

    private fun annotateSerializer() =
        msg.fields
            .map { it as StandardField }
            .map { serializeString(it, ctx) }

    private fun annotateDeserializer() =
        msg.fields
            .map { it as StandardField }
            .map {
                DeserializerInfo(
                    tag = it.tag,
                    assignment =
                        DeserializerInfo.Assignment(
                            it.fieldName,
                            deserializeString(it, ctx)
                        )
                )
            }

    private fun annotateSizeof() =
        msg.fields
            .map { it as StandardField }
            .map { sizeOfString(it, ctx, None) }

    companion object {
        fun annotateMapEntry(msg: Message, ctx: Context) =
            MapEntryAnnotator(msg, ctx).annotateMapEntry()
    }
}
