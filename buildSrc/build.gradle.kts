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
    buildConfigField("String", "DEFAULT_PROTOBUF_VERSION", "\"${libs.versions.protobuf.java.get()}\"")
    buildConfigField("String", "PROTOKT_VERSION", "\"$version\"")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(libs.androidGradlePlugin)
    implementation(libs.animalSnifferGradlePlugin)
    implementation(libs.binaryCompatibilityValidator)
    implementation(libs.gradleMavenPublishPlugin)
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.protobuf.gradlePlugin)
    implementation(libs.spotlessGradlePlugin)
    implementation(kotlin("gradle-plugin-api"))
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
