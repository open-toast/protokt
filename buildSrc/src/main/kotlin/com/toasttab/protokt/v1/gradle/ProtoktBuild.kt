/*
 * Copyright (c) 2019 Toast, Inc.
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

package com.toasttab.protokt.v1.gradle

import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.outputSourceDirectoriesHack
import com.google.protobuf.gradle.proto
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import kotlin.reflect.KClass

const val CODEGEN_NAME = "protoc-gen-protokt"

const val EXTENSIONS = "protoktExtensions"

const val TEST_EXTENSIONS = "testProtoktExtensions"

fun configureProtokt(project: Project, protoktVersion: Any?, resolveBinary: () -> String) {
    injectKotlinPluginsIntoProtobufGradle()
    val ext = project.extensions.create<ProtoktExtension>("protokt")
    configureProtobufPlugin(project, ext, resolveBinary())

    project.createExtensionConfigurationsAndConfigureProtobuf()

    // must wait for extension to resolve
    project.afterEvaluate {
        project.configurations.named(EXTENSIONS) {
            project.resolveProtoktCoreDep(protoktVersion)?.let(dependencies::add)
        }
    }
}

private fun injectKotlinPluginsIntoProtobufGradle() {
    val prerequisitePluginsField = ProtobufPlugin::class.java.getDeclaredField("PREREQ_PLUGIN_OPTIONS")
    prerequisitePluginsField.isAccessible = true

    @Suppress("UNCHECKED_CAST")
    val prerequisitePlugins = prerequisitePluginsField.get(null) as MutableList<String>
    prerequisitePlugins.add("org.jetbrains.kotlin.multiplatform")
    prerequisitePlugins.add("org.jetbrains.kotlin.js")
}

private fun Project.linkGenerateProtoToSourceCompileForKotlinJsOrMpp() {
    val sourceSetPrefix =
        if (isMultiplatform()) {
            "common"
        } else {
            ""
        }

    val mainSourceSetName = if (sourceSetPrefix.isEmpty()) "main" else sourceSetPrefix + "Main"
    val testSourceSetName = if (sourceSetPrefix.isEmpty()) "test" else sourceSetPrefix + "Test"

    linkGenerateProtoTasksAndIncludeGeneratedSource(mainSourceSetName, false)
    linkGenerateProtoTasksAndIncludeGeneratedSource(testSourceSetName, true)

    tasks.withType<Jar> {
        from(fileTree("$buildDir/extracted-protos/main"))
    }
}

private fun Project.linkGenerateProtoTasksAndIncludeGeneratedSource(sourceSetName: String, test: Boolean) {
    val protoSourceSetRoot = if (test) "test" else "main"

    val extension = project.extensions.getByType<ProtobufExtension>()
    extension.generateProtoTasks.ofSourceSet(protoSourceSetRoot).forEach { genProtoTask ->
        configureSourceSets(sourceSetName, protoSourceSetRoot, genProtoTask)
        tasks.withType<AbstractKotlinCompile<*>> {
            if ((test && name.contains("Test")) || (!test && !name.contains("Test"))) {
                dependsOn(genProtoTask)
            }
        }
    }
}

private fun Project.configureSourceSets(
    sourceSetName: String,
    protoSourceSetRoot: String,
    genProtoTask: GenerateProtoTask
) {
    val kotlinExtension = extensions.getByName("kotlin") as ExtensionAware

    @Suppress("UNCHECKED_CAST")
    val sourceSets = kotlinExtension.extensions.getByName("sourceSets") as NamedDomainObjectContainer<KotlinSourceSet>

    sourceSets.named(sourceSetName).configure {
        kotlin.srcDir(genProtoTask.buildSourceDirectorySet())
        the<SourceSetContainer>()
            .getByName(protoSourceSetRoot)
            .proto { resources.source(this) }
    }
}

private fun GenerateProtoTask.buildSourceDirectorySet(): SourceDirectorySet {
    val srcSetName = "generate-proto-$name"
    val srcSet = objectFactory.sourceDirectorySet(srcSetName, srcSetName)
    srcSet.srcDirs(
        objectFactory
            .fileCollection()
            .builtBy(this)
            .from(providerFactory.provider { outputSourceDirectoriesHack })
    )
    return srcSet
}

private fun Project.createExtensionConfigurationsAndConfigureProtobuf() {
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
    }

    when {
        isMultiplatform() -> {
            configureProtoktConfigurations(KotlinMultiplatformExtension::class, "commonMain", "commonTest")
            linkGenerateProtoToSourceCompileForKotlinJsOrMpp()
        }
        isJs() -> {
            afterEvaluate {
                configureProtoktConfigurations(KotlinJsProjectExtension::class, "main", "test")
                linkGenerateProtoToSourceCompileForKotlinJsOrMpp()
            }
        }
        else -> {
            configurations.getByName("api").extendsFrom(extensionsConfiguration)
            configurations.getByName("testApi").extendsFrom(testExtensionsConfiguration)
        }
    }
}

internal fun Project.resolveProtoktCoreDep(protoktVersion: Any?): Dependency? {
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

private fun createProtoSourceSetsIfNeeded(project: Project) {
    if (project.isMultiplatform()) {
        with(project.the<SourceSetContainer>()) {
            if (none { it.name == "main" }) {
                create("main")
            }
            if (none { it.name == "test" }) {
                create("test")
            }
        }
    }
}
