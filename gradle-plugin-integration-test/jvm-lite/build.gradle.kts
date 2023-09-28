/*
 * Copyright (c) 2021 Toast, Inc.
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

plugins {
    kotlin("jvm")
    id("com.toasttab.protokt.v1")
}

protokt {
    formatOutput = false // https://github.com/pinterest/ktlint/issues/1195
    generate { lite() }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-jvm-extensions-lite:$version")
    protoktExtensions(project(":wrapper-types"))

    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.protobuf.java)
    testImplementation("com.toasttab.protokt:protokt-util:$version")
}

sourceSets {
    main {
        proto {
            srcDir("../multiplatform/src/main/proto")
        }
    }
    test {
        java {
            val common = "../multiplatform/src/commonTest/kotlin"
            val lite = "../../testing/plugin-options/lite/src/test/kotlin/protokt/v1/testing/lite"
            check(file(common).exists())
            check(file(lite).exists())
            srcDir(common)
            srcDir(lite)
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(System.getProperty("java-integration.version", libs.versions.java.get()).toInt()))
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(System.getProperty("java-integration.version", libs.versions.java.get()).toInt()))
    }
}
