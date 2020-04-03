/*
 * Copyright (c) 2020 Toast Inc.
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
import arrow.core.getOrElse
import arrow.core.orElse
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.toasttab.protokt.codegen.FileOptions
import com.toasttab.protokt.codegen.fileOptions
import com.toasttab.protokt.codegen.impl.STAnnotator.rootGoogleProto
import com.toasttab.protokt.codegen.model.PPackage

fun packagesByTypeName(
    req: CodeGeneratorRequest,
    respectJavaPackage: Boolean
): Map<String, PPackage> {
    val map = mutableMapOf<String, PPackage>()

    req.protoFileList.forEach { fdp ->
        fdp.messageTypeList.forEach { dp ->
            gatherPackages(fdp, dp, emptyList(), respectJavaPackage, map)
        }

        fdp.enumTypeList.forEach { edp ->
            map[edp.foreignFullyQualifiedName(fdp)] =
                resolvePackage(fdp, respectJavaPackage)
        }
    }

    return map
}

private fun EnumDescriptorProto.foreignFullyQualifiedName(
    fdp: FileDescriptorProto
) =
    "${fdp.fullQualification}.$name"

private fun gatherPackages(
    fdp: FileDescriptorProto,
    dp: DescriptorProto,
    parents: List<DescriptorProto>,
    respectJavaPackage: Boolean,
    map: MutableMap<String, PPackage>
) {
    val enclosingName =
        parents.joinToString(".") { it.name }.emptyOrFollowWithDot()

    map[dp.fullyQualifiedName(fdp, enclosingName)] =
        resolvePackage(fdp, respectJavaPackage)

    dp.nestedTypeList.forEach {
        gatherPackages(fdp, it, parents + dp, respectJavaPackage, map)
    }

    dp.enumTypeList.forEach { edp ->
        map[edp.nestedFullyQualifiedName(fdp, enclosingName, dp)] =
            resolvePackage(fdp, respectJavaPackage)
    }
}

private fun DescriptorProto.fullyQualifiedName(
    fdp: FileDescriptorProto,
    enclosingName: String
) =
    "${fdp.fullQualification}.$enclosingName$name"

private fun EnumDescriptorProto.nestedFullyQualifiedName(
    fdp: FileDescriptorProto,
    enclosingName: String,
    dp: DescriptorProto
) =
    "${fdp.fullQualification}.$enclosingName${dp.name}.$name"


private val FileDescriptorProto.fullQualification
    get() = `package`.emptyOrPrecedeWithDot()

private fun String.emptyOrPrecedeWithDot() =
    emptyToNone().fold({ "" }, { ".$it" })

private fun String.emptyOrFollowWithDot() =
    emptyToNone().fold({ "" }, { "$it." })

private fun resolvePackage(
    fdp: FileDescriptorProto,
    respectJavaPackage: Boolean
) =
    resolvePackage(fdp.fileOptions, fdp.`package`, respectJavaPackage)

internal fun resolvePackage(
    fileOptions: FileOptions,
    protoPackage: String,
    respectJavaPackage: Boolean
) =
    fileOptions.protokt.kotlinPackage.emptyToNone()
        .orElse { javaPackage(respectJavaPackage, fileOptions) }
        .orElse { protoPackage.emptyToNone() }
        .map { overrideComGoogleProtobuf(it) }
        .getOrElse { PPackage.DEFAULT }

private fun javaPackage(respectJavaPackage: Boolean, fileOptions: FileOptions) =
    if (respectJavaPackage) {
        fileOptions.default.javaPackage.emptyToNone()
    } else {
        None
    }

private fun overrideComGoogleProtobuf(type: String) =
    PPackage.fromString(overrideGoogleProtobuf(type, "com.$rootGoogleProto"))

internal fun overrideGoogleProtobuf(type: String, prefix: String) =
    if (type.startsWith(prefix)) {
        rootPkg.toString() + type.removePrefix(prefix)
    } else {
        type
    }
