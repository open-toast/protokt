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

import build.buf.gradle.BUF_BINARY_CONFIGURATION_NAME
import build.buf.gradle.BUF_BUILD_DIR
import build.buf.gradle.FormatCheckTask
import build.buf.gradle.LintTask
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.proto
import org.gradle.api.distribution.plugins.DistributionPlugin.TASK_INSTALL_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME

plugins {
    id("protokt.jvm-conventions")
    alias(libs.plugins.bufGradlePlugin)
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

    testImplementation(libs.truth)
}

val protovalidateVersion = libs.versions.protovalidate.get()

val downloadConformanceProtos =
    tasks.register<Exec>("downloadConformanceProtos") {
        commandLine(
            configurations.getByName(BUF_BINARY_CONFIGURATION_NAME).singleFile.absolutePath,
            "export",
            "buf.build/bufbuild/protovalidate-testing:v$protovalidateVersion",
            "--output=build/$BUF_BUILD_DIR/export"
        )
    }

tasks.withType<GenerateProtoTask> {
    dependsOn(downloadConformanceProtos)
}

sourceSets.main {
    proto {
        srcDir(project.layout.buildDirectory.file("$BUF_BUILD_DIR/export"))
    }
}

val conformanceExecutable = project.layout.buildDirectory.file("gobin/protovalidate-conformance").get().asFile

val installConformance =
    tasks.register<Exec>("installProtovalidateConformance") {
        environment("GOBIN", project.layout.buildDirectory.file("gobin").get().asFile.absolutePath)
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

val conformance =
    tasks.register<Exec>("conformance") {
        dependsOn(TASK_INSTALL_NAME, installConformance)
        commandLine(
            conformanceExecutable.absolutePath,
            "--strict_message",
            "--strict_error",
            project.layout.buildDirectory
                .file("install/protovalidate-conformance/bin/protovalidate-conformance")
                .get()
                .asFile
                .absolutePath
        )
    }

tasks.named(CHECK_TASK_NAME).dependsOn(conformance)

tasks.withType<LintTask> {
    enabled = false
}

tasks.withType<FormatCheckTask> {
    enabled = false
}
