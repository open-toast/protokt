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

package com.toasttab.protokt

import arrow.core.None
import arrow.core.Some
import arrow.core.extensions.list.foldable.nonEmpty
import arrow.syntax.collections.flatten
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import com.toasttab.protokt.codegen.generate
import com.toasttab.protokt.codegen.impl.STAnnotator
import com.toasttab.protokt.codegen.impl.STEffects
import com.toasttab.protokt.codegen.impl.packagesByTypeName
import com.toasttab.protokt.codegen.impl.resolvePackage
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.fileName
import com.toasttab.protokt.codegen.protoc.respectJavaPackage
import com.toasttab.protokt.codegen.protoc.toProtocol
import com.toasttab.protokt.ext.Protokt
import java.io.OutputStream
import kotlin.system.exitProcess

fun main() =
    main(System.`in`.use { it.readBytes() }, System.out)

internal fun main(bytes: ByteArray, out: OutputStream) {
    val req = parseCodeGeneratorRequest(bytes)
    val params = parseParams(req)
    val filesToGenerate = req.fileToGenerateList.toSet()

    val files = req.protoFileList
        .filter { filesToGenerate.contains(it.name) }
        .map { response(it, generate(it, req.protoFileList, params), params) }
        .flatten()

    if (files.nonEmpty()) {
        CodeGeneratorResponse.newBuilder()
            .addAllFile(files)
            .build()
            .writeTo(out)
    }
}

private fun generate(
    fdp: FileDescriptorProto,
    protoFileList: List<FileDescriptorProto>,
    params: Map<String, String>
): String {
    val code = StringBuilder()
    val g = generate(
        toProtocol(
            ProtocolContext(
                fdp,
                packagesByTypeName(
                    protoFileList,
                    respectJavaPackage(params)
                ),
                params
            )
        ),
        STAnnotator,
        STEffects,
        { t ->
            t.printStackTrace(System.err)
            exitProcess(-1)
        }
    )
    g { s -> code.append(s) }

    return code.toString()
}

private fun response(
    fdp: FileDescriptorProto,
    code: String,
    params: Map<String, String>
) =
    if (code.isNotBlank()) {
        Some(
            CodeGeneratorResponse.File
                .newBuilder()
                .setContent(code)
                .setName(
                    fileName(
                        resolvePackage(fdp, respectJavaPackage(params)),
                        fdp.name
                    )
                ).build()
        )
    } else {
        None
    }

private fun parseParams(req: CodeGeneratorRequest) =
    if (req.parameter == null || req.parameter.isEmpty()) {
        emptyMap()
    } else {
        req.parameter
            .split(',')
            .map { it.substringBefore('=') to it.substringAfter('=', "") }
            .toMap()
    }

private fun parseCodeGeneratorRequest(bytes: ByteArray) =
    CodeGeneratorRequest.parseFrom(
        bytes,
        ExtensionRegistry.newInstance()
            .also { Protokt.registerAllExtensions(it) }
    )
