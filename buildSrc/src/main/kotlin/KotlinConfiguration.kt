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

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

fun KotlinCommonCompilerOptions.configureKotlin() {
    allWarningsAsErrors = true
    languageVersion.set(KotlinVersion.KOTLIN_1_8)
    apiVersion.set(KotlinVersion.KOTLIN_1_8)

    // do not generate DefaultImpls objects since we do not target < JVM 1.8
    // https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-m3-generating-default-methods-in-interfaces
    freeCompilerArgs.addAll("-Xjvm-default=all", "-Xexpect-actual-classes")
}

fun Project.configureJvmToolchain() {
    the<JavaPluginExtension>().toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
    configure<KotlinProjectExtension> {
        jvmToolchain(libs.versions.java.get().toInt())
    }
}
