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

package com.toasttab.protokt.codegen.protoc

import arrow.core.None
import arrow.core.Option
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.gradle.GENERATE_GRPC
import com.toasttab.protokt.gradle.KOTLIN_EXTRA_CLASSPATH
import com.toasttab.protokt.gradle.ONLY_GENERATE_GRPC
import com.toasttab.protokt.gradle.RESPECT_JAVA_PACKAGE
import com.toasttab.protokt.util.getProtoktVersion

class ProtocolContext(
    val fdp: FileDescriptorProto,
    val allPackagesByTypeName: Map<String, PPackage>,
    params: Map<String, String>
) {
    val classpath = params.getOrDefault(KOTLIN_EXTRA_CLASSPATH, "").split(";")
    val respectJavaPackage = respectJavaPackage(params)
    val generateGrpc = params.getValue(GENERATE_GRPC).toBoolean()
    val onlyGenerateGrpc = params.getValue(ONLY_GENERATE_GRPC).toBoolean()

    val fileName = fdp.name
    val version = getProtoktVersion(ProtocolContext::class)
}

fun respectJavaPackage(params: Map<String, String>) =
    params.getValue(RESPECT_JAVA_PACKAGE).toBoolean()

fun ProtocolContext.ppackage(typeName: String) =
    allPackagesByTypeName.getValue(typeName)

fun ProtocolContext.findLocal(
    name: String,
    parent: Option<DescriptorProto> = None
): Option<DescriptorProto> {
    val (typeList, typeName) = parent.fold(
        {
            fdp.messageTypeList.filterNotNull() to
                name.removePrefix(".${fdp.`package`}.")
        },
        {
            it.nestedTypeList.filterNotNull() to name
        })

    typeName.indexOf('.').let { idx ->
        if (idx == -1) return Option.fromNullable(
            typeList.find { it.name == typeName })
        return findLocal(
            typeName.substring(idx + 1),
            Option.fromNullable(
                typeList.find { it.name == typeName.substring(0, idx) }))
    }
}
