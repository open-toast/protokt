/*
 * Copyright (c) 2024 Toast, Inc.
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

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.net.URI
import javax.inject.Inject

private val BOOTSTRAP_FILES =
    listOf(
        "protokt/v1/google/protobuf/descriptor.kt" to "protokt/v1/google/protobuf/Descriptor.kt",
        "protokt/v1/google/protobuf/compiler/plugin.kt" to "protokt/v1/google/protobuf/compiler/Plugin.kt",
        "protokt/v1/protokt.kt" to "protokt/v1/Protokt.kt"
    )

@CacheableTask
abstract class DownloadWellKnownProtos : DefaultTask() {
    @get:Input
    abstract val protobufVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun download() {
        val baseUrl = "https://raw.githubusercontent.com/protocolbuffers/protobuf/v${protobufVersion.get()}/src"
        listOf(
            "google/protobuf/descriptor.proto",
            "google/protobuf/compiler/plugin.proto"
        ).forEach { proto ->
            val target = outputDir.get().file(proto).asFile
            target.parentFile.mkdirs()
            URI("$baseUrl/$proto").toURL().openStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }
}

abstract class RegenerateBootstrap @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val protoc: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val codegen: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val extensionsProtoDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val wellKnownProtosDir: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedDir: DirectoryProperty

    @get:Internal
    abstract val bootstrapDir: DirectoryProperty

    @TaskAction
    fun regenerate() {
        val genDir = generatedDir.get().asFile
        genDir.deleteRecursively()
        genDir.mkdirs()

        execOperations.exec {
            commandLine(
                protoc.get().asFile.absolutePath,
                "--plugin=protoc-gen-custom=${codegen.get().asFile.absolutePath}",
                "--custom_out=$genDir",
                "--custom_opt=kotlin_target=jvm,generate_types=true,generate_descriptors=false",
                "--proto_path=${wellKnownProtosDir.get().asFile.absolutePath}",
                "--proto_path=${extensionsProtoDir.get().asFile.absolutePath}",
                "google/protobuf/descriptor.proto",
                "google/protobuf/compiler/plugin.proto",
                "protokt/v1/protokt.proto"
            )
        }

        val bootstrap = bootstrapDir.get().asFile
        BOOTSTRAP_FILES.forEach { (src, dest) ->
            val destFile = bootstrap.resolve(dest)
            destFile.parentFile.mkdirs()
            genDir.resolve(src).copyTo(destFile, overwrite = true)
        }
    }
}

abstract class VerifyBootstrap : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val generatedDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val bootstrapDir: DirectoryProperty

    @TaskAction
    fun verify() {
        val genDir = generatedDir.get().asFile
        val bootstrap = bootstrapDir.get().asFile
        BOOTSTRAP_FILES.forEach { (generated, checkedIn) ->
            check(genDir.resolve(generated).readText() == bootstrap.resolve(checkedIn).readText()) {
                "Bootstrap file ${checkedIn.substringAfterLast('/')} is out of date. " +
                    "Run: ./gradlew :protokt-codegen:regenerateBootstrap"
            }
        }
    }
}
