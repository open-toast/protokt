/*
 * Copyright (c) 2022 Toast, Inc.
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

import org.gradle.kotlin.dsl.add
import protokt.v1.gradle.Os

plugins {
    id("protokt.jvm-conventions")
}

repositories {
    ivy {
        setUrl("https://github.com/ogolberg/")
        patternLayout {
            artifact("/[organization]/releases/download/[revision]/[artifact]-[classifier]-[revision]")
        }

        metadataSources {
            artifact()
        }

        content {
            content {
                includeGroup("build-protobuf-conformance-runner")
            }
        }
    }
}

configurations.create("conformance")

// protobuf-java version is [java-specific major version].[protobuf version], e.g. 4.26.1
// the conformance runner version is just [protobuf version], e.g. 26.1
val conformanceVersion = libs.versions.protobuf.java.get().replace(Regex("^\\d+\\."), "")

dependencies {
    testImplementation(project(":testing:testing-util"))
    testImplementation(project(":protokt-json"))
    add("conformance", "build-protobuf-conformance-runner:conformance_test_runner:$conformanceVersion") {
        artifact {
            extension = "exe"
            classifier = Os.current.conformanceClassifier
        }
    }
}

tasks.register<Copy>("setupRunner") {
    from(configurations.getAt("conformance"))
    into(layout.buildDirectory.dir("bin"))
    filePermissions {
        user {
            read = true
            execute = true
        }
    }
}

tasks {
    test {
        systemProperty("conformance-runner", layout.buildDirectory.dir("bin").get().file("conformance_test_runner-$conformanceVersion-${Os.current.conformanceClassifier}.exe").asFile.path)

        outputs.upToDateWhen { false }

        dependsOn("setupRunner")
        dependsOn(":testing:conformance:js-ir:compileProductionExecutableKotlinJs")
        dependsOn(":testing:conformance:jvm:installDist")
    }
}
