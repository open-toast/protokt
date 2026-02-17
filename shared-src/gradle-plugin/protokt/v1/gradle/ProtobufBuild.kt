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
import com.google.protobuf.gradle.ProtobufExtract
import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.id
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import java.net.URLEncoder

internal fun configureProtobufPlugin(
    project: Project,
    ext: ProtoktExtension,
    disableJava: Boolean,
    target: KotlinTarget,
    binaryPath: Provider<String>
) {
    project.apply<ProtobufPlugin>()

    project.configure<ProtobufExtension> {
        configureSources(project)

        protoc {
            artifact = "com.google.protobuf:protoc:${ext.protocVersion}"
        }

        plugins {
            id(target.protocPluginName)
        }

        generateProtoTasks {
            val mainExtractProtoAdditions = mutableListOf<TaskInputFiles>()
            val testExtractProtoAdditions = mutableListOf<TaskInputFiles>()

            for (task in all()) {
                if (disableJava) {
                    task.builtins {
                        findByName("java")?.run(::remove)
                    }
                }

                val extensions = resolveExtensions(project, task)

                if (task.isTestTask() && extensions.test != null) {
                    testExtractProtoAdditions.add(TaskInputFiles(extensions.taskName, extensions.test))
                } else {
                    mainExtractProtoAdditions.add(TaskInputFiles(extensions.taskName, extensions.main))
                }

                val extensionFiles = project.objects.fileCollection().from(extensions.asList())
                task.inputs.files(extensionFiles).withPropertyName("protoktExtensionClasspath")

                task.plugins {
                    id(target.protocPluginName) {
                        option("$GENERATE_TYPES=${ext.generate.types}")
                        option("$GENERATE_DESCRIPTORS=${ext.generate.descriptors}")
                        option("$GENERATE_GRPC_DESCRIPTORS=${ext.generate.grpcDescriptors}")
                        option("$GENERATE_GRPC_KOTLIN_STUBS=${ext.generate.grpcKotlinStubs}")
                        option("$FORMAT_OUTPUT=${ext.formatOutput}")
                        option("$KOTLIN_TARGET=$target")

                        val pluginOptions = this
                        task.doFirst {
                            val classpath = extensionFiles.files.joinToString(";") { URLEncoder.encode(it.path, "UTF-8") }
                            pluginOptions.option("$KOTLIN_EXTRA_CLASSPATH=$classpath")
                        }
                    }
                }
            }

            project.handleExtraInputFiles(mainExtractProtoAdditions, testExtractProtoAdditions)
        }
    }

    project.afterEvaluate {
        configure<ProtobufExtension> {
            plugins {
                val pluginLocator = getByName(target.protocPluginName)
                project.tasks.withType<GenerateProtoTask>().configureEach {
                    doFirst {
                        pluginLocator.path = normalizePath(binaryPath.get())
                    }
                }
            }
        }
    }
}

private fun Project.handleExtraInputFiles(main: List<TaskInputFiles>, test: List<TaskInputFiles>) {
    afterEvaluate {
        handleExtractProtoAdditions(main, false)
        handleExtractProtoAdditions(test, true)
    }
}

private fun Project.handleExtractProtoAdditions(additions: List<TaskInputFiles>, test: Boolean) {
    additions.forEach {
        // there is more than one task in multiplatform projects: e.g. extractIncludeProto and extractIncludeJvmMainProto
        extractIncludeProtoTasks().forEach { task ->
            if (task.isTestTask() == test) {
                logger.log(DEBUG_LOG_LEVEL, "Adding input files to task ${task.name} from ${it.taskName}")
                task.inputFiles.from(it.inputFiles)
            }
        }
    }
}

private class TaskInputFiles(
    val taskName: String,
    val inputFiles: Any
)

private class ExtensionsConfigurations(
    val taskName: String,
    val main: Provider<Configuration>,
    val test: Provider<Configuration>?
) {
    fun asList() =
        listOfNotNull(main, test)
}

private fun resolveExtensions(project: Project, task: GenerateProtoTask) =
    ExtensionsConfigurations(
        task.name,
        project.configurations.named(EXTENSIONS),
        if (task.isTestTask()) {
            project.configurations.named(TEST_EXTENSIONS)
        } else {
            null
        }
    )

private fun configureSources(project: Project) {
    project.afterEvaluate {
        if (project.tasks.findByName("sourcesJar") != null) {
            tasks.named<Jar>("sourcesJar").configure {
                from("generated/source/proto/main")
            }
        }
    }
}

private fun normalizePath(binaryPath: String) =
    if (Os.current.kind == Os.Kind.WINDOWS) {
        // on windows, protoc expects a full, /-separated path to the binary
        binaryPath.replace('\\', '/') + ".bat"
    } else {
        binaryPath
    }

private fun GenerateProtoTask.isTestTask() =
    name.isTestTask()

private fun ProtobufExtract.isTestTask() =
    name.isTestTask()

private fun String.isTestTask() =
    endsWith("TestProto")

private fun Project.extractIncludeProtoTasks() =
    project.tasks.withType<ProtobufExtract>().filter { it.name.startsWith("extractInclude") }

internal fun Project.extractProtoTasks() =
    project.tasks.withType<ProtobufExtract>().filter {
        !it.name.startsWith("extractInclude") && !it.isTestTask()
    }
