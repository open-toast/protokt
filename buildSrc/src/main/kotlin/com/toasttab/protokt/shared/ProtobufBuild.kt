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

import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import com.google.protobuf.gradle.remove
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the

internal fun configureProtobufPlugin(project: Project, ext: ProtoktExtension, binaryPath: String) {
    project.apply(plugin = "com.google.protobuf")

    project.protobuf {
        generatedFilesBaseDir = "${project.buildDir}/generated-sources"

        project.the<JavaPluginConvention>().sourceSets.main.kotlin
            .srcDirs.add(project.file("$generatedFilesBaseDir/main/protokt"))

        protoc {
            artifact = "com.google.protobuf:protoc:${ext.protocVersion}"
        }

        plugins {
            id("protokt") {
                path = binaryPath
            }
        }

        generateProtoTasks {
            for (task in all()) {
                task.builtins {
                    remove("java")
                }

                task.plugins {
                    id("protokt") {
                        val classpath = project.configurations.getByName("protoktExtensions").asPath.replace(':', ';')

                        option(
                            "kotlin_extra_classpath=$classpath"
                        )
                    }
                }
            }
        }
    }
}
