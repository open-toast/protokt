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

package com.toasttab.protokt.codegen.impl

import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FileSpec
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.TopLevelType
import com.toasttab.protokt.codegen.protoc.TypeDesc
import com.toasttab.protokt.codegen.template.Descriptor.Descriptor
import com.toasttab.protokt.codegen.template.Descriptor.EnumDescriptorProperty
import com.toasttab.protokt.codegen.template.Descriptor.MessageDescriptorProperty

class FileDescriptorInfo(
    val fdp: String,
    val imports: Set<FileSpec.Builder.() -> Unit>
)

class FileDescriptorResolver
private constructor(
    private val descs: List<TypeDesc>
) {
    private val ctx = descs.first().desc.context
    private val pkg = kotlinPackage(descs.first())

    private fun resolveFileDescriptor(): FileDescriptorInfo? {
        if (ctx.lite || ctx.onlyGenerateGrpc) {
            return null
        }

        val dependenciesAndImports = dependencies()

        val template =
            Descriptor.render(
                ctx.fileDescriptorObjectName,
                fileDescriptorParts(),
                dependenciesAndImports.dependencies,
                ctx.fdp.dependencyCount > 1,
                enumDescriptorExtensionProperties() + messageDescriptorExtensionProperties()
            )
        return FileDescriptorInfo(template, dependenciesAndImports.imports)
    }

    private fun fileDescriptorParts() =
        encodeFileDescriptor(
            clearJsonInfo(
                ctx.fdp.toBuilder()
                    .clearSourceCodeInfo()
                    .build()
            )
        )

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

    private class DependenciesAndImports(
        val dependencies: List<String>,
        val imports: Set<FileSpec.Builder.() -> Unit>
    )

    private fun dependencies(): DependenciesAndImports {
        val imports = mutableSetOf<FileSpec.Builder.() -> Unit>()

        val dependencies =
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
                        imports.add { addImport(depPkg.toString(), descriptorObjectName) }
                    }
                    descriptorObjectName
                }

        return DependenciesAndImports(dependencies, imports)
    }

    private fun enumDescriptorExtensionProperties() =
        descs.flatMap { findEnums(emptyList(), it.type.rawType) }
            .map { (enum, containingTypes) ->
                EnumDescriptorProperty.render(
                    containingTypes.isEmpty(),
                    enum.name,
                    qualification(containingTypes),
                    ctx.fileDescriptorObjectName,
                    enum.index,
                    containingTypes.any { it.options.default.deprecated } ||
                        enum.options.default.deprecated
                )
            }

    private data class EnumInfo(
        val enum: Enum,
        val containingTypes: List<Message>
    )

    private fun findEnums(enclosingMessages: List<Message>, type: TopLevelType): List<EnumInfo> =
        when (type) {
            is Enum -> listOf(EnumInfo(type, enclosingMessages))
            is Message -> findEnums(enclosingMessages + type, type)
            else -> emptyList()
        }

    private fun findEnums(enclosingMessages: List<Message>, m: Message) =
        m.nestedTypes.flatMap { findEnums(enclosingMessages, it) }

    private fun messageDescriptorExtensionProperties() =
        descs.flatMap { findMessages(emptyList(), it.type.rawType) }
            .map { (msg, containingTypes) ->
                MessageDescriptorProperty.render(
                    containingTypes.isEmpty(),
                    msg.name,
                    qualification(containingTypes),
                    ctx.fileDescriptorObjectName,
                    msg.index,
                    containingTypes.any { it.options.default.deprecated } ||
                        msg.options.default.deprecated
                )
            }

    private data class MessageInfo(
        val msg: Message,
        val containingTypes: List<Message>
    )

    private fun findMessages(enclosingMessages: List<Message>, type: TopLevelType): List<MessageInfo> =
        when (type) {
            is Message ->
                if (type.mapEntry) {
                    emptyList()
                } else {
                    listOf(MessageInfo(type, enclosingMessages))
                } + findMessages(enclosingMessages + type, type)
            else -> emptyList()
        }

    private fun findMessages(enclosingMessages: List<Message>, m: Message) =
        m.nestedTypes.flatMap { findMessages(enclosingMessages, it) }

    private fun qualification(enclosingMessages: List<Message>) =
        if (enclosingMessages.isNotEmpty()) {
            enclosingMessages.joinToString(".") { it.name } + "."
        } else {
            null
        }

    companion object {
        fun resolveFileDescriptor(descs: List<TypeDesc>) =
            if (descs.isNotEmpty()) {
                FileDescriptorResolver(descs).resolveFileDescriptor()
            } else {
                null
            }
    }
}
