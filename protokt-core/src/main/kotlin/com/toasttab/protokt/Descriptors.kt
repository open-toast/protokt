package com.toasttab.protokt

import com.toasttab.protokt.rt.finishList

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
        }.let { finishList(it) }

    val name
        get() = proto.name

    private companion object {
        fun computeFullName(
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
    }
}

class FileDescriptor(
    val proto: FileDescriptorProto,
    val dependencies: List<FileDescriptor>
) {
    companion object {
        fun fromData(
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
