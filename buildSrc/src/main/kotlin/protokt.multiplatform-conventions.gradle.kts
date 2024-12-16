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

plugins {
    `kotlin-multiplatform`
    id("protokt.common-conventions")
}

kotlin {
    jvm {
        withJava()
    }

    js(IR) {
        configureJsTests()
    }

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

    targets {
        all {
            compilations.all {
                kotlinOptions {
                    allWarningsAsErrors = true
                    // expect / actual classes are in Beta and emit a warning in Kotlin 1.9.20
                    // see https://youtrack.jetbrains.com/issue/KT-61573
                    freeCompilerArgs += "-Xexpect-actual-classes"
                    languageVersion = "1.8"
                    apiVersion = "1.8"
                }
            }
        }

        jvm().compilations.all {
            kotlinOptions {
                // do not generate DefaultImpls objects since we do not target < JVM 1.8
                // https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-m3-generating-default-methods-in-interfaces
                freeCompilerArgs += "-Xjvm-default=all"
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

pureKotlin()

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}
