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

plugins {
    id("protokt.common-conventions")
    id("com.android.library")
    `kotlin-android`
}

javaBasedProjectConventions()

repositories {
    google()
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

// remove after AGP 8.1.0
// https://issuetracker.google.com/issues/260059413
android {
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.java.get().toInt()}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.java.get().toInt()}")
    }
}
