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

import com.toasttab.protokt.gradle.CODEGEN_NAME
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

private const val CODEGEN_CONFIGURATION = "protoktCodegen"

internal fun binaryFromArtifact(project: Project): String {
    project.afterEvaluate {
        installBinary(project, configureArtifact(project))
    }

    return "${getTargetDirectory(project)}/bin/$CODEGEN_NAME"
}

private fun installBinary(project: Project, artifact: Dependency) {
    val targetDir = getTargetDirectory(project)

    if (PROTOKT_VERSION.endsWith("-SNAPSHOT") || !targetDir.exists()) {
        targetDir.mkdirs()

        val toolsArchive = project.zipTree(
            project.configurations
                .getByName(CODEGEN_CONFIGURATION)
                .fileCollection(artifact)
                .singleFile
        )

        project.copy {
            from(toolsArchive)
            into(targetDir.parent)
        }
    }
}

private fun configureArtifact(project: Project): Dependency {
    project.configurations.create(CODEGEN_CONFIGURATION)

    return project.dependencies.add(
        CODEGEN_CONFIGURATION,
        mapOf(
            "group" to "com.toasttab.protokt",
            "name" to "protokt-codegen",
            "version" to PROTOKT_VERSION,
            "classifier" to "dist",
            "ext" to "zip"
        )
    )!!
}

private fun getTargetDirectory(project: Project) =
    project.file(
        "${project.rootDir}/.gradle/tools/$CODEGEN_NAME-$PROTOKT_VERSION"
    )
