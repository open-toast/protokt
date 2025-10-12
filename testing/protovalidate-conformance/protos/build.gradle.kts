/*
 * Copyright (c) 2025 Toast, Inc.
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
import protokt.v1.gradle.ProtoktExtension

plugins {
    id("protokt.jvm-conventions")
}

localProtokt(false)

sourceSets.main {
    proto {
        srcDir(project.layout.buildDirectory.file("protovalidate/export"))
    }
}

configure<ProtoktExtension> {
    generate {
        // lots of protos; this would take a long time
        formatOutput = false
    }
}

dependencies {
    implementation(libs.protobuf.java)
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
