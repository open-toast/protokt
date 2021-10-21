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

package com.toasttab.protokt.codegen.descriptor

import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.toasttab.protokt.codegen.impl.bindMargin
import com.toasttab.protokt.codegen.impl.embed
import com.toasttab.protokt.codegen.impl.namedCodeBlock
import com.toasttab.protokt.codegen.impl.toParamName
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Protocol
import com.toasttab.protokt.codegen.protoc.TopLevelType

class FileDescriptorInfo(
    val fdp: TypeSpec,
    val properties: List<PropertySpec>
)

class FileDescriptorResolver
private constructor(
    private val protocol: Protocol
) {
    private val ctx = protocol.desc.context

    private fun resolveFileDescriptor(): FileDescriptorInfo? {
        if (ctx.lite || ctx.onlyGenerateGrpc) {
            return null
        }

        val dependencies = dependencies()

        val type =
            TypeSpec.objectBuilder(ctx.fileDescriptorObjectName)
                .addProperty(
                    PropertySpec.builder("descriptor", ClassName("com.toasttab.protokt", "FileDescriptor"))
                        .delegate(
                            namedCodeBlock(
                                """
                                    |lazy {
                                    |    val descriptorData = arrayOf(
                                    |%descriptorData:L
                                    |    )
                                    |
                                    |    %fileDescriptor:T.buildFrom(
                                    |        descriptorData,
                                    |        listOf(
                                    |${dependencyLines(dependencies)}
                                    |        )
                                    |    )
                                    |}
                                """.bindMargin(),
                                mapOf(
                                    "descriptorData" to descriptorLines(),
                                    "fileDescriptor" to ClassName("com.toasttab.protokt", "FileDescriptor")
                                ) + dependencies.associateBy { it.toParamName() }
                            )
                        ).build()
                )
                .build()

        val properties = enumDescriptorExtensionProperties() + messageDescriptorExtensionProperties()

        return FileDescriptorInfo(type, properties)
    }

    private fun descriptorLines() =
        fileDescriptorParts().joinToString(",\n") {
            "        " + it.joinToString(" +\n        ") { line -> line.embed() }
        }

    private fun fileDescriptorParts() =
        encodeFileDescriptor(
            clearJsonInfo(
                ctx.fdp.toBuilder()
                    .clearSourceCodeInfo()
                    .build()
            )
        )

    private fun dependencyLines(dependencies: List<TypeName>) =
        dependencies.joinToString(",\n") { "%${it.toParamName()}:T.descriptor" }

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

    private fun dependencies() =
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
                ClassName(
                    ctx.allPackagesByFileName.getValue(it).toString(),
                    ctx.allDescriptorClassNamesByDescriptorName.getValue(it)
                )
            }

    private fun enumDescriptorExtensionProperties() =
        protocol.types.flatMap { findEnums(emptyList(), it) }
            .map { (enum, containingTypes) ->
                PropertySpec.builder("descriptor", ClassName("com.toasttab.protokt", "EnumDescriptor"))
                    .receiver(enum.deserializerTypeName)
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
                    .receiver(msg.deserializerTypeName)
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
                                    .addMember("DEPRECATION".embed())
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
