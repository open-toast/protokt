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

import protokt.v1.gradle.protoktExtensions

plugins {
    kotlin("multiplatform")
    id("com.toasttab.protokt")
}

kotlin {
    jvm {
        withJava()
    }

    js(IR) {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }

        nodejs {
            testTask {
                useMocha()
            }
        }

        useCommonJs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // todo: not sure why this is needed
                implementation("com.toasttab.protokt:protokt-core:$version")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmTest by getting {
            dependencies {
                runtimeOnly(libs.protobuf.java)
            }
        }

        // todo: figure out how to reuse the tests from testing/protobufjs
        val jsTest by getting {}
    }

    targets {
        jvm().compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjvm-default=all")
            }
        }

        all {
            compilations.all {
                kotlinOptions {
                    languageVersion = System.getProperty("kotlin-integration.version")
                        ?.substringBeforeLast(".")
                        ?: libs.versions.kotlin.get().substringBeforeLast(".")
                    apiVersion = languageVersion
                }
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-extensions:$version")
}

tasks.named("jsNodeTest") {
    enabled = System.getProperty("kotlin.version", libs.versions.kotlin.get()) == libs.versions.kotlin.get()
}

tasks.named("jsBrowserTest") {
    enabled = System.getProperty("kotlin.version", libs.versions.kotlin.get()) == libs.versions.kotlin.get()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(System.getProperty("java-integration.version", libs.versions.java.get()).toInt()))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(System.getProperty("java-integration.version", libs.versions.java.get()).toInt()))
    }
}
