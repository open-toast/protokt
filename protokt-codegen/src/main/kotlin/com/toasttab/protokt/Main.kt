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

import arrow.core.Some
import arrow.core.extensions.list.foldable.nonEmpty
import arrow.fx.IO
import arrow.syntax.collections.flatten
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos
import com.toasttab.protokt.codegen.ProtocolContext
import com.toasttab.protokt.codegen.generate
import com.toasttab.protokt.codegen.impl.STAnnotator
import com.toasttab.protokt.codegen.impl.STEffects
import com.toasttab.protokt.codegen.newFileName
import com.toasttab.protokt.codegen.toProtocol
import com.toasttab.protokt.ext.Protokt
import java.io.OutputStream
import kotlin.system.exitProcess

// Code generator entry point
fun main() =
    main(System.`in`.use { it.readBytes() }, System.out)

internal fun main(bytes: ByteArray, out: OutputStream) = IO {
    val req = toCodeGeneratorRequest(bytes)
    val params = parseParams(req)
    val filesToGenerate = req.fileToGenerateList.toSet()

    val files = req.protoFileList
        .filter { filesToGenerate.contains(it.name) }
        .map {
            val code = StringBuilder()
            val g = generate(
                toProtocol(ProtocolContext(it, params)),
                STAnnotator,
                STEffects,
                { t ->
                    t.printStackTrace(System.err)
                    exitProcess(-1)
                }
            )
            g { s -> code.append(s + "\n") }

            Some(
                PluginProtos.CodeGeneratorResponse.File
                    .newBuilder()
                    .setContent(code.toString())
                    .setName(it.newFileName(it.`package`))
                    .build()
            )
        }.flatten()

    if (files.nonEmpty()) {
        PluginProtos.CodeGeneratorResponse
            .newBuilder()
            .addAllFile(files)
            .build()
            .writeTo(out)
    }
}.unsafeRunSync()

private fun parseParams(req: PluginProtos.CodeGeneratorRequest) =
    if (req.parameter == null || req.parameter.isEmpty()) {
        emptyMap()
    } else {
        req.parameter
            .split(',')
            .map { it.substringBefore('=') to it.substringAfter('=', "") }
            .toMap()
    }

private fun toCodeGeneratorRequest(bytes: ByteArray) =
    PluginProtos.CodeGeneratorRequest.parseFrom(
        bytes,
        ExtensionRegistry.newInstance()
            .also { Protokt.registerAllExtensions(it) }
    )
