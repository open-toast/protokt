/*
 * Copyright (c) 2021 Toast, Inc.
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

import com.google.protobuf.gradle.protobuf
import protokt.v1.gradle.protokt

plugins {
    id("protokt.grpc-examples-conventions")
}

localProtokt()
pureKotlin()

protokt {
    onlyGenerateGrpc = true
    onlyGenerateGrpcDescriptors = true
    lite = true
}

dependencies {
    protobuf(project(":examples:protos"))

    implementation(project(":examples:protos"))
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
    }

    test {
        java {
            srcDir("../../testing/plugin-options/lite/src/test/kotlin/protokt/v1/testing/lite")
        }
    }
}
