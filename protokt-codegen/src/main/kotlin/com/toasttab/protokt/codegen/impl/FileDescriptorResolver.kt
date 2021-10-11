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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Protocol
import com.toasttab.protokt.codegen.protoc.TopLevelType

class FileDescriptorInfo(
    val fdp: TypeSpec,
    val imports: Set<FileSpec.Builder.() -> Unit>,
    val properties: List<PropertySpec>
)

class FileDescriptorResolver
private constructor(
    private val protocol: Protocol
) {
    private val ctx = protocol.desc.context
    private val pkg = kotlinPackage(protocol)

    private fun resolveFileDescriptor(): FileDescriptorInfo? {
        if (ctx.lite || ctx.onlyGenerateGrpc) {
            return null
        }

        val dependenciesAndImports = dependencies()

        val type =
            TypeSpec.objectBuilder(ctx.fileDescriptorObjectName)
                .addProperty(
                    PropertySpec.builder("descriptor", ClassName("com.toasttab.protokt", "FileDescriptor"))
                        .delegate(
                            """
                                |lazy {
                                |  val descriptorData = arrayOf(
                                |%L
                                |  )
                                |
                                |  %T.buildFrom(
                                |    descriptorData,
                                |    listOf(
                                |${dependencyLines(dependenciesAndImports.dependencies)}
                                |    )
                                |  )
                                |}
                            """.trimMargin(),
                            descriptorLines(),
                            ClassName("com.toasttab.protokt", "FileDescriptor")
                        )
                        .build()
                )
                .build()

        val properties = enumDescriptorExtensionProperties() + messageDescriptorExtensionProperties()

        return FileDescriptorInfo(type, dependenciesAndImports.imports, properties)
    }

    private fun descriptorLines() =
        fileDescriptorParts().joinToString(",\n") {
            it.joinToString(" +\n") { line ->
                "\"" + line + "\""
            }
        }

    private fun fileDescriptorParts() =
        encodeFileDescriptor(
            clearJsonInfo(
                ctx.fdp.toBuilder()
                    .clearSourceCodeInfo()
                    .build()
            )
        )

    private fun dependencyLines(dependencies: List<String>) =
        dependencies.joinToString(",\n") { "$it.descriptor" }

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
                        imports.add { addImport("com.toasttab.protokt", "FileDescriptor") }
                    }
                    descriptorObjectName
                }

        return DependenciesAndImports(dependencies, imports)
    }

    private fun enumDescriptorExtensionProperties() =
        protocol.types.flatMap { findEnums(emptyList(), it) }
            .map { (enum, containingTypes) ->
                PropertySpec.builder("descriptor", ClassName("com.toasttab.protokt", "EnumDescriptor"))
                    .receiver(TypeVariableName((qualification(containingTypes) ?: "") + enum.name + ".Deserializer"))
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode(
                                "return " +
                                if (containingTypes.isEmpty()) {
                                    ctx.fileDescriptorObjectName + ".descriptor.enumTypes[${enum.index}]"
                                } else {
                                    (qualification(containingTypes) ?: "") + "descriptor.enumTypes[${enum.index}]"
                                }
                            )
                            .build()
                    )
                    .apply {
                        if (
                            containingTypes.any { it.options.default.deprecated } ||
                            enum.options.default.deprecated
                        ) {
                            addAnnotation(
                                AnnotationSpec.builder(Suppress::class)
                                    .addMember("\"DEPRECATION\"")
                                    .build()
                            )
                        }
                    }
                    .build()
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
        protocol.types.flatMap { findMessages(emptyList(), it) }
            .map { (msg, containingTypes) ->
                PropertySpec.builder("descriptor", ClassName("com.toasttab.protokt", "Descriptor"))
                    .receiver(TypeVariableName((qualification(containingTypes) ?: "") + msg.name + ".Deserializer"))
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode(
                                "return " +
                                if (containingTypes.isEmpty()) {
                                    ctx.fileDescriptorObjectName + ".descriptor.messageTypes[${msg.index}]"
                                } else {
                                    (qualification(containingTypes) ?: "") + "descriptor.nestedTypes[${msg.index}]"
                                }
                            )
                            .build()
                    )
                    .apply {
                        if (
                            containingTypes.any { it.options.default.deprecated } ||
                            msg.options.default.deprecated
                        ) {
                            addAnnotation(
                                AnnotationSpec.builder(Suppress::class)
                                    .addMember("\"DEPRECATION\"")
                                    .build()
                            )
                        }
                    }
                    .build()
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
        fun resolveFileDescriptor(protocol: Protocol) =
            if (protocol.types.isNotEmpty()) {
                FileDescriptorResolver(protocol).resolveFileDescriptor()
            } else {
                null
            }
    }
}
