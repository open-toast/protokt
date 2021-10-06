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

import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.protobuf.compiler.PluginProtos
import com.toasttab.protokt.gradle.ProtoktExtension
import com.toasttab.protokt.testing.util.ProcessOutput.Src.ERR
import com.toasttab.protokt.testing.util.projectRoot
import com.toasttab.protokt.testing.util.runCommand
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

class MainTest {
    @Test
    fun `step through code generation with debugger`() {
        generatedFile.delete()

        val ext =
            ProtoktExtension().apply {
                // set any plugin options here
            }

        listOf(
            System.getenv("PROTOC_PATH") ?: "protoc",
            "--plugin=protoc-gen-custom=$binGenerator",
            "--custom_out=.", // ignored
            "-I$codegenTestingProto",
            "-I$runtimeResources",
            "-I$includeProtos",
            buildPluginOptions(ext),
            "$testProto"
        ).joinToString(" ")
            .runCommand(
                projectRoot.toPath()
            ).orFail("Failed to generate code generator request", ERR)

        val out = ByteArrayOutputStream()
        main(generatedFile.readBytes(), out)
        PluginProtos.CodeGeneratorResponse.parseFrom(out.toByteArray())
            .fileList
            .forEach {
                println(it.name)
                println(it.content)
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

private val codegenTestingProto =
    Path.of(
        "protokt-codegen", "src", "test", "proto",
        "toasttab", "protokt", "codegen", "testing"
    )

private val testProto =
    File(codegenTestingProto.toFile(), "test.proto")

private val runtimeResources =
    Path.of("protokt-runtime", "src", "main", "resources")

private val generatedFile =
    File(
        projectRoot,
        Path.of(
            "protokt-codegen", "src", "test", "resources",
            "com", "toasttab", "protokt", "test-proto-bin-request.bin"
        ).toString()
    )

private val includeProtos =
    File(projectRoot, "protokt-codegen/build/extracted-include-protos/main")
