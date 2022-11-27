/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt.codegen.util

import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.squareup.kotlinpoet.ClassName
import com.toasttab.protokt.ext.Protokt

class EnumParser(
    private val pkg: String,
    private val idx: Int,
    private val desc: EnumDescriptorProto,
    private val enclosingMessages: List<String>
) {
    fun toEnum(): Enum {
        val typeName = desc.name

        val enumTypeNamePrefixToStrip =
            (camelToUpperSnake(desc.name) + '_')
                .takeIf { prefix ->
                    desc.valueList.all { it.name.startsWith(prefix) }
                }

        return Enum(
            values = desc.valueList.mapIndexed { enumIdx, t ->
                Enum.Value(
                    t.number,
                    t.name,
                    newEnumValueName(enumTypeNamePrefixToStrip, t.name),
                    EnumValueOptions(
                        t.options,
                        t.options.getExtension(Protokt.enumValue)
                    ),
                    enumIdx
                )
            },
            index = idx,
            options = EnumOptions(
                desc.options,
                desc.options.getExtension(Protokt.enum_)
            ),
            className = ClassName(pkg, enclosingMessages + typeName),
            deserializerClassName = ClassName(pkg, enclosingMessages + typeName + "Deserializer")
        )
    }
}

private fun newEnumValueName(enumTypeNamePrefix: String?, name: String) =
    if (enumTypeNamePrefix != null) {
        name.removePrefix(enumTypeNamePrefix)
    } else {
        name
    }

private fun camelToUpperSnake(str: String) =
    str.replace(Regex("(?<=[a-z])([A-Z0-9])"), "_$1").uppercase()
