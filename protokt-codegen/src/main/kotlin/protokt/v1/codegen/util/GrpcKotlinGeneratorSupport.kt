/*
 * Copyright (c) 2023 Toast, Inc.
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

package protokt.v1.codegen.util

import io.grpc.kotlin.generator.GeneratorRunner
import protokt.v1.google.protobuf.compiler.CodeGeneratorRequest
import protokt.v1.google.protobuf.compiler.CodeGeneratorResponse
import protokt.v1.reflect.resolvePackage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal fun generateGrpcKotlinStubs(
    params: PluginParams,
    request: CodeGeneratorRequest
): List<CodeGeneratorResponse.File> =
    if (
        params.kotlinTarget.treatTargetAsJvm &&
        params.generateGrpcKotlinStubs
    ) {
        val out = ReadableByteArrayOutputStream()
        GeneratorRunner.mainAsProtocPlugin(stripPackages(request).inputStream(), out)
        CodeGeneratorResponse.deserialize(out.toByteArray())
            .file
            .map { it.copy { content = tidy(it.content.orEmpty(), params.formatOutput) } }
    } else {
        emptyList()
    }

private fun stripPackages(request: CodeGeneratorRequest): ByteArray =
    request.copy {
        protoFile =
            request.protoFile.map { fdp ->
                fdp.copy {
                    options =
                        (fdp.options ?: protokt.v1.google.protobuf.FileOptions {}).copy {
                            javaPackage = resolvePackage(fdp.`package`.orEmpty())
                            javaMultipleFiles = true
                        }
                }
            }
    }.serialize()

private class ReadableByteArrayOutputStream : ByteArrayOutputStream() {
    fun inputStream() =
        ByteArrayInputStream(buf, 0, count)
}
