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

import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    `kotlin-multiplatform`
    id("protokt.common-conventions")
    `java-base`
}

the<SourceSetContainer>().create("main")
the<SourceSetContainer>().create("test")

enablePublishing()
pureKotlin()
trackKotlinApiCompatibility()

repositories {
    maven("https://packages.jetbrains.team/maven/p/krpc/grpc")
}

kotlin {
    jvm {
        compilerOptions {
            jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        }
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

    applyDefaultHierarchyTemplate()

    compilerOptions {
        configureKotlin()
        freeCompilerArgs.addAll("-opt-in=kotlinx.rpc.internal.utils.ExperimentalRpcApi")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":protokt-runtime-kotlinx-io"))
                api(libs.kotlinx.rpc.grpc.marshaller)
            }
        }

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
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

configureJvmToolchain()
