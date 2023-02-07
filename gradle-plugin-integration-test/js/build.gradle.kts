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

import com.google.protobuf.gradle.proto
import com.toasttab.protokt.gradle.protoktExtensions

plugins {
    id("org.jetbrains.kotlin.js")
    id("com.toasttab.protokt")
}

protokt {
    formatOutput = false // https://github.com/pinterest/ktlint/issues/1195
}

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-extensions:$version")

    testImplementation(kotlin("test"))
}

kotlin {
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
}

kotlin {
    sourceSets {
        test {
            kotlin.srcDir("../multiplatform/src/commonTest/kotlin")
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("../multiplatform/src/main/proto")
        }
    }
}

tasks.named("compileKotlin") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}

tasks.named("irNodeTest") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}

tasks.named("irBrowserTest") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}

tasks.named("legacyNodeTest") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}

tasks.named("legacyBrowserTest") {
    enabled = System.getProperty("kotlin.version", "1.5.32") == "1.5.32"
}
