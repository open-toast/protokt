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

import com.toasttab.protokt.testing.util.ProcessOutput.Src.ERR
import com.toasttab.protokt.testing.util.projectRoot
import com.toasttab.protokt.testing.util.runCommand
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Paths
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MainTest {
    @Test
    @Disabled // Enable this test if you want to step through the code generator
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
        main(generatedFile.readBytes(), out)
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
