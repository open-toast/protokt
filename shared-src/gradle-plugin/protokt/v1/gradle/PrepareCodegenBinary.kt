/*
 * Copyright (c) 2026 Toast, Inc.
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

package protokt.v1.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

internal class CodegenBinary(
    val file: Provider<RegularFile>,
    val builtBy: Any
)

@DisableCachingByDefault(because = "The launcher contains a machine-specific absolute path")
internal abstract class PrepareCodegenBinary : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun writeLauncher() {
        val input = inputFile.get().asFile.absolutePath
        val output = outputFile.get().asFile
        output.parentFile.mkdirs()

        if (Os.current.kind == Os.Kind.WINDOWS) {
            output.writeText("@echo off\r\ncall \"$input\" %*\r\n")
        } else {
            output.writeText("#!/bin/sh\nexec ${input.shellQuoted()} \"\$@\"\n")
            if (!output.setExecutable(true)) {
                throw GradleException("Could not make ${output.absolutePath} executable")
            }
        }
    }
}

internal fun Project.prepareCodegenBinary(binary: CodegenBinary): TaskProvider<PrepareCodegenBinary> =
    tasks.register("prepareProtoktCodegenBinary", PrepareCodegenBinary::class.java) {
        inputFile.set(binary.file)
        outputFile.set(layout.buildDirectory.file("protokt/bin/${codegenExecutableName(CODEGEN_NAME)}"))
        dependsOn(binary.builtBy)
    }

internal fun codegenExecutableName(name: String) =
    name + if (Os.current.kind == Os.Kind.WINDOWS) ".bat" else ""

private fun String.shellQuoted() =
    "'${replace("'", "'\"'\"'")}'"
