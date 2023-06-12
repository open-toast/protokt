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
    id("protokt.jvm-conventions")
}

val grpcKotlinGenerator = configurations.create("grpcKotlinGenerator")

dependencies {
    grpcKotlinGenerator(libs.grpc.kotlin.gen) {
        artifact {
            name = "protoc-gen-grpc-kotlin"
            classifier = "jdk8"
            type = "jar"
            extension = "jar"
        }
    }
}

tasks.withType<Jar> {
    from(
        zipTree(grpcKotlinGenerator.singleFile)
            .matching { include("**/io/grpc/kotlin/**") }
    )
}
