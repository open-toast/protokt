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
import org.gradle.api.file.FileCollection
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
    binaryPath: String
) {
    project.apply<ProtobufPlugin>()

    project.configure<ProtobufExtension> {
        configureSources(project)

        protoc {
            artifact = "com.google.protobuf:protoc:${ext.protocVersion}"
        }

        plugins {
            id("protokt" + target.pluginSuffix) {
                path = normalizePath(binaryPath)
            }
        }

        generateProtoTasks {
            for (task in all()) {
                if (disableJava) {
                    task.builtins {
                        findByName("java")?.run(::remove)
                    }
                }

                task.plugins {
                    id("protokt" + target.pluginSuffix) {
                        project.afterEvaluate {
                            option("$KOTLIN_EXTRA_CLASSPATH=${extraClasspath(project, task)}")
                            option("$GENERATE_TYPES=${ext.generate.types}")
                            option("$GENERATE_DESCRIPTORS=${ext.generate.descriptors}")
                            option("$GENERATE_GRPC_DESCRIPTORS=${ext.generate.grpcDescriptors}")
                            option("$GENERATE_GRPC_KOTLIN_STUBS=${ext.generate.grpcKotlinStubs}")
                            option("$FORMAT_OUTPUT=${ext.formatOutput}")
                            option("$KOTLIN_TARGET=$target")
                        }
                    }
                }
            }
        }
    }
}

private fun extraClasspath(project: Project, task: GenerateProtoTask): String {
    var extensions: FileCollection = project.configurations.getByName(EXTENSIONS)

    if (task.isTest) {
        extensions += project.configurations.getByName(TEST_EXTENSIONS)
    }

    // Must explicitly register input files here; if any extensions dependencies are project dependencies then Gradle
    // won't pick them up as dependencies unless we do this. There may be a better way to do this but for now just
    // manually do what protobuf-gradle-plugin used to do.
    // https://github.com/google/protobuf-gradle-plugin/commit/0521fe707ccedee7a0b4ce0fb88409eefb04e59d
    project.tasks.withType<ProtobufExtract> {
        if (name.startsWith("extractInclude")) {
            inputFiles.from(extensions)
        }
    }

    return extensions.joinToString(";") { URLEncoder.encode(it.path, "UTF-8") }
}

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
