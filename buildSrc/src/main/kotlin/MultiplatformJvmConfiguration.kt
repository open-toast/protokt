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

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureMultiplatformJvm() {
    the<SourceSetContainer>().create("main")
    the<SourceSetContainer>().create("test")

    configure<KotlinMultiplatformExtension> {
        jvm {
            compilerOptions {
                jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
            }
        }

        sourceSets.named("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        sourceSets.named("jvmTest") {
            dependencies {
                implementation(libs.junit.jupiter)
                implementation(libs.truth)
            }
        }

        compilerOptions {
            configureKotlin()
        }
    }

    tasks.named<Test>("jvmTest") {
        useJUnitPlatform()
    }

    configureJvmToolchain()
}
