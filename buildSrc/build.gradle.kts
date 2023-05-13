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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
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
    implementation(libs.protobufGradlePlugin)
    implementation(libs.spotlessGradlePlugin)
    implementation(kotlin("gradle-plugin-api"))
}

tasks.withType<KotlinCompile> {
    dependsOn("generateVersions")
}

val versionOutputDir = file("$buildDir/generated-sources/protobuf-version")

sourceSets["main"].java.srcDir(versionOutputDir)

tasks.register("generateVersions") {
    doFirst {
        val protobuf = File(versionOutputDir, "com/toasttab/protokt/v1/gradle/ProtobufVersion.kt")
        protobuf.parentFile.mkdirs()
        protobuf.writeText(
            """
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
                 
                package com.toasttab.protokt.v1.gradle
                
                const val DEFAULT_PROTOBUF_VERSION = "${libs.versions.protobuf.get()}"
            """.trimIndent()
        )

        val protokt = File(versionOutputDir, "com/toasttab/protokt/v1/gradle/ProtoktVersion.kt")
        protokt.parentFile.mkdirs()
        protokt.writeText(
            """
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

                package com.toasttab.protokt.v1.gradle

                const val PROTOKT_VERSION = "$version"
            """.trimIndent()
        )
    }
}
