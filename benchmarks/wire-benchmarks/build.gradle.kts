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

import com.squareup.wire.gradle.WireExtension

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libraries.wireGradle)
    }
}

plugins {
    application
}

apply(plugin = "com.squareup.wire")

application {
    mainClass.set("com.toasttab.protokt.benchmarks.WireBenchmarksKt")
    executableDir = ".."
}

dependencies {
    implementation(project(":benchmarks:benchmarks-util"))
    implementation(libraries.wireRuntime)
}

configure<WireExtension> {
    sourcePath("../schema/src/main/resources")

    kotlin {
        out = "$buildDir/generated-sources/proto/main/java"
    }
}

sourceSets {
    main {
        java.srcDirs.add(file("$buildDir/generated-sources/proto/main/java"))
    }
}

tasks.named("run") {
    dependsOn(":benchmarks:datasets")
}
