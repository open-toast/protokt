/*
 * Copyright (c) 2020 Toast Inc.
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

package com.toasttab.protokt.shared

import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the

private fun Project.sourceMainProto() =
    the<JavaPluginConvention>().sourceSets
        .getByName("main").extensions.getByName("proto") as SourceDirectorySet

internal fun configurePublishingTasks(project: Project, ext: ProtoktExtension) {
    val task = project.tasks.register<Zip>("packageProto") {
        from(project.sourceMainProto())

        archiveFileName.set("${project.name}-${project.version}-proto.zip")
        destinationDirectory.set(project.file("${project.buildDir}/dist"))
    }

    project.afterEvaluate {
        if (ext.publishProto) {
            project.convention.findByType<PublishingExtension>()?.apply {
                publications {
                    create<MavenPublication>("proto") {
                        artifact(
                            mapOf(
                                "source" to task.get(),
                                "extension" to "zip",
                                "classifier" to "proto"
                            )
                        )
                        artifactId = project.name
                    }
                }
            }
        }
    }
}
