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

import com.google.protobuf.gradle.proto
import com.toasttab.protokt.v1.gradle.protokt
import com.toasttab.protokt.v1.gradle.protoktExtensions
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("org.jetbrains.kotlin.js")
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin
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
}

localProtokt()

dependencies {
    protoktExtensions(project(":extensions:protokt-extensions"))

    implementation(project(":protokt-runtime-grpc-lite"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

sourceSets {
    named("jsMain") {
        proto {
            srcDir("../protos/src/main/proto")
        }
    }
}

protokt {
    generateGrpc = true
}

tasks.withType<NodeJsExec> {
    args(listOfNotNull(properties["service"], properties["mode"]))
}

fun GradleBuild.setUp(service: String, mode: String) {
    startParameter.projectProperties =
        mapOf(
            "service" to service,
            "mode" to mode
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

tasks.register<GradleBuild>("AnimalsClient") {
    setUp("animals", "client")
    tasks = listOf("nodeProductionRun")
}
