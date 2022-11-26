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
    id("protokt.jvm-conventions")
    id("com.gradle.plugin-publish") version "0.11.0"
}

repositories {
    maven(url = "https://jitpack.io")
}

gradlePlugin {
    isAutomatedPublishing = false

    plugins {
        create("protokt") {
            id = "com.toasttab.protokt"
            implementationClass = "com.toasttab.protokt.gradle.plugin.ProtoktPlugin"
            displayName = ProtoktProjectInfo.name
            description = ProtoktProjectInfo.description
        }
    }
}

pluginBundle {
    mavenCoordinates {
        group = "${project.group}"
    }
    website = ProtoktProjectInfo.url
    vcsUrl = ProtoktProjectInfo.url
    description = ProtoktProjectInfo.description
    tags = listOf("protobuf", "kotlin")
}

ext[com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY] = System.getenv("GRADLE_PORTAL_PUBLISH_KEY")
ext[com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET] = System.getenv("GRADLE_PORTAL_PUBLISH_SECRET")

tasks.named("publishPlugins") {
    enabled = isRelease()
}

enablePublishing()

dependencies {
    implementation(project(":protokt-util"))
    implementation(kotlin("gradle-plugin"))
    implementation(gradleApi())
    implementation(libs.protobufGradlePlugin)
}

includeBuildSrc(
    "com/toasttab/protokt/gradle/*",
    "com/google/protobuf/gradle/*"
)
