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

import com.google.protobuf.gradle.proto
import protokt.v1.gradle.CODEGEN_NAME

plugins {
    id("protokt.jvm-conventions")
    id("com.google.protobuf")
    alias(libs.plugins.buildConfig)
    application
}

defaultProtoc()

enablePublishing(defaultJars = false)

application {
    applicationName = CODEGEN_NAME
    mainClass.set("protokt.v1.codegen.MainKt")
}

dependencies {
    implementation(project(":extensions:protokt-extensions-api"))
    implementation(project(":protokt-runtime"))
    implementation(project(":protokt-runtime-grpc-lite"))
    implementation(project(":grpc-kotlin-shim"))

    implementation(kotlin("reflect"))

    implementation(libs.grpc.kotlin.stub)
    implementation(libs.grpc.stub)
    implementation(libs.kotlinLogging)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktlint)
    implementation(libs.ktlintRuleSetStandard)
    implementation(libs.protobuf.java)
    implementation(libs.slf4jSimple)

    testImplementation(project(":testing:testing-util"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("dist") {
            artifact(
                mapOf(
                    "source" to tasks.findByName("distZip"),
                    "extension" to "zip",
                    "classifier" to "dist"
                )
            )
            artifactId = project.name
            version = "${rootProject.version}"
            groupId = "${rootProject.group}"
        }
    }
}

tasks.withType<Test> {
    afterEvaluate {
        environment("PROTOC_PATH", configurations.named("protobufToolsLocator_protoc").get().singleFile)
    }
}

sourceSets {
    main {
        java {
            srcDir("../shared-src/codegen")
        }
        proto {
            srcDir("../extensions/protokt-extensions-lite/src/main/proto")
        }
    }
}

buildConfig {
    useKotlinOutput { topLevelConstants = true }
    packageName.set("protokt.v1.gradle")
    buildConfigField("String", "DEFAULT_PROTOBUF_VERSION", "\"${libs.versions.protobuf.java.get()}\"")
    buildConfigField("String", "PROTOKT_VERSION", "\"$version\"")
}
