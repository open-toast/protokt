/*
 * Copyright (c) 2022 Toast, Inc.
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

import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.common.io.Resources
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import com.toasttab.protokt.gradle.ProtoktExtension
import com.toasttab.protokt.testing.util.ProcessOutput.Src.ERR
import com.toasttab.protokt.testing.util.projectRoot
import com.toasttab.protokt.testing.util.runCommand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

abstract class AbstractProtoktCodegenTest {
    @TempDir
    lateinit var testDir: File

    private val testFile
        get() = File(testDir, "test_file.proto")

    @BeforeEach
    fun deleteGeneratedFile() {
        generatedFile.delete()
    }

    sealed interface PluginRunResult {
        fun orFail(): Success =
            when (this) {
                is Success -> this
                is Failure -> error(err)
            }
    }

    class Success(
        val response: CodeGeneratorResponse
    ) : PluginRunResult

    class Failure(
        val exitCode: Int,
        val err: String
    ) : PluginRunResult

    protected fun runPlugin(
        inputFile: String,
        ext: ProtoktExtension = ProtoktExtension(),
        transform: String.() -> String = { this }
    ): PluginRunResult {
        testFile.writeText(
            Paths.get(Resources.getResource(inputFile).toURI())
                .toFile()
                .readText()
                .transform()
        )

        listOf(
            System.getenv("PROTOC_PATH") ?: "protoc",
            "--plugin=protoc-gen-custom=$binGenerator",
            "--custom_out=.", // ignored
            "-I$testDir",
            "-I$extensionsProto",
            "-I$includeProtos",
            buildPluginOptions(ext),
            "$testFile"
        ).joinToString(" ")
            .runCommand(
                projectRoot.parentFile.toPath()
            ).orFail("Failed to generate code generator request", ERR)

        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val code = main(generatedFile.readBytes(), out, PrintStream(err))
        return if (code == 0) {
            Success(CodeGeneratorResponse.parseFrom(out.toByteArray()))
        } else {
            Failure(code, err.toString())
        }
    }
}

private fun buildPluginOptions(extension: ProtoktExtension) =
    "--custom_opt=" +
        extension::class.declaredMemberProperties
            .filter { it.returnType.classifier as KClass<*> == Boolean::class }
            .joinToString(",") {
                LOWER_CAMEL.to(LOWER_UNDERSCORE, it.name) + "=${it.call(extension)}"
            }

private val codegenTestingResources =
    Path.of(
        "protokt-codegen", "src", "test", "resources",
        "com", "toasttab", "protokt", "codegen", "testing"
    )

private val binGenerator =
    File(codegenTestingResources.toFile(), "bin-generator")

private val extensionsProto =
    Path.of("extensions", "protokt-extensions-lite", "src", "main", "proto")

private val includeProtos =
    File(projectRoot, "build/extracted-include-protos/main")

private val generatedFile =
    File(
        projectRoot,
        Path.of(
            "src",
            "test",
            "resources",
            "com",
            "toasttab",
            "protokt",
            "test-proto-bin-request.bin"
        ).toString()
    )
