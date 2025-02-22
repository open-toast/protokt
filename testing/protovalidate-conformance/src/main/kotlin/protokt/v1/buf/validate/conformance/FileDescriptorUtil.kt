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

import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.TypeRegistry

fun parse(fileDescriptors: Map<String, FileDescriptor>): Map<String, Descriptor> =
    fileDescriptors
        .values
        .flatMap { fileDescriptor ->
            fileDescriptor.messageTypes.map { messageType ->
                messageType.fullName to messageType
            }
        }
        .toMap()

fun createTypeRegistry(descriptorMap: Map<String, FileDescriptor>) =
    TypeRegistry.newBuilder().apply {
        descriptorMap.values.forEach {
            add(it.messageTypes)
        }
    }.build()

fun createExtensionRegistry(descriptorMap: Map<String, FileDescriptor>): ExtensionRegistry =
    ExtensionRegistry.newInstance().apply {
        descriptorMap.values.forEach(::registerExtensions)
    }

private fun ExtensionRegistry.registerExtensions(fileDescriptor: FileDescriptor) {
    registerExtensions(fileDescriptor.extensions)
    fileDescriptor.messageTypes.forEach(::registerExtensions)
}

private fun ExtensionRegistry.registerExtensions(descriptor: Descriptor) {
    registerExtensions(descriptor.extensions)
    descriptor.nestedTypes.forEach(::registerExtensions)
}

private fun ExtensionRegistry.registerExtensions(extensions: Iterable<FieldDescriptor>) {
    extensions.forEach {
        if (it.javaType == FieldDescriptor.JavaType.MESSAGE) {
            add(it, DynamicMessage.getDefaultInstance(it.messageType))
        } else {
            add(it)
        }
    }
}

fun parseFileDescriptors(fileDescriptorSet: FileDescriptorSet): Map<String, FileDescriptor> =
    fileDescriptorSet.fileList.fold(mutableMapOf()) { map, fileDescriptorProto ->
        map[fileDescriptorProto.getName()] =
            FileDescriptor.buildFrom(
                fileDescriptorProto,
                fileDescriptorProto.dependencyList.mapNotNull(map::get).toTypedArray(),
                false
            )
        map
    }
