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

plugins {
    id("protokt.jvm-conventions")
}

repositories {
    ivy {
        setUrl("https://github.com/ogolberg/")
        patternLayout {
            artifact("/[organization]/releases/download/v[revision]-0/[artifact]-[classifier]-v[revision]")
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

object Os {
    const val MACOS_CLASSIFIER = "macos-latest"
    const val LINUX_CLASSIFIER = "ubuntu-latest"

    val CLASSIFIER by lazy {
        val os = System.getProperty("os.name").lowercase()

        if (os.contains("linux")) {
            LINUX_CLASSIFIER
        } else if (os.contains("mac")) {
            MACOS_CLASSIFIER
        } else {
            error("conformance tests cannot run on $os")
        }
    }
}

dependencies {
    testImplementation(project(":testing:testing-util"))
    add("conformance", "build-protobuf-conformance-runner:conformance_test_runner:${libs.versions.protobuf.java.get()}") {
        artifact {
            extension = "exe"
            classifier = Os.CLASSIFIER
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
        systemProperty("conformance-runner", layout.buildDirectory.dir("bin").get().file("conformance_test_runner-${libs.versions.protobuf.java.get()}-${Os.CLASSIFIER}.exe").asFile.path)

        outputs.upToDateWhen { false }

        dependsOn("setupRunner")
        dependsOn(":testing:conformance:js-ir:compileProductionExecutableKotlinJs")
        dependsOn(":testing:conformance:jvm:installDist")
    }
}
