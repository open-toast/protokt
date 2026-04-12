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

import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.proto
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

internal const val BASE_GROUP_NAME = "com.toasttab.protokt.v1"

const val CODEGEN_NAME = "protoc-gen-protokt"

const val EXTENSIONS = "protoktExtensions"

const val TEST_EXTENSIONS = "testProtoktExtensions"

internal val DEBUG_LOG_LEVEL = LogLevel.INFO

internal fun configureProtokt(
    project: Project,
    protoktVersion: Any?,
    disableJava: Boolean,
    binary: Provider<String>
) {
    injectKotlinPluginsIntoProtobufGradle()

    val config = project.createExtensionConfigurations()

    // must wait for extension to resolve
    project.afterEvaluate {
        val ext = the<ProtoktExtension>()

        (
            listOfNotNull(
                project.resolveProtoktCoreDep(protoktVersion),
                project.resolveProtoktGrpcDep(protoktVersion),
            ) + project.resolveCommonCodecDeps(protoktVersion) +
                project.resolveCollectionsDeps(protoktVersion)
            ).forEach(config.extensions.dependencies::add)

        // For OPTIMAL in KMP, add JVM-specific deps to target configs
        if (ext.codec.selection == ProtoktExtension.CodecSelection.OPTIMAL &&
            plugins.hasPlugin(KotlinPlugins.MULTIPLATFORM)
        ) {
            addPerTargetOptimalDeps(protoktVersion, ext)
        }
    }

    project.configureProtobuf(disableJava, config, binary)
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
    binary: Provider<String>
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

private fun Project.configureForJvmLike(config: Config, disableJava: Boolean, target: KotlinTarget, binary: Provider<String>) {
    logger.log(DEBUG_LOG_LEVEL, "Configuring protokt for Kotlin ${target.name}")
    configureProtobufPlugin(project, config.extension, disableJava, target, binary)
    configurations.getByName("api").extendsFrom(config.extensions)
    configurations.getByName("testApi").extendsFrom(config.testExtensions)
}

private fun Project.configureForMpp(
    disableJava: Boolean,
    config: Config,
    binary: Provider<String>
) {
    pluginManager.apply("java-base")
    the<SourceSetContainer>().maybeCreate("main")
    the<SourceSetContainer>().maybeCreate("test")

    configureTarget("common", disableJava, config, binary)

    extensions.getByType(KotlinMultiplatformExtension::class.java).targets.all {
        if (targetName != "metadata") {
            logger.log(DEBUG_LOG_LEVEL, "Handling Kotlin multiplatform target {}", this)
            configureTarget(targetName, disableJava, config, binary)
        }
    }

    afterEvaluate {
        configureJarTasksForMpp()
    }
}

private fun Project.configureTarget(
    targetName: String,
    disableJava: Boolean,
    config: Config,
    binary: Provider<String>
) {
    val target = KotlinTarget.fromMultiplatformTargetString(targetName)
    configureProtobufPlugin(project, config.extension, disableJava, target, binary)

    val sourceSets = extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets
    val mainSourceSet = sourceSets.getByName("${targetName}Main")
    val testSourceSet = sourceSets.getByName("${targetName}Test")

    configurations.getByName(mainSourceSet.apiConfigurationName).extendsFrom(config.extensions)
    configurations.getByName(testSourceSet.apiConfigurationName).extendsFrom(config.testExtensions)

    // KMP disables Java compilation via onlyIf and doesn't wire Java source sets to compile tasks. Re-enable and connect them here.
    if (!disableJava && target.treatTargetAsJvm) {
        for (taskSuffix in listOf("Main", "Test")) {
            val javaSources = the<SourceSetContainer>().getByName(taskSuffix.lowercase()).allJava
            tasks.named<JavaCompile>("compile${targetName.capitalized()}${taskSuffix}Java") {
                setOnlyIf("Java compilation enabled") { true }
                source(javaSources)
            }
        }
    }

    afterEvaluate {
        linkGenerateProtoTasksAndIncludeGeneratedSource(target, mainSourceSet, false)
        linkGenerateProtoTasksAndIncludeGeneratedSource(target, testSourceSet, true)
    }
}

private fun Project.configureJarTasksForMpp() {
    for (extractTask in extractProtoTasks()) {
        tasks.withType<Jar> {
            from(extractTask.destDir)
            excludeDuplicates()
        }
    }

    (tasks.findByName("jsProcessResources") as? Copy)?.excludeDuplicates()

    tasks.withType<Copy> {
        if (name.endsWith("ProcessResources") && name != "jvmProcessResources" && name != "jsProcessResources") {
            excludeDuplicates()
        }
    }
}

// TODO: figure out how to get rid of this?
private fun AbstractCopyTask.excludeDuplicates() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

private fun Project.linkGenerateProtoTasksAndIncludeGeneratedSource(target: KotlinTarget, sourceSet: KotlinSourceSet, test: Boolean) {
    val protoSourceSetRoot = if (test) "test" else "main"

    val extension = project.extensions.getByType<ProtobufExtension>()

    // JVM targets create Gradle source sets (e.g., jvmMain), so the protobuf plugin
    // creates per-target tasks (e.g., generateJvmMainProto). Non-JVM targets (common, JS)
    // don't create Gradle source sets, so only the base tasks exist.
    val taskName =
        if (target.treatTargetAsJvm) {
            "generate${sourceSet.name.capitalized()}Proto"
        } else if (test) {
            "generateTestProto"
        } else {
            "generateProto"
        }

    val allTasks = extension.generateProtoTasks.all()
    val generateProtoTask = allTasks.singleOrNull { it.name == taskName }

    generateProtoTask?.let { genProtoTask ->
        // Only include this target's output directory, not all targets' output directories.
        val targetOutputDir = layout.buildDirectory.dir("generated/sources/proto/$protoSourceSetRoot/${target.protocPluginName}")
        sourceSet.kotlin.srcDir(targetOutputDir)

        // JVM targets also need the Java protobuf output directory so the Kotlin compiler
        // can resolve references to generated Java classes (e.g., ProtoktProtos).
        if (target.treatTargetAsJvm) {
            sourceSet.kotlin.srcDir(layout.buildDirectory.dir("generated/sources/proto/$protoSourceSetRoot/java"))
        }

        the<SourceSetContainer>()
            .getByName(protoSourceSetRoot)
            .proto { sourceSet.resources.source(this) }

        tasks.withType<AbstractKotlinCompile<*>> {
            if ((test && "Test" in name) || (!test && "Test" !in name)) {
                logger.log(DEBUG_LOG_LEVEL, "Making task {} a dependency of {}", genProtoTask.name, name)
                dependsOn(genProtoTask)
            }
        }
        tasks.withType<KotlinNativeCompile> {
            if ((test && "Test" in name) || (!test && "Test" !in name)) {
                logger.log(DEBUG_LOG_LEVEL, "Making task {} a dependency of {}", genProtoTask.name, name)
                dependsOn(genProtoTask)
            }
        }
    }
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
        dependencies.create("$BASE_GROUP_NAME:$artifactId:$protoktVersion")
    }
}

