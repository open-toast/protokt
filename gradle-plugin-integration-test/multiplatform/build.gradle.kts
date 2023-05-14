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

import com.toasttab.protokt.v1.gradle.ProtoktExtension
import com.toasttab.protokt.v1.gradle.ProtoktPlugin
import com.toasttab.protokt.v1.gradle.protoktExtensions

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

spotless {
    kotlin {
        ktlint()
        targetExclude("**/generated-sources/**")
    }
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
        val commonMain by getting {}

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("com.google.protobuf:protobuf-javalite:3.19.1")
            }
        }

        val jsMain by getting {}
        val jsTest by getting {}
        val js = "../../testing/protobufjs/src/test/kotlin"
        check(file(js).exists())
        jsTest.kotlin.srcDir(js)
    }

    targets {
        jvm().compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjvm-default=all")
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

// awkward that we have to apply the plugin after source sets are configured
apply<ProtoktPlugin>()

configure<ProtoktExtension> {
    formatOutput = false // https://github.com/pinterest/ktlint/issues/1195
}

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-extensions:$version")
}

tasks.named("jsNodeTest") {
    enabled = System.getProperty("kotlin.version", "1.8.21") == "1.8.21"
}

tasks.named("jsBrowserTest") {
    enabled = System.getProperty("kotlin.version", "1.8.21") == "1.8.21"
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
