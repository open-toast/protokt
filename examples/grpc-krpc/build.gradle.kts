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
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import protokt.v1.gradle.protokt

plugins {
    id("protokt.krpc-conventions")
}

localProtokt()
pureKotlin()

apply(plugin = "org.jetbrains.kotlinx.rpc.plugin")

kotlin {
    macosArm64()
    macosX64()
    linuxX64()
    linuxArm64()

    targets.withType<KotlinNativeTarget> {
        binaries {
            executable()
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":examples:protos"))
                implementation(project(":protokt-runtime-grpc-krpc"))
                implementation(libs.kotlinx.rpc.grpc.client)
                implementation(libs.kotlinx.rpc.grpc.server)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        jvmMain {
            dependencies {
                runtimeOnly(libs.grpc.netty)
            }
        }
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
}
