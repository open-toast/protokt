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

import com.toasttab.protokt.shared.configureProtokt
import org.gradle.api.Plugin
import org.gradle.api.Project

class ProtoktPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        configureProtokt(project) { ext ->
            project.configurations.named("api") {
                it.dependencies.add(
                    project.dependencies.create("com.toasttab.protokt:protokt-core:${ext.version}")
                )
            }

            binaryFromArtifact(project, ext.version)
        }
    }
}
