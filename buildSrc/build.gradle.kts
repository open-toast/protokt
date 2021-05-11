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

plugins {
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:5.12.4")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.16")
    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("ru.vyarus:gradle-animalsniffer-plugin:1.5.0")
    implementation("org.jetbrains.kotlinx:binary-compatibility-validator:0.5.0")
    implementation(kotlin("gradle-plugin-api"))
}
