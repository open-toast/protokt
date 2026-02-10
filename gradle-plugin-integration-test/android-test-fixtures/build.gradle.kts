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

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.toasttab.protokt.v1")
}

android {
    namespace = "protokt.v1.testing.android"
    compileSdk = 31

    testFixtures {
        enable = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

protokt {
    generate { lite() }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly(libs.junit.platformLauncher)
    testRuntimeOnly(libs.protobuf.lite)
}
