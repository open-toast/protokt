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

plugins {
    id("protokt.multiplatform-published-conventions")
}

compatibleWithAndroid()

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":protokt-runtime"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.grpc.stub)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(npm("@grpc/grpc-js", libs.versions.grpc.js.get()))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=protokt.v1.OnlyForUseByGeneratedProtoCode")
    }
}
