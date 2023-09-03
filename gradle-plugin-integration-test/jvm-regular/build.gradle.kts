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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.toasttab.protokt.v1")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    if (System.getProperty("kotlin.version", libs.versions.kotlin.get()) == libs.versions.kotlin.get()) {
        withType<KotlinCompile> {
            kotlinOptions {
                allWarningsAsErrors = true
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjvm-default=all")
            }
        }
    }
}

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-jvm-extensions:$version")

    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.protobuf.java)
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
            check(file(common).exists())
            srcDir(common)
        }
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
