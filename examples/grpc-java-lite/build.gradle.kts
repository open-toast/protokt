/*
 * Copyright (c) 2021 Toast Inc.
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
import com.toasttab.protokt.gradle.protokt

plugins {
    id("protokt.grpc-examples-conventions")
}

localProtokt()
pureKotlin()

protokt {
    generateGrpc = true
    lite = true
}

dependencies {
    implementation(libs.jackson)

    runtimeOnly(libs.protobufLite)

    testImplementation(project(":protokt-util"))
}

sourceSets {
    main {
        java {
            srcDir("../grpc-java/src/main/kotlin")
            srcDir("../protos/src/main/kotlin")
        }

        proto {
            srcDir("../protos/src/main/proto")
        }
    }

    test {
        java {
            srcDir("../../testing/plugin-options/lite/src/test/kotlin/com/toasttab/protokt/testing/lite")
        }
    }
}
