/*
 * Copyright (c) 2024 Toast, Inc.
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
import com.google.protobuf.gradle.proto
import org.gradle.api.distribution.plugins.DistributionPlugin.TASK_INSTALL_NAME

plugins {
    id("protokt.jvm-conventions")
    application
}

localProtokt(false)

dependencies {
    implementation(project(":protokt-protovalidate"))
    implementation(project(":protokt-reflect"))
    implementation(kotlin("reflect"))
    implementation(libs.cel)
    implementation(libs.classgraph)
    implementation(libs.protovalidateJava)

    testImplementation(project(":testing:testing-util"))
    testImplementation(libs.truth)
}

sourceSets.main {
    proto {
        srcDir(project.layout.buildDirectory.file("protovalidate/export"))
    }
}

val protovalidateVersion = libs.versions.protovalidate.get()
val gobin = project.layout.buildDirectory.file("gobin").get().asFile.absolutePath
val bufExecutable = project.layout.buildDirectory.file("gobin/buf").get().asFile
val conformanceExecutable = project.layout.buildDirectory.file("gobin/protovalidate-conformance").get().asFile

val installBuf =
    tasks.register<Exec>("installBuf") {
        environment("GOBIN", gobin)
        outputs.file(bufExecutable)
        commandLine("go", "install", "github.com/bufbuild/buf/cmd/buf@v${libs.versions.buf.get()}")
    }

val downloadConformanceProtos =
    tasks.register<Exec>("downloadConformanceProtos") {
        dependsOn(installBuf)
        commandLine(
            bufExecutable,
            "export",
            "buf.build/bufbuild/protovalidate-testing:v$protovalidateVersion",
            "--output=build/protovalidate/export"
        )
    }

tasks.withType<GenerateProtoTask> {
    dependsOn(downloadConformanceProtos)
}

val installConformance =
    tasks.register<Exec>("installProtovalidateConformance") {
        environment("GOBIN", gobin)
        outputs.file(conformanceExecutable)
        commandLine(
            "go",
            "install",
            "github.com/bufbuild/protovalidate/tools/protovalidate-conformance@v$protovalidateVersion"
        )
    }

application {
    mainClass.set("protokt.v1.buf.validate.conformance.Main")
}

tasks {
    test {
        systemProperty("conformance-runner", conformanceExecutable.absolutePath)
        outputs.upToDateWhen { false }
        dependsOn(installConformance, TASK_INSTALL_NAME)
        testLogging.showStandardStreams = true
        maxHeapSize = "512m"
        jvmArgs = listOf("-XX:MaxPermSize=512m")
    }
}
