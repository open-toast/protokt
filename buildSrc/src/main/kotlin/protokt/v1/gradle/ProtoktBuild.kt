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

package protokt.v1.gradle

import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.outputSourceDirectoriesHack
import com.google.protobuf.gradle.proto
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
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

fun configureProtokt(
    project: Project,
    protoktVersion: Any?,
    disableJava: Boolean = true,
    resolveBinary: () -> String
) {
    injectKotlinPluginsIntoProtobufGradle()
    val ext = project.extensions.create<ProtoktExtension>("protokt")
    configureProtobufPlugin(project, ext, disableJava, resolveBinary())

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

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        configureProtoktConfigurations(KotlinMultiplatformExtension::class, "commonMain", "commonTest")
        linkGenerateProtoToSourceCompileForKotlinMpp("commonMain", "commonTest")
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.js") {
        configureProtoktConfigurations(KotlinJsProjectExtension::class, "main", "test")
        val (mainSourceSetName, testSourceSetName) = configureJs()

        // hacking into hack configurations owned by protobbuf-gradle-plugin
        configurations.getByName("${mainSourceSetName}CompileProtoPath").extendsFrom(extensionsConfiguration)
        configurations.getByName("${testSourceSetName}CompileProtoPath").extendsFrom(testExtensionsConfiguration)
    }

    val otherwise = {
        configurations.getByName("api").extendsFrom(extensionsConfiguration)
        configurations.getByName("testApi").extendsFrom(testExtensionsConfiguration)
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        otherwise()
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        otherwise()
    }
}

private fun Project.linkGenerateProtoToSourceCompileForKotlinMpp(mainSourceSetName: String, testSourceSetName: String) {
    linkGenerateProtoTasksAndIncludeGeneratedSource(mainSourceSetName, false)
    linkGenerateProtoTasksAndIncludeGeneratedSource(testSourceSetName, true)

    tasks.withType<Jar> {
        from(fileTree("${layout.buildDirectory.get()}/extracted-protos/main"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // TODO: figure out how to get rid of this
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

// The Kotlin JS plugin doesn't create main or test source sets
private fun Project.configureJs(): Pair<String, String> {
    // Can't call them "main" and "test" otherwise we'll get conflicting tasks 'processResources' and 'testClasses'
    val mainSourceSet = the<SourceSetContainer>().create("jsMain")
    val testSourceSet = the<SourceSetContainer>().create("jsTest")

    val extension = project.extensions.getByType<ProtobufExtension>()

    fun linkGenerateProtoTasksAndIncludeGeneratedSource(sourceSet: SourceSet, test: Boolean) {
        val targetSourceSetName = if (test) "test" else "main"

        extension.generateProtoTasks.ofSourceSet(sourceSet.name).forEach { genProtoTask ->
            configureSourceSets(targetSourceSetName, sourceSet.name, genProtoTask)
            the<SourceSetContainer>().getByName(sourceSet.name).proto { srcDir("src/$targetSourceSetName/proto") }
            tasks.withType<AbstractKotlinCompile<*>> {
                if ((test && name.contains("Test")) || (!test && !name.contains("Test"))) {
                    dependsOn(genProtoTask)
                }
            }
        }
    }

    linkGenerateProtoTasksAndIncludeGeneratedSource(mainSourceSet, false)
    linkGenerateProtoTasksAndIncludeGeneratedSource(testSourceSet, true)

    tasks.withType<Jar> {
        from(fileTree("${layout.buildDirectory.get()}/extracted-protos/${mainSourceSet.name}"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // TODO: figure out how to get rid of this
    }

    tasks.register("generateProto") { dependsOn("generateJsMainProto") }
    tasks.register("generateTestProto") { dependsOn("generateJsTestProto") }

    return mainSourceSet.name to testSourceSet.name
}

internal fun Project.resolveProtoktCoreDep(protoktVersion: Any?): Dependency? {
    if (name in setOf("protokt-core", "protokt-core-lite")) {
        return null
    }

    val artifactId =
        if (the<ProtoktExtension>().generate.descriptors) {
            "protokt-core"
        } else {
            "protokt-core-lite"
        }

    return if (protoktVersion == null) {
        dependencies.project(":$artifactId")
    } else {
        dependencies.create("com.toasttab.protokt:$artifactId:$protoktVersion")
    }
}

internal fun Project.appliedKotlinPlugin() =
    when {
        plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") ->
            "org.jetbrains.kotlin.multiplatform"
        plugins.hasPlugin("org.jetbrains.kotlin.js") ->
            "org.jetbrains.kotlin.js"
        plugins.hasPlugin("org.jetbrains.kotlin.jvm") ->
            "org.jetbrains.kotlin.jvm"
        plugins.hasPlugin("org.jetbrains.kotlin.android") ->
            "org.jetbrains.kotlin.android"
        else -> null
    }
