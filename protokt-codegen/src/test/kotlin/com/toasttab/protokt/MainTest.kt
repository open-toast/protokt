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

import com.google.protobuf.compiler.PluginProtos
import com.toasttab.protokt.gradle.GENERATE_GRPC
import com.toasttab.protokt.gradle.ONLY_GENERATE_GRPC
import com.toasttab.protokt.gradle.RESPECT_JAVA_PACKAGE
import com.toasttab.protokt.testing.util.ProcessOutput.Src.ERR
import com.toasttab.protokt.testing.util.projectRoot
import com.toasttab.protokt.testing.util.runCommand
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Paths
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MainTest {
    /**
     * Enable this test if you want to step through the code generator.
     * Note that you'll have to make a code change to allow the manifest in
     * ProtoktVersion to be null, e.g.
     *
     *     JarInputStream(it)
     *         .manifest
     *         ?.mainAttributes
     *         ?.getValue(MANIFEST_VERSION_PROPERTY)
     *         ?: "unknown"
     */
    @Test
    @Disabled
    fun `step through code generation with debugger`() {
        generatedFile.delete()

        listOf(
            "protoc",
            "--plugin=protoc-gen-custom=$binGenerator",
            "--custom_out=.", // ignored
            "-I$codegenTestingProto",
            "-I$runtimeResources",
            "$testProto"
        ).joinToString(" ")
            .runCommand(
                Paths.get(projectRoot)
            ).orFail("Failed to generate code generator request", ERR)

        val out = ByteArrayOutputStream()
        main(
            PluginProtos.CodeGeneratorRequest.parseFrom(generatedFile.readBytes())
                .toBuilder()
                .setParameter(
                    mapOf(
                        GENERATE_GRPC to true,
                        RESPECT_JAVA_PACKAGE to true,
                        ONLY_GENERATE_GRPC to false
                    ).entries.joinToString(separator = ",") { "${it.key}=${it.value}" }
                )
                .build()
                .toByteArray(),
            out
        )
        println(out.toString())
    }
}

private val codegenTestingResources =
    Paths.get(
        "protokt-codegen", "src", "test", "resources",
        "com", "toasttab", "protokt", "codegen", "testing"
    )

private val binGenerator =
    File(codegenTestingResources.toFile(), "bin-generator")

private val codegenTestingProto =
    Paths.get(
        "protokt-codegen", "src", "test", "proto",
        "com", "toasttab", "protokt", "codegen", "testing"
    )

private val testProto =
    File(codegenTestingProto.toFile(), "test.proto")

private val runtimeResources =
    Paths.get("protokt-runtime", "src", "main", "resources")

private val generatedFile =
    File(
        projectRoot,
        Paths.get(
            "protokt-codegen", "src", "test", "resources",
            "com", "toasttab", "protokt", "test-proto-bin-request.bin"
        ).toString()
    )
