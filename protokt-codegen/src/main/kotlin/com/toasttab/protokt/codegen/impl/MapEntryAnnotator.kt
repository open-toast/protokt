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

class MapEntryAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateMapEntry() =
        resolveMapEntry(msg).let {
            Entry.render(
                name = msg.name,
                key = prop(it.key, it.key.unqualifiedTypeName),
                value = prop(it.value, it.value.typePClass.renderName(ctx.pkg))
            )
        }

    private fun prop(f: StandardField, type: String) =
        PropertyAnnotator(msg, ctx).let { annotator ->
            Entry.PropertyInfo(
                fieldType = type,
                messageType = f.type.toString(),
                type = annotator.annotateStandard(f),
                sizeof = sizeOfString(f, ctx, None),
                serialize = serializeString(f, ctx),
                defaultValue = annotator.defaultValue(f),
                deserialize =
                    DeserializerInfo(
                        tag = f.tag,
                        assignment = deserializeString(f, ctx)
                    )
            )
        }

    companion object {
        fun annotateMapEntry(msg: Message, ctx: Context) =
            MapEntryAnnotator(msg, ctx).annotateMapEntry()
    }
}
