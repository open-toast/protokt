/*
 * Copyright (c) 2023 Toast, Inc.
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
    kotlin("kapt")
}

dependencies {
    implementation("com.toasttab.protokt:protokt-core:$version")
    implementation(libs.autoServiceAnnotations)

    testRuntimeOnly(libs.junit.platformLauncher)

    kapt(libs.autoService)
}

// pin to the runtime version of protokt
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())

    compilerOptions {
        // suppress a kapt warning for K2 and Kotlin 2.x
        freeCompilerArgs.add("-Xsuppress-version-warnings")
    }
}
