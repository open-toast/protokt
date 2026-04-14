/*
 * Copyright (c) 2026 Toast, Inc.
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
    id("protokt.jvm-conventions")
}

localProtokt()
pureKotlin()

apply(plugin = "org.jetbrains.kotlinx.rpc.plugin")

repositories {
    maven("https://packages.jetbrains.team/maven/p/krpc/grpc")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.rpc.internal.utils.ExperimentalRpcApi",
            "-opt-in=protokt.v1.OnlyForUseByGeneratedProtoCode",
        )
    }
}

protokt {
    generate {
        types = false
        descriptors = false
        grpcKrpc = true
    }
}

dependencies {
    protobuf(project(":examples:protos"))

    implementation(project(":examples:protos"))
    implementation(project(":protokt-runtime-grpc-krpc"))
    implementation(libs.kotlinx.rpc.grpc.client)
    implementation(libs.kotlinx.rpc.grpc.server)
    implementation(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.grpc.netty)

    testImplementation(libs.junit.jupiter)
}
