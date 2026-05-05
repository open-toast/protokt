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

import com.google.common.base.CaseFormat
import protokt.v1.enum
import protokt.v1.enumValue
import protokt.v1.google.protobuf.EnumDescriptorProto

internal class EnumParser(
    private val ctx: GeneratorContext,
    private val idx: Int,
    private val desc: EnumDescriptorProto,
    private val enclosingMessages: List<String>
) {
    fun toEnum(): Enum {
        val enumTypeNamePrefixToStrip =
            (CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, desc.name.orEmpty()) + '_')
                .takeIf {
                    desc.value.all { e ->
                        e.name.orEmpty().startsWith(it) && e.name.orEmpty().length > it.length && !e.name.orEmpty()[it.length].isDigit()
                    }
                }

        val simpleNames = enclosingMessages + desc.name.orEmpty()

        return Enum(
            values = desc.value.mapIndexed { enumIdx, t ->
                Enum.Value(
                    t.number ?: 0,
                    t.name.orEmpty(),
                    newEnumValueName(enumTypeNamePrefixToStrip, t.name.orEmpty()),
                    EnumValueOptions(
                        t.options ?: protokt.v1.google.protobuf.EnumValueOptions {},
                        t.options?.enumValue ?: protokt.v1.EnumValueOptions {}
                    ),
                    enumIdx
                )
            },
            index = idx,
            options = EnumOptions(
                desc.options ?: protokt.v1.google.protobuf.EnumOptions {},
                desc.options?.`enum` ?: protokt.v1.EnumOptions {}
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
