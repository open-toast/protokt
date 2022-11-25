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

import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature
import com.squareup.kotlinpoet.FileSpec
import com.toasttab.protokt.codegen.generate.GeneratorContext
import com.toasttab.protokt.codegen.generate.generateFile
import com.toasttab.protokt.codegen.impl.parseContents
import com.toasttab.protokt.codegen.impl.tidy
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
        .mapNotNull { fdp ->
            val context = GeneratorContext(fdp, params, filesToGenerate, req.protoFileList)
            val fileSpec = generateFile(parseContents(context))
            fileSpec?.let { response(it, context) }
        }

    if (files.isNotEmpty()) {
        CodeGeneratorResponse.newBuilder()
            .setSupportedFeatures(Feature.FEATURE_PROTO3_OPTIONAL.number.toLong())
            .addAllFile(files)
            .build()
            .writeTo(out)
    }
}

private fun response(fileSpec: FileSpec, context: GeneratorContext) =
    CodeGeneratorResponse.File
        .newBuilder()
        .setContent(tidy(fileSpec.toString(), context))
        .setName(fileSpec.name)
        .build()

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
