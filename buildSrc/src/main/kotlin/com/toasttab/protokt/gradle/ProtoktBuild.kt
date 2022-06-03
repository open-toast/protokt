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
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import kotlin.reflect.KClass

const val CODEGEN_NAME = "protoc-gen-protokt"

const val EXTENSIONS = "protoktExtensions"

const val TEST_EXTENSIONS = "testProtoktExtensions"

fun configureProtokt(project: Project, protoktVersion: String?, resolveBinary: () -> String) {
    project.createExtensionConfigurations()

    val ext = project.extensions.create<ProtoktExtension>("protokt")

    configureProtobufPlugin(project, ext, resolveBinary())

    // must wait for extension to resolve
    project.afterEvaluate {
        project.configurations.named(EXTENSIONS) {
            project.resolveProtoktCoreDep(protoktVersion)?.let(dependencies::add)
        }
    }
}

private fun Project.createExtensionConfigurations() {
    val extensionsConfiguration = configurations.create(EXTENSIONS)
    val testExtensionsConfiguration = configurations.create(TEST_EXTENSIONS)

    fun configureProtoktConfigurations(
        extension: KClass<out KotlinProjectExtension>,
        targetMainSourceSet: String,
        targetTestSourceSet: String
    ) {
        val sourceSets = extensions.getByType(extension.java).sourceSets
        val sourceSet = (sourceSets.getByName(targetMainSourceSet) as DefaultKotlinSourceSet)
        configurations.getByName(sourceSet.apiConfigurationName).extendsFrom(extensionsConfiguration)
        val testSourceSet = (sourceSets.getByName(targetTestSourceSet) as DefaultKotlinSourceSet)
        configurations.getByName(testSourceSet.apiConfigurationName).extendsFrom(testExtensionsConfiguration)
        configurations.create("compileProtoPath").extendsFrom(extensionsConfiguration)
        createProtoSourceSetsIfNeeded()
    }

    when {
        isMultiplatform() -> {
            configureProtoktConfigurations(KotlinMultiplatformExtension::class, "commonMain", "commonTest")
        }
        isJs() -> {
            configureProtoktConfigurations(KotlinJsProjectExtension::class, "main", "test")
        }
        else -> {
            configurations.getByName("api").extendsFrom(extensionsConfiguration)
            configurations.getByName("testApi").extendsFrom(testExtensionsConfiguration)
        }
    }
}

internal fun Project.resolveProtoktCoreDep(protoktVersion: String?): Dependency? {
    if (name in setOf("protokt-core", "protokt-core-lite")) {
        return null
    }

    val artifactId =
        if (the<ProtoktExtension>().lite) {
            "protokt-core-lite"
        } else {
            "protokt-core"
        }

    return if (protoktVersion == null) {
        dependencies.project(":$artifactId")
    } else {
        dependencies.create("com.toasttab.protokt:$artifactId:$protoktVersion")
    }
}

internal fun Project.isMultiplatform() =
    plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

private fun Project.isJs() =
    plugins.hasPlugin("org.jetbrains.kotlin.js")

private fun Project.createProtoSourceSetsIfNeeded() {
    with(the<SourceSetContainer>()) {
        if (none { it.name == "main" }) {
            create("main")
        }
        if (none { it.name == "test" }) {
            create("test")
        }
    }
}
