/*
 * Copyright (c) 2021 Toast Inc.
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

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

fun Project.trackKotlinApiCompatibility() {
    apply(plugin = "binary-compatibility-validator")

    tasks.named("apiCheck") {
        enabled = false
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("api") {
                artifact("$buildDir/api/${project.name}.api") {
                    extension = "api"
                    classifier = "ktapi"
                    builtBy(tasks.named("apiDump"))
                }

                artifactId = project.name
                version = "${project.version}"
                groupId = "$group"
            }
        }
    }
}
