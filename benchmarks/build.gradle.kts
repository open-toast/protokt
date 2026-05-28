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

import de.undercouch.gradle.tasks.download.Download

plugins {
    alias(libs.plugins.download)
}

val archive = layout.buildDirectory.file("datasets-${libs.versions.datasets.get()}.zip")

tasks.register<Download>("download") {
    enabled = !archive.get().asFile.exists()

    src("https://proto-benchmarks.s3.amazonaws.com/datasets-${libs.versions.datasets.get()}.zip")
    tempAndMove(true)
    dest(layout.buildDirectory)
}

tasks.register<Copy>("datasets") {
    dependsOn("download")
    enabled = !archive.get().asFile.exists()

    from(zipTree(archive))
    into(layout.buildDirectory.file("datasets"))
}

tasks.register("run") {
    dependsOn(
        "protobuf-java-benchmarks:run",
        "protokt-benchmarks:run",
        "wire-benchmarks:run"
    )
}
