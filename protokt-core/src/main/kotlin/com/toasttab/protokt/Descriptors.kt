/*
 * Copyright (c) 2021 Toast Inc.
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

package com.toasttab.protokt

import com.toasttab.protokt.rt.finishList
import protokt.com.google.protobuf.DescriptorProto
import protokt.com.google.protobuf.EnumDescriptorProto
import protokt.com.google.protobuf.FileDescriptorProto
import protokt.com.google.protobuf.ServiceDescriptorProto

class FileDescriptor(
    val proto: FileDescriptorProto,
    val dependencies: List<FileDescriptor>
) {
    val messageTypes =
        proto.messageType.mapIndexed { idx, proto ->
            Descriptor(proto, this, idx)
        }.let(::finishList)

    val enumTypes =
        proto.enumType.mapIndexed { idx, proto ->
            EnumDescriptor(proto, this, idx)
        }.let(::finishList)

    val services =
        proto.service.mapIndexed { idx, proto ->
            ServiceDescriptor(proto, this, idx)
        }.let(::finishList)

    companion object {
        fun buildFrom(
            data: Array<String>,
            dependencies: List<FileDescriptor>
        ): FileDescriptor {
            val descriptorBytes = ByteArray(data.sumBy { it.length })
            var idx = 0

            data.forEach { part ->
                part.forEach { char ->
                    descriptorBytes[idx++] = char.toByte()
                }
            }

            return FileDescriptor(
                FileDescriptorProto.deserialize(descriptorBytes),
                finishList(dependencies)
            )
        }
    }
}

class Descriptor(
    val proto: DescriptorProto,
    val file: FileDescriptor,
    val index: Int,
    val fullName: String
) {
    constructor(
        proto: DescriptorProto,
        file: FileDescriptor,
        index: Int,
        parent: Descriptor? = null
    ) : this(
        proto,
        file,
        index,
        computeFullName(file, parent, proto.name.orEmpty())
    )

    val nestedTypes =
        proto.nestedType.mapIndexed { idx, proto ->
            Descriptor(proto, file, idx, this)
        }.let(::finishList)

    val enumTypes =
        proto.enumType.mapIndexed { idx, proto ->
            EnumDescriptor(proto, file, idx)
        }.let(::finishList)
}

class EnumDescriptor(
    val proto: EnumDescriptorProto,
    val file: FileDescriptor,
    val index: Int
)

class ServiceDescriptor(
    val proto: ServiceDescriptorProto,
    val file: FileDescriptor,
    val index: Int
) {
    val fullName = computeFullName(file, null, proto.name.orEmpty())
}

private fun computeFullName(
    file: FileDescriptor,
    parent: Descriptor?,
    name: String
): String {
    if (parent != null) {
        return "${parent.fullName}.$name"
    }

    file.proto.`package`?.takeIf { it.isNotEmpty() }?.let {
        return "$it.$name"
    }

    return name
}
