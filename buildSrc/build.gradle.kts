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

plugins {
    `kotlin-dsl`
    alias(libs.plugins.buildConfig)
}

buildConfig {
    useKotlinOutput { topLevelConstants = true }
    packageName.set("protokt.v1.gradle")
    buildConfigField(String::class.java, "DEFAULT_PROTOBUF_VERSION", libs.versions.protobuf.java.get())
    buildConfigField(String::class.java, "PROTOKT_VERSION", version.toString())
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(libs.androidGradlePlugin)
    implementation(libs.binaryCompatibilityValidator)
    implementation(libs.expediter)
    implementation(libs.gradleMavenPublishPlugin)
    implementation(libs.kotlinGradlePlugin)
    implementation("org.jetbrains.kotlin:kotlin-allopen:${libs.versions.kotlin.get()}")
    implementation(libs.kotlinx.benchmark.plugin)
    implementation(libs.protobuf.gradlePlugin)
    implementation(libs.spotlessGradlePlugin)
    implementation(kotlin("gradle-plugin-api"))
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // kotlinx-benchmark-plugin pulls an older KotlinPoet that's binary-incompatible
    // with the version used by protokt-codegen; force alignment
    constraints {
        implementation(libs.kotlinPoet)
    }
}

sourceSets {
    main {
        java {
            srcDir("../shared-src/codegen")
            srcDir("../shared-src/gradle-plugin")
        }
    }
}
