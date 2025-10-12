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

package protokt.v1.gradle

import com.google.protobuf.gradle.GenerateProtoTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.file.PathTraversalChecker
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

abstract class ExtractCodegenTask : DefaultTask() {
    @get:InputFile
    abstract val zipFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun extract() {
        val zip = zipFile.get().asFile
        val outputDir = outputDirectory.get().asFile

        outputDir.deleteRecursively()
        outputDir.mkdirs()

        ZipInputStream(BufferedInputStream(FileInputStream(zip))).use { inputStream ->
            generateSequence { inputStream.nextEntry }.filter { !it.isDirectory }.forEach {
                val file = File(outputDir, PathTraversalChecker.safePathName(it.name))

                file.parentFile.mkdirs()
                file.outputStream().buffered().use(inputStream::copyTo)

                if (it.name.contains("/bin/")) {
                    file.setExecutable(true)
                }
            }
        }
    }
}

internal fun binaryFromArtifact(project: Project): String {
    val dependency = project.dependencies.create("$BASE_GROUP_NAME:protokt-codegen:$PROTOKT_VERSION:dist@zip")
    val configuration = project.configurations.detachedConfiguration(dependency)
    val targetDir = project.layout.buildDirectory.dir("protokt-codegen/$CODEGEN_NAME-$PROTOKT_VERSION")

    val extractTask =
        project.tasks.register<ExtractCodegenTask>("extractProtoktCodegen") {
            zipFile.fileProvider(project.provider { configuration.singleFile })
            outputDirectory.set(targetDir)
        }

    project.afterEvaluate {
        tasks.withType<GenerateProtoTask> {
            dependsOn(extractTask)
            inputs.files(configuration)
        }
    }

    return targetDir.get().asFile.resolve("$CODEGEN_NAME-$PROTOKT_VERSION/bin/$CODEGEN_NAME").absolutePath
}
