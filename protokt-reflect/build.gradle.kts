/*
 * Copyright (c) 2023 Toast, Inc.
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
import protokt.v1.gradle.testProtoktExtensions

plugins {
    id("protokt.multiplatform-published-conventions")
}

localProtokt(false)

kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(project(":protokt-core"))
                api(libs.protobuf.java)

                implementation(kotlin("reflect"))
            }
        }
    }
}

tasks.withType<JavaCompile> { enabled = true }

sourceSets {
    main {
        java {
            srcDir(rootProject.file("shared-src/reflect"))
        }
        proto {
            srcDir("../extensions/protokt-extensions-lite/src/extensions-proto")
        }
    }
}

dependencies {
    testProtoktExtensions(project(":extensions:protokt-extensions"))
}
