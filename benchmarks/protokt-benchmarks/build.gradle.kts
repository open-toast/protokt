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
    id("protokt.multiplatform-conventions")
    id("org.jetbrains.kotlinx.benchmark")
    `kotlin-kapt`
}

localProtokt()

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries {
            executable { entryPoint = "main" }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":benchmarks:benchmarks-util"))
                implementation(project(":protokt-runtime-kotlinx-io"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":protokt-runtime-persistent-collections"))
            }
        }
    }
}

dependencies {
    protobuf(project(":benchmarks:schema"))
    kapt(libs.jmh.generator)
}

benchmark {
    targets {
        register("jvm")
        register("macosArm64")
    }
}

tasks.matching { it.name.contains("benchmark", ignoreCase = true) }.configureEach {
    dependsOn(":benchmarks:datasets")
}

tasks.register<JavaExec>("run") {
    dependsOn(":benchmarks:datasets")
    mainClass.set("protokt.v1.benchmarks.ProtoktBenchmarksKt")
    classpath = kotlin.jvm().compilations["main"].runtimeDependencyFiles +
        kotlin.jvm().compilations["main"].output.allOutputs
    workingDir = file("..")
}
