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
    id("org.jetbrains.kotlin.multiplatform")
}

enablePublishing(defaultJars = false)
trackKotlinApiCompatibility()

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                compileOnly(libraries.protobufJava)

                testImplementation(libraries.junit)
                testImplementation(libraries.truth)
            }
        }
    }

    val publicationsFromMainHost =
        listOf(jvm()).map { it.name } + "kotlinMultiplatform"

    configure<PublishingExtension> {
        publications {
            matching { it.name in publicationsFromMainHost }.all {
                val targetPublication = this@all
                tasks.withType<AbstractPublishToMaven>()
                    .matching { it.publication == targetPublication }
                    .configureEach { onlyIf { findProperty("isMainHost") == "true" } }
            }
        }
    }
}
