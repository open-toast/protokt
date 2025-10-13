/*
 * Copyright (c) 2020 Toast, Inc.
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

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.toasttab.protokt.v1.ProtoktProtos
import protokt.v1.gradle.PROTOKT_VERSION

internal class GeneratorContext(
    val fdp: FileDescriptorProto,
    params: PluginParams,
    allFiles: List<FileDescriptorProto>
) {
    val classLookup = params.classLookup
    val generateTypes = params.generateTypes
    val generateDescriptors = params.generateDescriptors
    val generateGrpcDescriptors = params.generateGrpcDescriptors
    val generateGrpcKotlinStubs = params.generateGrpcKotlinStubs
    val formatOutput = params.formatOutput
    val kotlinTarget = params.kotlinTarget

    val protoktVersion = PROTOKT_VERSION

    val allPackagesByFileName = packagesByFileName(allFiles)
    val allFilesByName = allFiles.associateBy { it.name }
    val allDescriptorClassNamesByFileName = generateFdpObjectNames(allFiles)

    val fileOptions = fdp.fileOptions
    val fileDescriptorObjectName = allDescriptorClassNamesByFileName.getValue(fdp.name)
    val kotlinPackage = allPackagesByFileName.getValue(fdp.name)

    val proto2 = !fdp.hasSyntax() || fdp.syntax == "proto2"
    val proto3 = fdp.syntax == "proto3"
    val edition2023 = fdp.edition == DescriptorProtos.Edition.EDITION_2023
}

val FileDescriptorProto.fileOptions
    get() = FileOptions(options, options.getExtension(ProtoktProtos.file))

private fun generateFdpObjectNames(files: List<FileDescriptorProto>): Map<String, String> =
    files.associate { fdp ->
        Pair(
            fdp.name,
            fdp.fileOptions.protokt.fileDescriptorObjectName.takeIf { it.isNotEmpty() }
                ?: (fdp.name.substringBefore(".proto").substringAfterLast('/') + "_file_descriptor")
        )
    }
