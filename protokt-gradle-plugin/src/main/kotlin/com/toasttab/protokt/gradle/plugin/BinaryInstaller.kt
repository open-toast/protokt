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

package com.toasttab.protokt.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

private const val CODEGEN_CONFIGURATION = "protoktCodegen"
private const val CODEGEN_NAME = "protoc-gen-protokt"

internal fun binaryFromArtifact(project: Project, protoktVersion: String): String {
    project.afterEvaluate {
        installBinary(
            project,
            protoktVersion,
            configureArtifact(project, protoktVersion)
        )
    }

    val target = getTargetDirectory(project, protoktVersion)

    return "$target/bin/$CODEGEN_NAME"
}

private fun installBinary(project: Project, version: String, artifact: Dependency) {
    val targetDir = getTargetDirectory(project, version)

    if (version.endsWith("-SNAPSHOT") || !targetDir.exists()) {
        targetDir.mkdirs()

        val toolsArchive = project.zipTree(
            project.configurations.getByName(CODEGEN_CONFIGURATION).fileCollection(artifact).singleFile
        )

        project.copy {
            it.from(toolsArchive)
            it.into(targetDir.parent)
        }
    }
}

private fun configureArtifact(project: Project, version: String): Dependency {
    project.configurations.create(CODEGEN_CONFIGURATION)

    return project.dependencies.add(CODEGEN_CONFIGURATION, mapOf(
            "group" to "com.toasttab.protokt",
            "name" to "protokt-codegen",
            "version" to version,
            "classifier" to "dist",
            "ext" to "zip"
        )
    )!!
}

private fun getTargetDirectory(project: Project, version: String) =
    project.file("${project.rootDir}/.gradle/tools/$CODEGEN_NAME-$version")
