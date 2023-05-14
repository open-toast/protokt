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

import com.toasttab.protokt.v1.gradle.protoktExtensions

plugins {
    id("org.jetbrains.kotlin.js")
    id("com.toasttab.protokt.v1")
}

protokt {
    formatOutput = false // https://github.com/pinterest/ktlint/issues/1195
}

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-extensions:$version")

    testImplementation(kotlin("test"))
}

kotlin {
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
}

kotlin {
    sourceSets {
        test {
            val common = "../multiplatform/src/commonTest/kotlin"
            val js = "../../testing/protobufjs/src/test/kotlin"
            check(file(common).exists())
            check(file(js).exists())
            kotlin.srcDir(common)
            kotlin.srcDir(js)
        }
    }
}

// TODO
/*
sourceSets {
    named("main") {
        proto {
            srcDir("../multiplatform/src/main/proto")
            srcDir("../../testing/protobufjs/src/main/proto")
        }
    }
}
 */

tasks.all {
    enabled = System.getProperty("kotlin.version", "1.8.21") == "1.8.21"
}
