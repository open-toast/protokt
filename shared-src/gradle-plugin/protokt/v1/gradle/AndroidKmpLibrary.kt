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

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Wiring for AGP's `com.android.kotlin.multiplatform.library` target.
 *
 * The android target creates no Gradle source sets, so protos live in
 * `src/main/proto` and `src/test/proto` and are compiled by the base
 * `generateProto`/`generateTestProto` tasks. Kotlin source sets map exactly
 * to compilations: androidMain to the main compilation, androidHostTest to
 * host tests when enabled. Device tests are not wired.
 */
internal object AndroidKmpLibrary {
    fun configure(project: Project, target: KotlinTarget, config: Config) {
        project.wireSourceSet(target, "androidMain", "compileAndroidMain", config.extensions, false)
        project.wireSourceSet(target, "androidHostTest", "compileAndroidHostTest", config.testExtensions, true)

        // AGP derives baselineProfiles directories as siblings of Kotlin source
        // directories, which places them inside generateProto's output.
        project.tasks.matching { it.name.endsWith("ArtProfile") }.configureEach {
            mustRunAfter("generateProto")
        }
    }

    private fun Project.wireSourceSet(
        target: KotlinTarget,
        sourceSetName: String,
        compileTaskName: String,
        extensionsConfiguration: org.gradle.api.artifacts.Configuration,
        test: Boolean
    ) {
        val protoSourceSetRoot = if (test) "test" else "main"
        val generateProtoTaskName = if (test) "generateTestProto" else "generateProto"

        extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets.matching { it.name == sourceSetName }.all {
            configurations.getByName(apiConfigurationName).extendsFrom(extensionsConfiguration)
            kotlin.srcDir(layout.buildDirectory.dir("generated/sources/proto/$protoSourceSetRoot/${target.protocPluginName}"))

            tasks.matching { it.name == compileTaskName }.configureEach {
                dependsOn(generateProtoTaskName)
            }
        }
    }
}
