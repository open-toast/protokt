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

package com.toasttab.protokt.codegen.protoc

import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.toasttab.protokt.codegen.impl.resolvePackage
import com.toasttab.protokt.codegen.util.capitalize
import com.toasttab.protokt.codegen.util.decapitalize

internal object Keywords {
    val reserved =
        setOf(
            "Boolean",
            "Double",
            "Float",
            "Int",
            "List",
            "Long",
            "Map",
            "String",
            "Unit",
            "Enum",
            "Int32",
            "Int64",
            "Fixed32",
            "Fixed64",
            "SFixed32",
            "SFixed64",
            "SInt32",
            "SInt64",
            "UInt32",
            "UInt64",
            "Bytes",
            "Deserializer",
            "KtDeserializer",
            "KtSerializer",
            "KtMessageSerializer",
            "Tag",
            "deserializer",
            "serializer",
            "messageSize",
            "emptyList"
        )

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

internal fun newTypeNameFromCamel(
    preferred: String,
    set: Set<String> = emptySet()
) =
    newTypeNameFromPascal(snakeToCamel(preferred).capitalize(), set)

internal fun newTypeNameFromPascal(
    preferred: String,
    set: Set<String> = emptySet()
) =
    appendUnderscores(preferred, set)

internal fun newFieldName(preferred: String, set: Set<String>): String {
    var name = snakeToCamel(preferred).decapitalize()
    name = appendUnderscores(name, set)
    if (name in Keywords.kotlinReserved) {
        name = "`$name`"
    }
    return name
}

private fun appendUnderscores(orig: String, set: Set<String>): String {
    var name = orig
    while (name in set || name in Keywords.reserved) {
        name += '_'
    }
    return name
}

internal fun newEnumValueName(
    enumTypeNamePrefix: String?,
    preferred: String,
    set: Set<String>
): String {
    var name = preferred

    if (enumTypeNamePrefix != null) {
        name = name.removePrefix(enumTypeNamePrefix)
    }

    while (name in set) {
        name += '_'
    }

    return name
}

internal fun camelToUpperSnake(str: String) =
    str.replace(Regex("(?<=[a-z])([A-Z0-9])"), "_$1").uppercase()

internal fun fileName(protocol: Protocol): String {
    val pkg = protocol.desc.kotlinPackage
    val name = protocol.desc.name
    val suffixes = mutableListOf<String>()
    if (protocol.desc.context.onlyGenerateDescriptors) {
        suffixes.add("_protokt_descriptors")
    } else if (protocol.desc.context.onlyGenerateGrpc) {
        suffixes.add("_protokt_grpc")
    }
    val dir = pkg.toString().replace('.', '/') + '/'
    val fileNameBase = name.substringAfterLast('/').removeSuffix(".proto")

    return dir + fileNameBase + suffixes.joinToString("") + ".kt"
}

internal fun generateFdpObjectNames(
    files: List<FileDescriptorProto>,
    respectJavaPackage: Boolean
): Map<String, String> {
    val names = mutableMapOf<String, String>()

    val usedNames =
        files.flatMapTo(mutableSetOf()) { fdp ->
            files.filter {
                resolvePackage(it, respectJavaPackage) ==
                    resolvePackage(fdp, respectJavaPackage)
            }.flatMapTo(mutableSetOf()) {
                fdp.enumTypeList.map { e -> e.name } +
                    fdp.messageTypeList.map { m -> m.name } +
                    fdp.serviceList.map { s -> s.name }
            }
        }

    files.forEach { fdp ->
        var name =
            fdp.fileOptions.protokt.fileDescriptorObjectName.takeIf { it.isNotEmpty() }
                ?: fdp.fileOptions.default.javaOuterClassname.takeIf { it.isNotEmpty() }
                ?: fdp.name
                    .substringBefore(".proto")
                    .substringAfterLast('/')
                    .let(::snakeToCamel)
                    .capitalize()

        while (name in usedNames) {
            name += "_"
        }

        usedNames.add(name)
        names[fdp.name] = name
    }

    return names
}
