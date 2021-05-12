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

import com.toasttab.protokt.gradle.MANIFEST_VERSION_PROPERTY
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    kotlin("jvm") version versions.kotlin
}

allprojects {
    repositories {
        mavenCentral()
    }

    lint()
    group = "com.toasttab.protokt"
}

promoteStagingRepo()

subprojects {
    apply(plugin = "idea")
    apply(plugin = "kotlin")

    dependencies {
        api(libraries.kotlinStdlib)

        testImplementation(libraries.junit)
        testImplementation(libraries.truth)
    }

    version = rootProject.version

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                allWarningsAsErrors = true
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xinline-classes")
            }
        }

        withType<Test> {
            useJUnitPlatform()
        }

        jar {
            manifest {
                attributes(
                    MANIFEST_VERSION_PROPERTY to "${project.version}"
                )
            }
        }
    }
}
