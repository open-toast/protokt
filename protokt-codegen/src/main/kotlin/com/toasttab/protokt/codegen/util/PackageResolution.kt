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

package com.toasttab.protokt.codegen.util

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.orElse
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName

const val rootGoogleProto = "google.protobuf"
const val googleProto = ".google.protobuf"

const val protoktPkg = "com.toasttab.protokt"
val protoktRtPkg = com.toasttab.protokt.rt.Bytes::class.asTypeName().packageName

fun packagesByTypeName(
    protoFileList: List<FileDescriptorProto>,
    respectJavaPackage: Boolean
): Map<String, String> {
    val packagesByTypeName = mutableMapOf<String, String>()

    protoFileList.forEach { fdp ->
        fdp.messageTypeList.forEach { dp ->
            gatherPackages(fdp, dp, emptyList(), respectJavaPackage, packagesByTypeName)
        }

        fdp.enumTypeList.forEach { edp ->
            packagesByTypeName[edp.foreignFullyQualifiedName(fdp)] =
                resolvePackage(fdp, respectJavaPackage)
        }
    }

    return packagesByTypeName
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
    packagesByTypeName: MutableMap<String, String>
) {
    val enclosingName =
        parents.joinToString(".") { it.name }.emptyOrFollowWithDot()

    packagesByTypeName[dp.fullyQualifiedName(fdp, enclosingName)] =
        resolvePackage(fdp, respectJavaPackage)

    dp.nestedTypeList.forEach {
        gatherPackages(fdp, it, parents + dp, respectJavaPackage, packagesByTypeName)
    }

    dp.enumTypeList.forEach { edp ->
        packagesByTypeName[edp.nestedFullyQualifiedName(fdp, enclosingName, dp)] =
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

internal fun resolvePackage(
    fdp: FileDescriptorProto,
    respectJavaPackage: Boolean
) =
    resolvePackage(fdp.fileOptions, fdp.`package`, respectJavaPackage)

private fun resolvePackage(
    fileOptions: FileOptions,
    protoPackage: String,
    respectJavaPackage: Boolean
) =
    fileOptions.protokt.kotlinPackage.emptyToNone()
        .orElse { javaPackage(respectJavaPackage, fileOptions) }
        .orElse { protoPackage.emptyToNone() }
        .map { overrideComGoogleProtobuf(it) }
        .getOrElse { "" }

private fun javaPackage(respectJavaPackage: Boolean, fileOptions: FileOptions) =
    if (respectJavaPackage) {
        fileOptions.default.javaPackage.emptyToNone()
    } else {
        None
    }

private fun overrideComGoogleProtobuf(type: String) =
    overrideGoogleProtobuf(type, "com.$rootGoogleProto")

private fun overrideGoogleProtobuf(type: String, prefix: String) =
    if (type.startsWith(prefix)) {
        protoktPkg + type.removePrefix(prefix)
    } else {
        type
    }

fun requalifyProtoType(ctx: GeneratorContext, typeName: String): ClassName {
    val withOverriddenGoogleProtoPackage =
        ClassName.bestGuess(
            overrideGoogleProtobuf(typeName.removePrefix("."), rootGoogleProto)
        )

    val withOverriddenReservedName =
        ClassName(
            withOverriddenGoogleProtoPackage.packageName,
            withOverriddenGoogleProtoPackage.simpleNames.dropLast(1) +
                withOverriddenGoogleProtoPackage.simpleName
        )

    return if (ctx.respectJavaPackage) {
        ClassName(
            ctx.allPackagesByTypeName.getValue(typeName),
            withOverriddenReservedName.simpleNames
        )
    } else {
        withOverriddenReservedName
    }
}
