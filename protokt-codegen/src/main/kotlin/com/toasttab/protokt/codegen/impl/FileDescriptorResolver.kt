package com.toasttab.protokt.codegen.impl

import com.google.protobuf.DescriptorProtos
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.TypeDesc
import com.toasttab.protokt.codegen.template.Descriptor

class FileDescriptorInfo(
    val fdp: String,
    val imports: Set<String>
)

object FileDescriptorResolver {
    fun fileDescriptor(descs: List<TypeDesc>) =
        descs.firstOrNull()?.let {
            val aDesc = descs.first()
            fileDescriptor(aDesc.desc.context, kotlinPackage(aDesc))
        }

    private fun fileDescriptor(
        ctx: ProtocolContext,
        pkg: PPackage
    ): FileDescriptorInfo? {
        // If we're only generating services, the file descriptor will
        // already exist in the non-gRPC file
        if (ctx.onlyGenerateGrpc || ctx.lite) {
            return null
        }

        val imports = mutableSetOf<String>()

        val template =
            Descriptor.Descriptor.render(
                ctx.fileDescriptorObjectName,
                encodeFileDescriptor(
                    clearJsonInfo(
                        ctx.fdp.toBuilder()
                            .clearSourceCodeInfo()
                            .build()
                    )
                ),
                ctx.fdp.dependencyList
                    .filter {
                        // We don't generate anything for files without any of the
                        // following; e.g., a file containing only extensions.
                        ctx.allFilesByName.getValue(it).let { fdp ->
                            fdp.messageTypeCount +
                                fdp.enumTypeCount +
                                fdp.serviceCount > 0
                        }
                    }.map {
                        val depPkg = ctx.allPackagesByFileName.getValue(it)
                        val descriptorObjectName =
                            ctx.allDescriptorClassNamesByDescriptorName.getValue(it)
                        if (!depPkg.default && depPkg != pkg) {
                            imports.add("$depPkg.$descriptorObjectName")
                        }
                        descriptorObjectName
                    },
                ctx.fdp.dependencyCount > 1
            )
        return FileDescriptorInfo(template, imports)
    }

    private fun clearJsonInfo(fileDescriptorProto: DescriptorProtos.FileDescriptorProto) =
        fileDescriptorProto.toBuilder()
            .clearMessageType()
            .addAllMessageType(clearJsonInfo(fileDescriptorProto.messageTypeList))
            .build()

    private fun clearJsonInfo(descriptorProtos: Iterable<DescriptorProtos.DescriptorProto>): Iterable<DescriptorProtos.DescriptorProto> =
        descriptorProtos.map { dp ->
            dp.toBuilder()
                .clearField()
                .addAllField(
                    dp.fieldList
                        .map { fdp ->
                            fdp.toBuilder()
                                .clearJsonName()
                                .build()
                        }
                )
                .clearNestedType()
                .addAllNestedType(
                    clearJsonInfo(dp.nestedTypeList)
                )
                .build()
        }
}
