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

package com.toasttab.protokt.v1.codegen.util

import com.google.common.base.CaseFormat
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.toasttab.protokt.v1.ProtoktProto

class EnumParser(
    private val ctx: GeneratorContext,
    private val idx: Int,
    private val desc: EnumDescriptorProto,
    private val enclosingMessages: List<String>
) {
    fun toEnum(): Enum {
        val enumTypeNamePrefixToStrip =
            (CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, desc.name) + '_')
                .takeIf { desc.valueList.all { e -> e.name.startsWith(it) } }

        val simpleNames = enclosingMessages + desc.name

        return Enum(
            values = desc.valueList.mapIndexed { enumIdx, t ->
                Enum.Value(
                    t.number,
                    t.name,
                    newEnumValueName(enumTypeNamePrefixToStrip, t.name),
                    EnumValueOptions(
                        t.options,
                        t.options.getExtension(ProtoktProto.enumValue)
                    ),
                    enumIdx
                )
            },
            index = idx,
            options = EnumOptions(
                desc.options,
                desc.options.getExtension(ProtoktProto.enum_)
            ),
            className = ctx.className(simpleNames),
            deserializerClassName = ctx.className(simpleNames + DESERIALIZER)
        )
    }
}

private fun newEnumValueName(enumTypeNamePrefix: String?, name: String) =
    if (enumTypeNamePrefix != null) {
        name.removePrefix(enumTypeNamePrefix)
    } else {
        name
    }
