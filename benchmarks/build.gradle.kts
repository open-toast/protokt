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

import de.undercouch.gradle.tasks.download.Download

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath(libraries.gradleDownload)
    }
}

plugins {
    id("de.undercouch.download")
}

val archive = file("$buildDir/datasets-${versions.datasets}.zip")

tasks.register<Download>("download") {
    enabled = !archive.exists()

    src("https://proto-benchmarks.s3.amazonaws.com/datasets-${versions.datasets}.zip")
    tempAndMove(true)
    dest(buildDir)
}

tasks.register<Copy>("datasets") {
    dependsOn("download")
    enabled = !archive.exists()

    from(zipTree(archive))
    into(file("$buildDir/datasets"))
}

tasks.register("run") {
    dependsOn(
        "protobuf-java:run",
        "protokt:run",
        "wire:run"
    )
}
