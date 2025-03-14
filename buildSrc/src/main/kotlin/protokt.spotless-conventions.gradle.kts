/*
 * Copyright (c) 2023 Toast, Inc.
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

import com.diffplug.gradle.spotless.BaseKotlinExtension
import com.diffplug.gradle.spotless.FormatExtension

plugins {
    id("com.diffplug.spotless")
}

repositories { mavenCentral() }

spotless {
    val editorConfigOverride =
        mapOf(
            "ktlint_standard_trailing-comma-on-call-site" to "disabled",
            "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
            "ktlint_function_signature_body_expression_wrapping" to "always",
            "ij_kotlin_packages_to_use_import_on_demand" to null,
        )

    fun BaseKotlinExtension.ktlintConfig() =
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(editorConfigOverride)

    fun FormatExtension.kotlinTargets() =
        target("src/**/*.kt", "buildSrc/src/**/*.kt")

    kotlin {
        ktlintConfig()
        kotlinTargets()
    }

    kotlinGradle {
        ktlintConfig()
        target("*.kts", "buildSrc/**/*.kts")
        targetExclude("**/kotlin-dsl/plugins-blocks/extracted/**")
        licenseHeaderFile(
            rootProject.file("gradle/license-header-c-style"),
            "(package |@file|import |fun )|buildscript |plugins |group ="
        )
    }

    // Separate from ktlint to allow license formatting for files with shared copyrights to be specified independently
    format("kotlinLicense") {
        kotlinTargets()
        licenseHeaderFile(
            rootProject.file("gradle/license-header-c-style"),
            "(package |@file|import |fun )"
        )
    }

    format("protobufLicense") {
        target("src/**/*.proto")
        licenseHeaderFile(
            rootProject.file("gradle/license-header-c-style"),
            "(syntax |edition )"
        )
    }
}
