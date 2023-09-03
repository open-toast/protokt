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
    kotlin("multiplatform")
    id("protokt.common-conventions")
}

kotlin {
    jvm {
        withJava()
    }

    js(IR) {
        browser {}
        nodejs {}
        useCommonJs()
    }

    sourceSets {
        val jvmTest by getting {
            dependencies {
                implementation(findLibrary("junit-jupiter"))
                implementation(findLibrary("truth"))
            }
        }
    }

    targets {
        all {
            compilations.all {
                kotlinOptions {
                    allWarningsAsErrors = true
                }
            }
        }

        jvm().compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjvm-default=all")
            }

            jvmToolchain(8)
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

pureKotlin()
