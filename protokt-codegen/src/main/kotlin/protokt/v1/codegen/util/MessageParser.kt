/*
 * Copyright (c) 2022 Toast, Inc.
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

package protokt.v1.codegen.util

import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.toasttab.protokt.v1.ProtoktProtos

internal class MessageParser(
    private val ctx: GeneratorContext,
    private val idx: Int,
    private val desc: DescriptorProto,
    private val enclosingMessages: List<String>,
    private val keyWrap: String?,
    private val valueWrap: String?
) {
    fun toMessage(): Message {
        val fieldList = FieldParser(ctx, desc, enclosingMessages, keyWrap, valueWrap).toFields()
        val simpleNames = enclosingMessages + desc.name
        return Message(
            fields = fieldList.sortedBy {
                when (it) {
                    is StandardField -> it
                    is Oneof -> it.fields.first()
                }.number
            },
            nestedTypes = FileContentParser(
                ctx,
                desc.enumTypeList,
                desc.nestedTypeList,
                emptyList(),
                simpleNames
            ).parseContents(),
            mapEntry = desc.options.mapEntry,
            options = MessageOptions(
                desc.options,
                desc.options.getExtension(ProtoktProtos.class_)
            ),
            index = idx,
            fullProtobufTypeName = "${ctx.fdp.`package`}.${simpleNames.joinToString(".")}",
            className = ctx.className(simpleNames),
            deserializerClassName = ctx.className(simpleNames + DESERIALIZER)
        )
    }
}
