/*
 * Copyright (c) 2019 Toast Inc.
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

import arrow.core.firstOrNone
import com.google.protobuf.DescriptorProtos
import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.TypeDesc
import com.toasttab.protokt.codegen.template.Descriptor

internal object STEffects {
    fun apply(descs: List<TypeDesc>, acc: (String) -> Unit) {
        val imports = collectPossibleImports(descs)

        val templateAndImports =
            if (descs.isNotEmpty()) {
                val aDesc = descs.first()
                fileDescriptor(aDesc.desc.context, kotlinPackage(aDesc))
            } else {
                FdpTemplateAndImports("", emptySet())
            }

        imports.addAll(templateAndImports.imports.map { Import.Literal(it) })
        imports.addAll(collectPossibleImports(descs))

        val header = StringBuilder()
        HeaderAccumulator.write(descs, imports) { header.append(it + "\n") }

        val body = StringBuilder()
        descs.forEach {
            it.type.code.map { s ->
                if (s.isNotBlank()) {
                    body.append(s + "\n")
                }
            }
        }

        if (body.isNotBlank()) {
            acc(header.toString())
            body.append(templateAndImports.fdpTemplate)
            acc(ImportReplacer.replaceImports(body.toString(), imports))
        }
    }

    private fun collectPossibleImports(descs: List<TypeDesc>) =
        descs.firstOrNone()
            .fold(
                { setOf() },
                {
                    ImportResolver(it.desc.context, kotlinPackage(it))
                        .resolveImports(descs)
                }
            ).toMutableSet()

    private class FdpTemplateAndImports(
        val fdpTemplate: String,
        val imports: Set<String>
    )

    private fun fileDescriptor(
        ctx: ProtocolContext,
        pkg: PPackage
    ): FdpTemplateAndImports {
        // If we're only generating services, the file descriptor will
        // already exist in the non-gRPC file
        if (ctx.onlyGenerateGrpc || ctx.lite) {
            return FdpTemplateAndImports("", emptySet())
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
        return FdpTemplateAndImports(template, imports)
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
