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

package com.toasttab.protokt.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet

const val CODEGEN_NAME = "protoc-gen-protokt"

const val EXTENSIONS = "protoktExtensions"

const val TEST_EXTENSIONS = "testProtoktExtensions"

fun configureProtokt(project: Project, resolveBinary: () -> String) {
    project.createExtensionConfigurations()

    val ext = project.extensions.create<ProtoktExtension>("protokt")

    configureProtobufPlugin(project, ext, resolveBinary())
}

private fun Project.createExtensionConfigurations() {
    val extensionsConfiguration = configurations.create(EXTENSIONS)
    val testExtensionsConfiguration = configurations.create(TEST_EXTENSIONS)

    val isMultiplatform = project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

    when {
        isMultiplatform -> {
            val sourceSets = extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets
            val sourceSet = (sourceSets.getByName("commonMain") as DefaultKotlinSourceSet)
            configurations.getByName(sourceSet.apiConfigurationName)
                .extendsFrom(extensionsConfiguration)
            val testSourceSet = (sourceSets.getByName("commonTest") as DefaultKotlinSourceSet)
            configurations.getByName(testSourceSet.apiConfigurationName)
                .extendsFrom(testExtensionsConfiguration)
        }
        else -> {
            configurations.getByName("api").extendsFrom(extensionsConfiguration)
            configurations.getByName("testApi").extendsFrom(testExtensionsConfiguration)
        }
    }
}

fun Project.resolveProtoktCoreDep() =
    if (the<ProtoktExtension>().lite) {
        "protokt-core-lite"
    } else {
        "protokt-core"
    }
