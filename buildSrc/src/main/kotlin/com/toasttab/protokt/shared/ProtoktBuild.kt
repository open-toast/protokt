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

package com.toasttab.protokt.shared

import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

fun configureProtokt(project: Project, resolveBinary: (ext: ProtoktExtension) -> String) {
    createExtensionConfiguration(project)

    val ext = project.extensions.create<ProtoktExtension>("protokt")

    configureProtobufPlugin(project, ext, resolveBinary(ext))
    configurePublishingTasks(project, ext)
}

private fun createExtensionConfiguration(project: Project) {
    val extensionsConfiguration = project.configurations.create("protoktExtensions")

    project.configurations.named("implementation") {
        extendsFrom(extensionsConfiguration)
    }
}
