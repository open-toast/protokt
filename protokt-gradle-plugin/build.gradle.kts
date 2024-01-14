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
    id("protokt.jvm-conventions")
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.pluginPublish)
}

gradlePlugin {
    website.set(ProtoktProjectInfo.url)
    vcsUrl.set(ProtoktProjectInfo.url)

    plugins {
        create("protokt") {
            id = "com.toasttab.protokt"
            implementationClass = "protokt.v1.gradle.ProtoktPlugin"
            displayName = ProtoktProjectInfo.name
            description = ProtoktProjectInfo.description
            tags.set(listOf("protobuf", "kotlin"))
        }
    }
}

ext[com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY] = System.getenv("GRADLE_PORTAL_PUBLISH_KEY")
ext[com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET] = System.getenv("GRADLE_PORTAL_PUBLISH_SECRET")

tasks.named("publishPlugins") {
    enabled = isRelease()
}

enablePublishing(defaultJars = false)

publishing {
    publications {
        create<MavenPublication>("main") {
            from(components.getByName("java"))
            artifactId = project.name
            version = project.version.toString()
            groupId = project.group.toString()
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(gradleApi())
    implementation(libs.protobuf.gradlePlugin)
}

sourceSets {
    main {
        java {
            srcDir(rootProject.file("shared-src/codegen"))
            srcDir(rootProject.file("shared-src/gradle-plugin"))
        }
    }
}

buildConfig {
    useKotlinOutput { topLevelConstants = true }
    packageName.set("protokt.v1.gradle")
    buildConfigField("String", "DEFAULT_PROTOBUF_VERSION", "\"${libs.versions.protobuf.java.get()}\"")
    buildConfigField("String", "PROTOKT_VERSION", "\"$version\"")
}
