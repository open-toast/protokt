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

import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature
import com.squareup.kotlinpoet.FileSpec
import com.toasttab.protokt.codegen.impl.FileBuilder
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.toProtocol
import com.toasttab.protokt.ext.Protokt
import java.io.OutputStream
import kotlin.system.exitProcess

fun main() =
    try {
        main(System.`in`.use { it.readBytes() }, System.out)
    } catch (t: Throwable) {
        t.printStackTrace(System.err)
        exitProcess(-1)
    }

internal fun main(bytes: ByteArray, out: OutputStream) {
    val req = parseCodeGeneratorRequest(bytes)
    val params = parseParams(req)
    val filesToGenerate = req.fileToGenerateList.toSet()

    val files = req.protoFileList
        .filter { filesToGenerate.contains(it.name) }
        .mapNotNull {
            val fileSpec = generate(it, req.protoFileList, filesToGenerate, params)
            fileSpec?.let(::response)
        }

    if (files.isNotEmpty()) {
        CodeGeneratorResponse.newBuilder()
            .setSupportedFeatures(Feature.FEATURE_PROTO3_OPTIONAL.number.toLong())
            .addAllFile(files)
            .build()
            .writeTo(out)
    }
}

private fun generate(
    fdp: FileDescriptorProto,
    protoFileList: List<FileDescriptorProto>,
    filesToGenerate: Set<String>,
    params: Map<String, String>
): FileSpec? =
    FileBuilder.buildFile(
        toProtocol(
            ProtocolContext(
                fdp,
                params,
                filesToGenerate,
                protoFileList
            )
        )
    )

private fun response(fileSpec: FileSpec) =
    CodeGeneratorResponse.File
        .newBuilder()
        .setContent(fileSpec.toString().let(::tidy))
        .setName(fileSpec.name)
        .build()

// strips Explicit API mode declarations
// https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
private fun tidy(code: String) =
    code
        // https://stackoverflow.com/a/64970734
        .replace("public class ", "class ")
        .replace("public val ", "val ")
        .replace("public var ", "var ")
        .replace("public fun ", "fun ")
        .replace("public object ", "object ")
        .replace("public companion ", "companion ")
        .replace("public override ", "override ")
        .replace("public sealed ", "sealed ")
        .replace("public data ", "data ")
        // https://github.com/square/kotlinpoet/pull/932
        .replace("): Unit {", ") {")

private fun parseParams(req: CodeGeneratorRequest) =
    if (req.parameter == null || req.parameter.isEmpty()) {
        emptyMap()
    } else {
        req.parameter
            .split(',')
            .associate { it.substringBefore('=') to it.substringAfter('=', "") }
    }

private fun parseCodeGeneratorRequest(bytes: ByteArray) =
    CodeGeneratorRequest.parseFrom(
        bytes,
        ExtensionRegistry.newInstance()
            .also { Protokt.registerAllExtensions(it) }
    )
