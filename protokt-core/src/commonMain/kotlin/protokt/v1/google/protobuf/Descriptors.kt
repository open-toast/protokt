/*
 * Copyright (c) 2021 Toast, Inc.
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

package protokt.v1.google.protobuf

import protokt.v1.Collections.unmodifiableList
import kotlin.jvm.JvmStatic

class FileDescriptor private constructor(
    val proto: FileDescriptorProto,
    val dependencies: List<FileDescriptor>
) {
    val messageTypes =
        proto.messageType.mapIndexed { idx, proto ->
            Descriptor(proto, this, idx)
        }.let(::unmodifiableList)

    val enumTypes =
        proto.enumType.mapIndexed { idx, proto ->
            EnumDescriptor(proto, this, idx)
        }.let(::unmodifiableList)

    val services =
        proto.service.mapIndexed { idx, proto ->
            ServiceDescriptor(proto, this, idx)
        }.let(::unmodifiableList)

    companion object {
        @JvmStatic
        fun buildFrom(
            data: Array<String>,
            dependencies: List<FileDescriptor>
        ): FileDescriptor {
            val descriptorBytes = ByteArray(data.sumOf { it.length })
            var idx = 0

            data.forEach { part ->
                part.forEach { char ->
                    descriptorBytes[idx++] = char.code.toByte()
                }
            }

            return FileDescriptor(
                FileDescriptorProto.deserialize(descriptorBytes),
                unmodifiableList(dependencies)
            )
        }
    }
}

class Descriptor private constructor(
    val proto: DescriptorProto,
    val file: FileDescriptor,
    val index: Int,
    val fullName: String
) {
    internal constructor(
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
        }.let(::unmodifiableList)

    val enumTypes =
        proto.enumType.mapIndexed { idx, proto ->
            EnumDescriptor(proto, file, idx)
        }.let(::unmodifiableList)
}

class EnumDescriptor internal constructor(
    val proto: EnumDescriptorProto,
    val file: FileDescriptor,
    val index: Int
) {
    val fullName = computeFullName(file, null, proto.name.orEmpty())
}

class ServiceDescriptor internal constructor(
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
