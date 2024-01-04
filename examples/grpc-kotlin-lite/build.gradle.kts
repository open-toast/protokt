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
    generate {
        types = false
        descriptors = false
        grpcDescriptors = true
        grpcKotlinStubs = true
    }
}

dependencies {
    protobuf(project(":examples:protos"))

    implementation(project(":examples:protos"))
    implementation(libs.grpc.kotlin.stub)
    implementation(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.protobuf.lite)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.grpc.testing)
}

sourceSets {
    main {
        java {
            srcDir("../grpc-kotlin/src/main/kotlin")
        }
        resources {
            srcDir("../protos/src/main/resources")
        }
    }

    test {
        java {
            srcDir("../grpc-kotlin/src/test/kotlin")
            srcDir(liteOptionTestSourceDir())
            srcDir(rootProject.file("shared-src/lite-util"))
        }
    }
}
