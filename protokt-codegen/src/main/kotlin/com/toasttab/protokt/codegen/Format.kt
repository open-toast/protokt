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

package com.toasttab.protokt.codegen

internal object Keywords {
    val reserved =
        setOf(
            "Boolean",
            "Companion",
            "Double",
            "Float",
            "Int",
            "List",
            "Long",
            "Map",
            "String",
            "emptyList",
            "protokt",
            "messageSize",
            "deserialize",
            "serialize",
            "enumValue",
            "unknownFields",
            "res",
            "default")
    val kotlinReserved =
        setOf(
            "as",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "for",
            "fun",
            "if",
            "in",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "val",
            "var",
            "when",
            "while"
    )
}

internal fun snakeToCamel(str: String): String {
    var ret = str
    var lastIndex = -1
    while (true) {
        lastIndex =
            ret.indexOf('_', lastIndex + 1)
                .also {
                    if (it == -1) {
                        return ret
                    }
                }
        ret = ret.substring(0, lastIndex) +
            ret.substring(lastIndex + 1).capitalize()
    }
}

internal fun newTypeName(preferred: String, set: Set<String> = emptySet()): String {
    var name = snakeToCamel(preferred).capitalize()
    name = appendUnderscores(name, set)
    return name
}

internal fun newFieldName(preferred: String, set: Set<String>): String {
    var name = snakeToCamel(preferred).decapitalize()
    name = appendUnderscores(name, set)
    if (Keywords.kotlinReserved.contains(name)) {
        name = "`$name`"
    }
    return name
}

private fun appendUnderscores(orig: String, set: Set<String>): String {
    var name = orig
    while (set.contains(name) || Keywords.reserved.contains(name)) {
        name += '_'
    }
    return name
}

internal fun newEnumValueName(preferred: String, set: Set<String>): String {
    var name = preferred.toUpperCase()
    while (set.contains(name)) name += '_'
    return name
}

internal fun newFileName(pkg: String?, name: String): String {
    return (pkg?.replace('.', '/')?.plus('/') ?: "")
        .plus(name.substringAfterLast('/').removeSuffix(".proto"))
        .plus(".kt")
}
