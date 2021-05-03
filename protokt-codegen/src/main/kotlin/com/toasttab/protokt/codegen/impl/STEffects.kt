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
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.algebra.Accumulator
import com.toasttab.protokt.codegen.algebra.Effects
import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.protoc.TypeDesc
import com.toasttab.protokt.codegen.template.Descriptor

internal object STEffects : Effects<AST<TypeDesc>, Accumulator<String>> {
    override fun invoke(astList: List<AST<TypeDesc>>, acc: (String) -> Unit) {
        val imports = collectPossibleImports(astList)

        val header = StringBuilder()
        HeaderAccumulator.write(astList, imports) { header.append(it + "\n") }

        val body = StringBuilder()
        astList.forEach {
            it.data.type.code.map { s ->
                if (s.isNotBlank()) {
                    body.append(s + "\n")
                }
            }
        }

        if (body.isNotBlank()) {
            acc(header.toString())
            body.append(fileDescriptor(astList.first().data.desc.context.fdp))
            acc(ImportReplacer.replaceImports(body.toString(), imports))
        }
    }

    private fun collectPossibleImports(astList: List<AST<TypeDesc>>): Set<Import> =
        astList.firstOrNone()
            .fold(
                { emptySet() },
                {
                    ImportResolver(it.data.desc.context, kotlinPackage(it))
                        .resolveImports(astList)
                }
            )

    private fun fileDescriptor(fileDescriptorProto: DescriptorProtos.FileDescriptorProto) =
        Descriptor.Descriptor.render(
            fileDescriptorProto.name
                .substringBefore(".proto")
                .replace("_", "_us_")
                .replace(".", "_dot_")
                .replace(";", "_semi_")
                .replace("[", "_sqb_")
                .replace("<", "_lt_")
                .replace(">", "_gt_")
                .replace(":", "_cl_")
                .replace('/', '_'),
            encodeFileDescriptor(
                clearJsonInfo(
                    fileDescriptorProto.toBuilder()
                        .clearSourceCodeInfo()
                        .build()
                )
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
}
