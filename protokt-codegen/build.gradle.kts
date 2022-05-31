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

import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import com.toasttab.protokt.gradle.CODEGEN_NAME

plugins {
    id("protokt.jvm-conventions")
    id("com.google.protobuf")
    application
}

enablePublishing(defaultJars = false)

application {
    applicationName = CODEGEN_NAME
    mainClass.set("com.toasttab.protokt.MainKt")
}

dependencies {
    implementation(project(":extensions:protokt-extensions-api"))
    implementation(project(":protokt-runtime"))
    implementation(project(":protokt-runtime-grpc"))
    implementation(project(":protokt-util"))

    implementation(kotlin("reflect"))

    implementation(libraries.arrow)
    implementation(libraries.grpcStub)
    implementation(libraries.kotlinPoet)
    implementation(libraries.kotlinxCollections)
    implementation(libraries.kotlinxCoroutinesCore)
    implementation(libraries.protobufJava)

    testImplementation(project(":testing:testing-util"))

    testImplementation(libraries.junit)
    testImplementation(libraries.truth)

    protobuf(files("../extensions/protokt-extensions-lite/src/main/proto"))
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

protobuf {
    protoc {
        artifact = libraries.protoc
    }
}

tasks.withType<Test> {
    doFirst {
        tasks.named<GenerateProtoTask>("generateProto") {
            environment("PROTOC_PATH", locatorToAlternativePathsMapping.getting("protoc").get().singleFile)
        }
    }
}
