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
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile

const val CODEGEN_NAME = "protoc-gen-protokt"

const val EXTENSIONS = "protoktExtensions"

const val TEST_EXTENSIONS = "testProtoktExtensions"

internal val DEBUG_LOG_LEVEL = LogLevel.INFO

internal fun configureProtokt(
    project: Project,
    protoktVersion: Any?,
    disableJava: Boolean,
    binary: String
) {
    injectKotlinPluginsIntoProtobufGradle()

    val config = project.createExtensionConfigurations()
    project.configureProtobuf(disableJava, config, binary)

    // must wait for extension to resolve
    project.afterEvaluate {
        listOfNotNull(
            project.resolveProtoktCoreDep(protoktVersion),
            project.resolveProtoktGrpcDep(protoktVersion)
        ).forEach(config.extensions.dependencies::add)
    }
}

private fun injectKotlinPluginsIntoProtobufGradle() {
    val prerequisitePluginsField = ProtobufPlugin::class.java.getDeclaredField("PREREQ_PLUGIN_OPTIONS")
    prerequisitePluginsField.isAccessible = true

    @Suppress("UNCHECKED_CAST")
    val prerequisitePlugins = prerequisitePluginsField.get(null) as MutableList<String>
    prerequisitePlugins.add("org.jetbrains.kotlin.multiplatform")
}

private class Config(
    val extension: ProtoktExtension,
    val extensions: Configuration,
    val testExtensions: Configuration
)

private fun Project.createExtensionConfigurations(): Config {
    val ext = extensions.create<ProtoktExtension>("protokt")
    val extensionsConfiguration = configurations.create(EXTENSIONS)
    val testExtensionsConfiguration = configurations.create(TEST_EXTENSIONS)

    return Config(ext, extensionsConfiguration, testExtensionsConfiguration)
}

private fun Project.configureProtobuf(
    disableJava: Boolean,
    config: Config,
    binary: String
) {
    pluginManager.withPlugin(KotlinPlugins.MULTIPLATFORM) {
        configureForMpp(disableJava, config, binary)
    }

    pluginManager.withPlugin(KotlinPlugins.JVM) {
        configureForJvmLike(config, disableJava, KotlinTarget.Jvm, binary)
    }

    pluginManager.withPlugin(KotlinPlugins.ANDROID) {
        configureForJvmLike(config, disableJava, KotlinTarget.Android, binary)
    }
}

private fun Project.configureForJvmLike(config: Config, disableJava: Boolean, target: KotlinTarget, binary: String) {
    logger.log(DEBUG_LOG_LEVEL, "Configuring protokt for Kotlin ${target.name}")
    configureProtobufPlugin(project, config.extension, disableJava, target, binary)
    configurations.getByName("api").extendsFrom(config.extensions)
    configurations.getByName("testApi").extendsFrom(config.testExtensions)
}

private fun Project.configureForMpp(
    disableJava: Boolean,
    config: Config,
    binary: String
) {
    configureTarget("common", disableJava, config, binary)

    // todo: figure out how to run this after the user specifies their targets without using afterEvaluate
    afterEvaluate {
        @Suppress("DEPRECATION")
        val targets = kotlinExtension.targets
        logger.log(DEBUG_LOG_LEVEL, "Configuring protokt for Kotlin multiplatform for targets: ${targets.map { it.targetName }}")

        targets
            .filterNot { it.targetName == "metadata" }
            .forEach {
                logger.log(DEBUG_LOG_LEVEL, "Handling Kotlin multiplatform target {}", it)
                configureTarget(it.targetName, disableJava, config, binary)
            }
    }
}

private fun Project.configureTarget(
    targetName: String,
    disableJava: Boolean,
    config: Config,
    binary: String
) {
    configureProtobufPlugin(project, config.extension, disableJava, KotlinTarget.fromMultiplatformTargetString(targetName), binary)

    val sourceSets = extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets
    val mainSourceSet = sourceSets.getByName("${targetName}Main")
    val testSourceSet = sourceSets.getByName("${targetName}Test")

    configurations.getByName(mainSourceSet.apiConfigurationName).extendsFrom(config.extensions)
    configurations.getByName(testSourceSet.apiConfigurationName).extendsFrom(config.testExtensions)

    linkGenerateProtoToSourceCompileForKotlinMpp(mainSourceSet, testSourceSet)
}

private fun Project.linkGenerateProtoToSourceCompileForKotlinMpp(mainSourceSet: KotlinSourceSet, testSourceSet: KotlinSourceSet) {
    linkGenerateProtoTasksAndIncludeGeneratedSource(mainSourceSet, false)
    linkGenerateProtoTasksAndIncludeGeneratedSource(testSourceSet, true)

    tasks.withType<Jar> {
        from(fileTree("${layout.buildDirectory.get()}/extracted-protos/main"))
        excludeDuplicates()
    }

    val jsProcessResources = tasks.findByName("jsProcessResources") as Copy?
    jsProcessResources?.excludeDuplicates()
}

// TODO: figure out how to get rid of this?
private fun AbstractCopyTask.excludeDuplicates() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

private fun Project.linkGenerateProtoTasksAndIncludeGeneratedSource(sourceSet: KotlinSourceSet, test: Boolean) {
    val protoSourceSetRoot = if (test) "test" else "main"

    val extension = project.extensions.getByType<ProtobufExtension>()

    extension.generateProtoTasks.ofSourceSet(protoSourceSetRoot).forEach { genProtoTask ->
        sourceSet.kotlin.srcDir(genProtoTask.buildSourceDirectorySet())
        the<SourceSetContainer>()
            .getByName(protoSourceSetRoot)
            .proto { sourceSet.resources.source(this) }

        // todo: it would be better to avoid this by making the outputs of genProtoTask an input of the correct compile task
        tasks.withType<AbstractKotlinCompile<*>> {
            if ((test && name.contains("Test")) || (!test && !name.contains("Test"))) {
                logger.log(DEBUG_LOG_LEVEL, "Making task {} a dependency of {}", genProtoTask.name, name)
                dependsOn(genProtoTask)
            }
        }
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

private fun Project.resolveProtoktCoreDep(protoktVersion: Any?): Dependency? {
    if (name in setOf("protokt-core", "protokt-core-lite")) {
        return null
    }

    return resolveDependency("protokt-core", protoktVersion)
}

private fun Project.resolveProtoktGrpcDep(protoktVersion: Any?): Dependency? {
    if (!the<ProtoktExtension>().generate.grpcDescriptors) {
        return null
    }

    return resolveDependency("protokt-runtime-grpc", protoktVersion)
}

private fun Project.resolveDependency(rootArtifactId: String, protoktVersion: Any?): Dependency? {
    val artifactId =
        if (the<ProtoktExtension>().generate.descriptors) {
            rootArtifactId
        } else {
            "$rootArtifactId-lite"
        }

    return if (protoktVersion == null) {
        dependencies.project(":$artifactId")
    } else {
        dependencies.create("com.toasttab.protokt:$artifactId:$protoktVersion")
    }
}
