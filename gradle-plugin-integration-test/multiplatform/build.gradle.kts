/*
 * Copyright (c) 2022 Toast Inc.
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

import com.toasttab.protokt.gradle.plugin.ProtoktPlugin
import com.toasttab.protokt.gradle.protoktExtensions

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

    js(BOTH) {
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
                implementation("com.toasttab.protokt:protokt-util:$version")
            }
        }

        val jsTest by getting {}
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

// awkward that we have to apply the plugin after source sets are configured
apply<ProtoktPlugin>()

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-extensions:$version")
}

tasks.named("jsIrNodeTest") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}

tasks.named("jsIrBrowserTest") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}

tasks.named("jsLegacyNodeTest") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}

tasks.named("jsLegacyBrowserTest") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}
