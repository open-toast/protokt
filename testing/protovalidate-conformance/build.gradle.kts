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

import org.gradle.api.distribution.plugins.DistributionPlugin.TASK_INSTALL_NAME

plugins {
    id("protokt.jvm-conventions")
    application
}

dependencies {
    implementation(project(":protokt-protovalidate"))
    implementation(project(":protokt-reflect"))
    implementation(project(":testing:protovalidate-conformance:protos"))
    implementation(kotlin("reflect"))
    implementation(libs.cel)
    implementation(libs.classgraph)
    implementation(libs.protovalidateJava)

    testImplementation(project(":testing:testing-util"))
    testImplementation(libs.truth)
}

val protovalidateVersion = libs.versions.protovalidate.get()
val gobin = project.layout.buildDirectory.file("gobin").get().asFile.absolutePath
val conformanceExecutable = project.layout.buildDirectory.file("gobin/protovalidate-conformance").get().asFile

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

val lazyBufImpl: String by project

val conformance =
    tasks.register<Exec>("conformance") {
        dependsOn(TASK_INSTALL_NAME, installConformance)
        description = "Runs protovalidate conformance tests."
        environment(
            "JAVA_OPTS" to "-Xmx64M",
            "GOMEMLIMIT" to "40MiB",
            "LAZY_BUF_IMPL" to lazyBufImpl
        )
        commandLine(
            conformanceExecutable.absolutePath,
            "--strict_message",
            "--strict_error",
            "--expected_failures",
            "expected_failures.yaml",
            project.layout.buildDirectory.dir("install/${project.name}/bin/${project.name}").get().asFile.absolutePath
        )
    }

// Unstable in CI.
// Run locally with `./gradlew :testing:protovalidate-conformance:conformance`
// tasks.test { dependsOn(conformance) }

application {
    mainClass.set("protokt.v1.buf.validate.conformance.Main")
}
