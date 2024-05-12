/*
 * Copyright (c) 2024 Toast, Inc.
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

package protokt.v1.buf.validate.conformance

import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FileDescriptor

fun parse(fileDescriptorSet: FileDescriptorSet): Map<String, Descriptor> =
    parseFileDescriptors(fileDescriptorSet)
        .values
        .flatMap { fileDescriptor ->
            fileDescriptor.messageTypes.map { messageType ->
                messageType.fullName to messageType
            }
        }
        .toMap()

private fun parseFileDescriptors(fileDescriptorSet: FileDescriptorSet): Map<String, FileDescriptor> {
    val fileDescriptorProtoMap = mutableMapOf<String, FileDescriptorProto>()
    for (fileDescriptorProto in fileDescriptorSet.fileList) {
        if (fileDescriptorProto.getName() in fileDescriptorProtoMap) {
            error("duplicate files found.")
        }
        fileDescriptorProtoMap[fileDescriptorProto.getName()] = fileDescriptorProto
    }

    return fileDescriptorSet.fileList.fold(mutableMapOf()) { map, fileDescriptorProto ->
        map[fileDescriptorProto.getName()] =
            FileDescriptor.buildFrom(
                fileDescriptorProto,
                fileDescriptorProto.dependencyList.mapNotNull(map::get).toTypedArray(),
                false
            )
        map
    }
}
