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

import protokt.v1.gradle.protokt
import protokt.v1.gradle.protoktExtensions

plugins {
    id("protokt.jvm-conventions")
}

localProtokt()
pureKotlin()

protokt {
    generate {
        types = true
        grpcDescriptors = true
    }
}

dependencies {
    protoktExtensions(project(":extensions:protokt-extensions"))

    implementation(libs.grpc.stub)
    implementation(project(":protokt-runtime-grpc"))

    testImplementation(project(":protokt-reflect"))
    testImplementation(project(":testing:protobuf-java"))
    testImplementation(kotlin("reflect"))
    testImplementation(libs.jackson)
    testImplementation(libs.protobuf.java)
}