private fun Project.resolveCommonCodecDeps(protoktVersion: Any?): List<Dependency> {
    val ext = the<ProtoktExtension>()
    return when (ext.codec.selection) {
        ProtoktExtension.CodecSelection.OPTIMAL ->
            if (plugins.hasPlugin(KotlinPlugins.MULTIPLATFORM)) {
                // KMP: common deps are kotlinx-io only; JVM-specific deps added per-target
                resolveOptimalKmpDeps(protoktVersion)
            } else if (plugins.hasPlugin(KotlinPlugins.ANDROID)) {
                resolveOptimalJvmLiteDeps(protoktVersion, ext)
            } else {
                resolveOptimalJvmDeps(protoktVersion, ext)
            }

        ProtoktExtension.CodecSelection.OPTIMAL_KMP ->
            resolveOptimalKmpDeps(protoktVersion)

        ProtoktExtension.CodecSelection.OPTIMAL_JVM ->
            resolveOptimalJvmDeps(protoktVersion, ext)

        ProtoktExtension.CodecSelection.OPTIMAL_JVM_LITE ->
            resolveOptimalJvmLiteDeps(protoktVersion, ext)

        ProtoktExtension.CodecSelection.PROTOBUF_JAVA -> listOfNotNull(
            resolveOptionalDep("protokt-runtime-protobuf-java", protoktVersion),
            dependencies.create("com.google.protobuf:protobuf-java:${ext.protocVersion}")
        )

        ProtoktExtension.CodecSelection.PROTOBUF_JAVALITE -> listOfNotNull(
            resolveOptionalDep("protokt-runtime-protobuf-java", protoktVersion),
            dependencies.create("com.google.protobuf:protobuf-javalite:${ext.protocVersion}")
        )

        ProtoktExtension.CodecSelection.MINIMAL -> emptyList()
    }
}

private fun Project.resolveOptimalKmpDeps(protoktVersion: Any?) =
    listOfNotNull(resolveOptionalDep("protokt-runtime-kotlinx-io", protoktVersion))

private fun Project.resolveOptimalJvmDeps(protoktVersion: Any?, ext: ProtoktExtension) =
    listOfNotNull(
        resolveOptionalDep("protokt-runtime-protobuf-java", protoktVersion),
        dependencies.create("com.google.protobuf:protobuf-java:${ext.protocVersion}")
    )

private fun Project.resolveOptimalJvmLiteDeps(protoktVersion: Any?, ext: ProtoktExtension) =
    listOfNotNull(
        resolveOptionalDep("protokt-runtime-protobuf-java", protoktVersion),
        dependencies.create("com.google.protobuf:protobuf-javalite:${ext.protocVersion}")
    )

private fun Project.addPerTargetOptimalDeps(protoktVersion: Any?, ext: ProtoktExtension) {
    val kmpExt = extensions.getByType(KotlinMultiplatformExtension::class.java)
    kmpExt.targets.all {
        val target = KotlinTarget.fromMultiplatformTargetString(targetName)
        if (target.treatTargetAsJvm) {
            val mainImplConfig = kmpExt.sourceSets
                .getByName("${targetName}Main")
                .implementationConfigurationName
            val deps = if (target is KotlinTarget.MultiplatformAndroid) {
                resolveOptimalJvmLiteDeps(protoktVersion, ext)
            } else {
                resolveOptimalJvmDeps(protoktVersion, ext)
            }
            deps.forEach { configurations.getByName(mainImplConfig).dependencies.add(it) }
        }
    }
}

private fun Project.resolveCollectionsDeps(protoktVersion: Any?): List<Dependency> =
    when (the<ProtoktExtension>().collections.selection) {
        ProtoktExtension.CollectionsSelection.PERSISTENT ->
            listOfNotNull(resolveOptionalDep("protokt-runtime-persistent-collections", protoktVersion))

        ProtoktExtension.CollectionsSelection.MINIMAL -> emptyList()
    }

private fun Project.resolveOptionalDep(artifactId: String, protoktVersion: Any?): Dependency? {
    if (name == artifactId) return null
    return if (protoktVersion == null) {
        dependencies.project(":$artifactId")
    } else {
        dependencies.create("$BASE_GROUP_NAME:$artifactId:$protoktVersion")
    }
}
