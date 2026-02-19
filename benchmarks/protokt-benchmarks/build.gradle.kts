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

import com.google.protobuf.gradle.protobuf

plugins {
    id("protokt.benchmarks-conventions")
    application
}

localProtokt()

configure<JavaApplication> {
    mainClass.set("protokt.v1.benchmarks.ProtoktBenchmarksKt")
    executableDir = ".."
}

dependencies {
    protobuf(project(":benchmarks:schema"))

    implementation(kotlin("reflect"))
    implementation(project(":benchmarks:benchmarks-util"))
    implementation(libs.kotlinx.collectionsImmutable)

    runtimeOnly(libs.protobuf.java)
}

// Run all parameter combinations:
//   ./gradlew :benchmarks:protokt-benchmarks:run
//
// Pin specific parameters with -p:
//   ./gradlew :benchmarks:protokt-benchmarks:run --args="-p codec=protokt.v1.KotlinCodec"
//   ./gradlew :benchmarks:protokt-benchmarks:run --args="-p collectionFactory=protokt.v1.DefaultCollectionFactory"
//   ./gradlew :benchmarks:protokt-benchmarks:run --args="-p codec=protokt.v1.KotlinCodec -p collectionFactory=protokt.v1.DefaultCollectionFactory"
tasks.named("run") {
    dependsOn(":benchmarks:datasets")
}
