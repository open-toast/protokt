/*
 * Copyright (c) 2022 Toast, Inc.
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

import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    `kotlin-multiplatform`
    id("protokt.common-conventions")
    `java-base` // protobuf-gradle-plugin requires Java source sets to discover proto directories
}

the<SourceSetContainer>().create("main")
the<SourceSetContainer>().create("test")

kotlin {
    jvm {
        compilerOptions {
            // do not generate DefaultImpls objects since we do not target < JVM 1.8
            // https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-m3-generating-default-methods-in-interfaces
            jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        }
    }

    js(IR) {
        configureJsTests()
    }

    macosArm64()
    macosX64()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter)
                implementation(libs.truth)
            }
        }
    }

    compilerOptions {
        configureKotlin()
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

// Disable compilation, test, and publication tasks for native targets that
// can't cross-compile on the current host (e.g., linuxArm64 on an x64 host).
afterEvaluate {
    val disabledTargets = kotlin.targets
        .filterIsInstance<KotlinNativeTarget>()
        .filter { !HostManager().isEnabled(it.konanTarget) }

    for (target in disabledTargets) {
        tasks.matching { it.name.contains(target.name, ignoreCase = true) }
            .configureEach { enabled = false }
    }
}

configureJvmToolchain()
