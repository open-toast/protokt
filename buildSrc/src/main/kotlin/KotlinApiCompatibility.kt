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

import kotlinx.validation.ApiCompareCompareTask
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

fun Project.trackKotlinApiCompatibility(validate: Boolean = true) {
    apply(plugin = "binary-compatibility-validator")

    if (validate) {
        val apiDir = buildDir.resolve("reference-api")

        tasks.named<ApiCompareCompareTask>("apiCheck") {
            projectApiDir = apiDir
            dependsOn("downloadPreviousApiSignatures")
        }

        tasks.register<Copy>("downloadPreviousApiSignatures") {
            selfDependencyHack { group ->
                val ktapiScope = project.configurations.create("ktapi")
                dependencies.add("ktapi", "$group:${project.name}:latest.release:ktapi@api")

                from(ktapiScope.files)
            }

            rename {
                "${project.name}.api"
            }

            into(apiDir)
        }
    } else {
        tasks.named<ApiCompareCompareTask>("apiCheck") {
            enabled = false
        }
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

/**
 * Works around the Gradle bug https://github.com/gradle/gradle/issues/7706
 * to declare a dependency on a published artifact from a prior version.
 */
private fun Project.selfDependencyHack(action: Project.(String) -> Unit) {
    val originalGroup = group

    try {
        group = "_hack_gradle_issues_7706_"
        action.invoke(this, "$originalGroup")
    } finally {
        group = originalGroup
    }
}
