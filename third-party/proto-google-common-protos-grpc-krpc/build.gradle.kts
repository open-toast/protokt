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

import com.toasttab.expediter.gradle.config.ExpediterExtension
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import protokt.v1.gradle.protokt

plugins {
    `kotlin-multiplatform`
    id("protokt.common-conventions")
    id("protokt.third-party-conventions")
    `java-base`
}

the<SourceSetContainer>().create("main")
the<SourceSetContainer>().create("test")

publishedLocalProtokt()
pureKotlin()
enablePublishing()
compatibleWithAndroid()
trackKotlinApiCompatibility()

configure<ExpediterExtension> {
    ignore {
        // kotlinx-rpc transitively pulls in protobuf-java via grpc-java alongside
        // protokt's protobuf-javalite; these duplicate types are benign since this
        // module only contains generated @Grpc interfaces.
        targetStartsWith("com/google/protobuf")
    }
}

repositories {
    maven("https://packages.jetbrains.team/maven/p/krpc/grpc")
}

protokt {
    generate {
        types = false
        descriptors = false
        grpcKrpc = true
    }
}

kotlin {
    jvm {
        compilerOptions {
            jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        }
    }

    macosArm64()
    macosX64()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
    linuxX64()
    linuxArm64()

    applyDefaultHierarchyTemplate()

    compilerOptions {
        configureKotlin()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":third-party:proto-google-common-protos-lite"))
                api(libs.kotlinx.rpc.grpc.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter)
                implementation(libs.truth)
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

configureJvmToolchain()

dependencies {
    protobufExcludingProtobufJava(libs.protoGoogleCommonProtos)
}
