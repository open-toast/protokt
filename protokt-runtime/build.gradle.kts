/*
 * Copyright (c) 2019 Toast, Inc.
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
                compileOnly(libs.kotlinx.collectionsImmutable)
            }
        }

        val jvmMain by getting {
            dependencies {
                compileOnly(libs.protobuf.java)
            }
        }

        val jsMain by getting {
            dependencies {
                api(npm("protobufjs", libs.versions.protobuf.js.get()))

                // JS module imports fail at load time if referenced classes are missing,
                // regardless of whether the code path is reached. Since Collections.kt and
                // BuilderScope.kt reference PersistentList/PersistentMap, the library must
                // always be bundled for JS even when persistent collections are not enabled.
                implementation(libs.kotlinx.collectionsImmutable)
            }
        }
    }
}
