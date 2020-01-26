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
    jcenter()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:3.25.0")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.10")
    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.21.2")
    implementation("com.google.guava:guava:28.1-jre")
    implementation(kotlin("gradle-plugin-api"))
}
