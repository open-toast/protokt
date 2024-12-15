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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.internal.file.PathTraversalChecker
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.registerTransform
import org.gradle.kotlin.dsl.withType
import org.gradle.work.DisableCachingByDefault
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

private const val CODEGEN_CONFIGURATION = "_protokt_codegen_"
private val ARTIFACT_TYPE_ATTRIBUTE = Attribute.of("artifactType", String::class.java)
private val UNPACKED_CODEGEN_ATTRIBUTE = Attribute.of("unpackedCodegen", Boolean::class.javaObjectType)

/**
 * Unzip an application distribution and chmox +x the launchers.
 */
@DisableCachingByDefault(because = "Not worth caching")
abstract class UnzipDistTransform : TransformAction<TransformParameters.None> {
    @get:InputArtifact
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val zippedFile: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val zippedFile = zippedFile.get().asFile
        val unzippedDirName = zippedFile.name.substringAfter('.')
        val unzipDir = outputs.dir(unzippedDirName)

        ZipInputStream(BufferedInputStream(FileInputStream(zippedFile))).use { inputStream ->
            generateSequence { inputStream.nextEntry }.filter { !it.isDirectory }.forEach {
                val file = File(unzipDir, PathTraversalChecker.safePathName(it.name))

                file.parentFile.mkdirs()

                file.outputStream().buffered().use {
                    inputStream.copyTo(it)
                }

                if (it.name.contains("/bin/")) {
                    file.setExecutable(true)
                }
            }
        }
    }
}

internal fun binaryFromArtifact(project: Project): String {
    val configuration = project.configurations.create(CODEGEN_CONFIGURATION) {
        attributes.attribute(UNPACKED_CODEGEN_ATTRIBUTE, true)
    }

    project.dependencies {
        attributesSchema {
            attribute(UNPACKED_CODEGEN_ATTRIBUTE)
        }

        artifactTypes.createIfNecessary("zip") {
            attributes.attribute(UNPACKED_CODEGEN_ATTRIBUTE, false)
        }

        registerTransform(UnzipDistTransform::class) {
            from.attribute(UNPACKED_CODEGEN_ATTRIBUTE, false).attribute(ARTIFACT_TYPE_ATTRIBUTE, "zip")
            to.attribute(UNPACKED_CODEGEN_ATTRIBUTE, true).attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
        }

        add(
            CODEGEN_CONFIGURATION,
            "com.toasttab.protokt:protokt-codegen:$PROTOKT_VERSION:dist@zip"
        )

        project.afterEvaluate {
            tasks.withType<GenerateProtoTask> {
                inputs.files(project.configurations.getByName(CODEGEN_CONFIGURATION))
            }
        }
    }

    return configuration.singleFile.absolutePath + "/$CODEGEN_NAME-$PROTOKT_VERSION/bin/$CODEGEN_NAME"
}

private fun <T> NamedDomainObjectContainer<T>.createIfNecessary(name: String, configure: T.() -> Unit) {
    (findByName(name) ?: create(name)).configure()
}
