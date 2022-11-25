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

package com.toasttab.protokt.codegen.util

import com.google.protobuf.DescriptorProtos.FileDescriptorProto

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

internal fun newFieldName(preferred: String) =
    // Ideally we'd avoid decapitalization but people have a tendency to
    // capitalize oneof defintions which will cause a clash between the field
    // name and the oneof sealed class definition. Can be avoided if the name
    // of the sealed class is modified when the field name is capitalized
    snakeToCamel(preferred).decapitalize()

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

internal fun fileName(contents: ProtoFileContents): String {
    val pkg = contents.info.kotlinPackage
    val name = contents.info.name
    val suffixes = mutableListOf<String>()
    if (contents.info.context.onlyGenerateDescriptors) {
        suffixes.add("_protokt_descriptors")
    } else if (contents.info.context.onlyGenerateGrpc) {
        suffixes.add("_protokt_grpc")
    }
    val dir = pkg.replace('.', '/') + '/'
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
