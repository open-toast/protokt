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

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
}

buildscript {
    repositories {
        maven(url = "$projectDir/../build/repos/integration")
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("com.toasttab.protokt:protokt-gradle-plugin:$version")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${System.getProperty("kotlin.version", "1.6.32")}")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:5.15.0")
    }
}

allprojects {
    group = "com.toasttab.protokt.integration"
    version = rootProject.version

    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        kotlinGradle {
            ktlint()
        }
    }
}

subprojects {
    repositories {
        maven(url = "${rootProject.projectDir}/../build/repos/integration")
        mavenCentral()
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                allWarningsAsErrors = true
                jvmTarget = "1.8"

                apiVersion =
                    System.getProperty("kotlin.version")
                        ?.substringBeforeLast(".")
                        ?: "1.6"

                languageVersion = apiVersion
            }
        }

        withType<Test> {
            environment("version", version.toString())
        }

        withType<JavaCompile> {
            enabled = false
        }
    }
}
