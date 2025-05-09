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

import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import protokt.v1.gradle.protokt

plugins {
    `kotlin-multiplatform`
}

kotlin {
    js(IR) {
        nodejs {
            testTask {
                useMocha()
            }
        }
        binaries.executable()
        useCommonJs()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                api(project(":protokt-runtime-grpc-lite"))
                api(project(":examples:protos"))
                api(libs.kotlinx.coroutines.core)
            }
        }

        val jsTest by getting {
            dependencies {
                api(kotlin("test"))
                api(libs.kotlinx.coroutines.test)
            }
        }
    }
}

localProtokt()

protokt {
    generate {
        types = false
        descriptors = false
        grpcDescriptors = true
        grpcKotlinStubs = true
    }
}

tasks.withType<NodeJsExec> {
    args(listOfNotNull(properties["service"], properties["mode"], properties["args"]))
}

fun GradleBuild.setUp(service: String, mode: String, vararg extra: Pair<String, String>) {
    startParameter.projectProperties =
        mapOf(
            "service" to service,
            "mode" to mode,
            *extra
        )
}

tasks.register<GradleBuild>("HelloWorldServer") {
    setUp("helloworld", "server")
    tasks = listOf("nodeProductionRun")
}

tasks.register<GradleBuild>("RouteGuideServer") {
    setUp("routeguide", "server")
    tasks = listOf("nodeProductionRun")
}

tasks.register<GradleBuild>("AnimalsServer") {
    setUp("animals", "server")
    tasks = listOf("nodeProductionRun")
}

tasks.register<GradleBuild>("HelloWorldClient") {
    setUp("helloworld", "client")
    tasks = listOf("nodeProductionRun")
}

tasks.register<GradleBuild>("RouteGuideClient") {
    setUp("routeguide", "client")
    tasks = listOf("nodeProductionRun")
}

tasks.register<AnimalsClientTask>("AnimalsClient") {
    doFirst { setUp("animals", "client", "args" to args) }
    tasks = listOf("nodeProductionRun")
}

abstract class AnimalsClientTask : GradleBuild() {
    @Input
    @Option(option = "args", description = "shim for JavaExec args")
    lateinit var args: String
}
