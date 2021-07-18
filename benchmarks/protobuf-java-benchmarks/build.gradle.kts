/*
 * Copyright (c) 2019 Toast Inc.
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
import com.google.protobuf.gradle.protoc

plugins {
    id("com.google.protobuf")
    application
}

configure<JavaApplication> {
    mainClassName = "com.toasttab.protokt.benchmarks.ProtobufBenchmarksKt"
    executableDir = ".."
}

protobuf {
    protoc {
        artifact = libraries.protoc
    }
}

dependencies {
    implementation(project(":benchmarks:benchmarks-util"))
    implementation(libraries.protobuf)

    protobuf(project(":benchmarks:schema"))
}

tasks.named("run") {
    dependsOn(":benchmarks:datasets")
}
