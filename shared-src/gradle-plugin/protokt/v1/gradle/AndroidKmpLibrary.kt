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

import com.android.build.api.variant.Component
import com.android.build.api.variant.HostTest
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.work.DisableCachingByDefault
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import javax.inject.Inject

/**
 * Wiring for AGP's `com.android.kotlin.multiplatform.library` target.
 *
 * The android target creates no Gradle source sets, so protos live in
 * `src/main/proto` and `src/test/proto` and are compiled by the base
 * `generateProto`/`generateTestProto` tasks. Generated sources are registered
 * with the variant API rather than added to Kotlin source sets directly:
 * source sets map exactly to compilations (androidMain to the main
 * compilation, androidHostTest to host tests).
 */
internal object AndroidKmpLibrary {
    fun configure(project: Project, target: KotlinTarget, config: Config) {
        val sourceSets = project.extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets

        sourceSets.matching { it.name == "androidMain" }.all {
            project.configurations.getByName(apiConfigurationName).extendsFrom(config.extensions)
        }

        sourceSets.matching { it.name == "androidHostTest" }.all {
            project.configurations.getByName(apiConfigurationName).extendsFrom(config.testExtensions)
        }

        val components = project.extensions.getByType(KotlinMultiplatformAndroidComponentsExtension::class.java)

        components.onVariants(components.selector().all()) { variant ->
            project.addGeneratedProtoSources(variant, target, false)

            variant.nestedComponents.filterIsInstance<HostTest>().forEach {
                project.addGeneratedProtoSources(it, target, true)
            }
        }

        // commonMain's generated srcDir participates in the android compilation, and AGP
        // derives baselineProfiles directories as siblings of Kotlin source directories,
        // placing them inside generateProto's output.
        project.tasks.matching { it.name.endsWith("ArtProfile") }.configureEach {
            mustRunAfter("generateProto")
        }
    }

    private fun Project.addGeneratedProtoSources(component: Component, target: KotlinTarget, test: Boolean) {
        val protoSourceSetRoot = if (test) "test" else "main"
        val generateProtoTaskName = if (test) "generateTestProto" else "generateProto"

        val syncTask =
            tasks.register<SyncGeneratedProtoSources>("sync${component.name.capitalized()}GeneratedProtoSources") {
                dependsOn(generateProtoTaskName)
                generatedSources.from(layout.buildDirectory.dir("generated/sources/proto/$protoSourceSetRoot/${target.protocPluginName}"))
            }

        component.sources.kotlin?.addGeneratedSourceDirectory(syncTask, SyncGeneratedProtoSources::destination)
    }
}

@DisableCachingByDefault(because = "copies already-generated sources")
abstract class SyncGeneratedProtoSources : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val generatedSources: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @get:Inject
    protected abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun sync() {
        fileSystemOperations.sync {
            from(generatedSources)
            into(destination)
        }
    }
}
