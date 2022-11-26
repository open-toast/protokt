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

import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.toasttab.protokt.ext.Protokt
import com.toasttab.protokt.gradle.GENERATE_GRPC
import com.toasttab.protokt.gradle.KOTLIN_EXTRA_CLASSPATH
import com.toasttab.protokt.gradle.LINT_OUTPUT
import com.toasttab.protokt.gradle.LITE
import com.toasttab.protokt.gradle.ONLY_GENERATE_DESCRIPTORS
import com.toasttab.protokt.gradle.ONLY_GENERATE_GRPC
import com.toasttab.protokt.gradle.ProtoktExtension
import com.toasttab.protokt.gradle.RESPECT_JAVA_PACKAGE
import com.toasttab.protokt.util.getProtoktVersion
import java.net.URLDecoder
import kotlin.reflect.full.declaredMemberProperties

class GeneratorContext(
    val fdp: FileDescriptorProto,
    params: Map<String, String>,
    private val filesToGenerate: Set<String>,
    allFiles: List<FileDescriptorProto>
) {
    val classpath =
        params.getOrDefault(KOTLIN_EXTRA_CLASSPATH, "").split(";").map {
            URLDecoder.decode(it, "UTF-8")
        }

    val respectJavaPackage = respectJavaPackage(params)
    val generateGrpc = params.getOrDefault(GENERATE_GRPC)
    val onlyGenerateGrpc = params.getOrDefault(ONLY_GENERATE_GRPC)
    val lite = params.getOrDefault(LITE)
    val onlyGenerateDescriptors = params.getOrDefault(ONLY_GENERATE_DESCRIPTORS)
    val lintOutput = params.getOrDefault(LINT_OUTPUT)

    val fileName = fdp.name
    val fileOptions = fdp.fileOptions
    val version = getProtoktVersion(GeneratorContext::class)

    val proto2 = !fdp.hasSyntax() || fdp.syntax == "proto2"
    val proto3 = fdp.syntax == "proto3"

    val allPackagesByTypeName = packagesByTypeName(allFiles, respectJavaPackage(params))

    val allPackagesByFileName =
        allFiles.associate {
            it.name to
                resolvePackage(
                    it,
                    it.name !in filesToGenerate ||
                        respectJavaPackage(params)
                )
        }

    val allFilesByName = allFiles.associateBy { it.name }

    val allDescriptorClassNamesByDescriptorName =
        generateFdpObjectNames(allFiles, respectJavaPackage(params))

    val fileDescriptorObjectName =
        allDescriptorClassNamesByDescriptorName.getValue(fdp.name)
}

fun respectJavaPackage(params: Map<String, String>) =
    params.getOrDefault(RESPECT_JAVA_PACKAGE)

private fun Map<String, String>.getOrDefault(key: String): Boolean {
    val defaultExtension = ProtoktExtension()

    val defaultValue =
        defaultExtension::class.declaredMemberProperties
            .single { it.name == LOWER_UNDERSCORE.to(LOWER_CAMEL, key) }
            .call(defaultExtension) as Boolean

    return get(key)?.toBoolean() ?: defaultValue
}

val FileDescriptorProto.fileOptions
    get() =
        FileOptions(options, options.getExtension(Protokt.file))
