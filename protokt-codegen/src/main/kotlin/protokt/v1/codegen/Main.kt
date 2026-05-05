/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1.codegen

import com.squareup.kotlinpoet.FileSpec
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import protokt.v1.codegen.generate.generateFile
import protokt.v1.codegen.util.ErrorContext.withFileName
import protokt.v1.codegen.util.GeneratorContext
import protokt.v1.codegen.util.PluginParams
import protokt.v1.codegen.util.formatErrorMessage
import protokt.v1.codegen.util.generateGrpcKotlinStubs
import protokt.v1.codegen.util.parseFileContents
import protokt.v1.codegen.util.tidy
import protokt.v1.google.protobuf.Edition
import protokt.v1.google.protobuf.compiler.CodeGeneratorRequest
import protokt.v1.google.protobuf.compiler.CodeGeneratorResponse
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import kotlin.system.exitProcess

fun main() {
    KotlinLoggingConfiguration.logStartupMessage = false
    exitProcess(main(System.`in`, System.out, System.err))
}

internal fun main(`in`: InputStream, out: OutputStream, err: PrintStream) =
    try {
        main(`in`, out)
        0
    } catch (t: Throwable) {
        err.println(formatErrorMessage())
        t.printStackTrace(err)
        -1
    }

private fun main(`in`: InputStream, out: OutputStream) {
    val req = CodeGeneratorRequest.deserialize(`in`)
    val params = PluginParams(parseParams(req))
    val filesToGenerate = req.fileToGenerate.toSet()

    val files = req.protoFile
        .filter { filesToGenerate.contains(it.name) }
        .mapNotNull { fdp ->
            val context = GeneratorContext(fdp, params, req.protoFile)
            val fileSpec = withFileName(fdp.name.orEmpty()) { generateFile(parseFileContents(context)) }
            fileSpec?.let { response(it, context) }
        }

    val grpcKotlinFiles = generateGrpcKotlinStubs(params, req)

    CodeGeneratorResponse {
        supportedFeatures =
            (
                CodeGeneratorResponse.Feature.PROTO3_OPTIONAL.value or
                    CodeGeneratorResponse.Feature.SUPPORTS_EDITIONS.value
                ).toULong()

        minimumEdition = Edition.EDITION_PROTO2.value
        maximumEdition = Edition.EDITION_2023.value
        file = files + grpcKotlinFiles
    }.serialize(out)
}

private fun response(fileSpec: FileSpec, context: GeneratorContext) =
    CodeGeneratorResponse.File {
        content = tidy(fileSpec.toString(), context.formatOutput)
        name = fileSpec.name
    }

private fun parseParams(req: CodeGeneratorRequest) =
    req.parameter.orEmpty().let { param ->
        if (param.isEmpty()) {
            emptyMap()
        } else {
            param
                .split(',')
                .associate { it.substringBefore('=') to it.substringAfter('=', "") }
        }
    }
