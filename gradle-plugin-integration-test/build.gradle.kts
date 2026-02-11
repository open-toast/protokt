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

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
}

buildscript {
    val protoktVersion =
        file("$projectDir/../build/repos/integration/com/toasttab/protokt/v1/protokt-gradle-plugin")
            .listFiles { f -> f.isDirectory }!!
            .single()
            .name

    repositories {
        mavenCentral()
        google()
        maven(url = "$projectDir/../build/repos/integration")
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.toasttab.protokt.v1:protokt-gradle-plugin:$protoktVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${System.getProperty("kotlin-integration.version", libs.versions.kotlin.get())}")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:${libs.versions.spotless.get()}")
        classpath("com.android.tools.build:gradle:${libs.versions.androidGradlePlugin.get()}")
    }
}

val protoktVersion =
    file("$projectDir/../build/repos/integration/com/toasttab/protokt/v1/protokt-gradle-plugin")
        .listFiles { f -> f.isDirectory }!!
        .single()
        .name

allprojects {
    group = "com.toasttab.protokt.integration"
    version = protoktVersion

    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        val editorConfigOverride =
            mapOf(
                "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                "ktlint_function_signature_body_expression_wrapping" to "always",
                "ij_kotlin_packages_to_use_import_on_demand" to null,
            )

        kotlinGradle {
            target("**/*.kts")
            targetExclude("**/build/generated/**")
            ktlint(libs.versions.ktlint.get()).editorConfigOverride(editorConfigOverride)
        }

        kotlin {
            target("**/*.kt")
            targetExclude("**/build/generated/**")
            ktlint(libs.versions.ktlint.get()).editorConfigOverride(editorConfigOverride)
        }

        format("kotlinLicense") {
            target("**/*.kt")
            licenseHeaderFile(
                rootProject.file("gradle/license-header-c-style"),
                "(package |@file|import |fun )"
            )
            targetExclude("**/generated-sources/**")
        }

        format("protobufLicense") {
            target("**/*.proto")
            licenseHeaderFile(
                rootProject.file("gradle/license-header-c-style"),
                "(syntax )"
            )
        }
    }
}

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        maven(url = "${rootProject.projectDir}/../build/repos/integration")
        mavenCentral()
        google()
    }

    tasks {
        withType<Test> {
            environment("version", protoktVersion)
        }

        withType<JavaCompile> {
            enabled = false
        }

        withType<KotlinCompile> {
            compilerOptions {
                allWarningsAsErrors = true

                apiVersion = KotlinVersion.fromVersion(
                    System.getProperty("kotlin-integration.version")
                        ?.substringBeforeLast(".")
                        ?: libs.versions.kotlin.get().substringBeforeLast(".")
                )

                languageVersion = apiVersion
            }
        }
    }
}
