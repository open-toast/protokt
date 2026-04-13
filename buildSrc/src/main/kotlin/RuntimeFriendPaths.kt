/*
 * Copyright (c) 2026 Toast, Inc.
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

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.BaseKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.io.File

fun Project.runtimeFriendPaths() {
    configure<KotlinMultiplatformExtension> {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=protokt.v1.OnlyForUseByGeneratedProtoCode")
        }

        targets.all {
            compilations.all {
                val compilation = this
                compileTaskProvider.configure {
                    val runtimeProject = project(":protokt-runtime")
                    val runtimeKmp = runtimeProject
                        .extensions
                        .getByType(KotlinMultiplatformExtension::class.java)
                    val runtimeCompilation = runtimeKmp
                        .targets
                        .getByName(compilation.target.name)
                        .compilations
                        .getByName(compilation.compilationName)
                    when (this) {
                        is BaseKotlinCompile -> {
                            friendPaths.from(
                                runtimeCompilation.output.classesDirs,
                                runtimeCompilation.output.allOutputs
                            )
                            val jarTaskName = "${compilation.target.name}Jar"
                            if (jarTaskName in runtimeProject.tasks.names) {
                                friendPaths.from(runtimeProject.tasks.named(jarTaskName))
                            }
                        }

                        is KotlinNativeCompile -> {
                            val friendDirs = if (compilation.target.name == "metadata") {
                                // Metadata target KotlinNativeCompile tasks need
                                // friends from all metadata compilations, not just
                                // the matching one, because internal members may be
                                // defined in a different intermediate source set
                                // (e.g. codecOverride in nonJvmMain accessed from
                                // nativeMain).
                                runtimeKmp.targets.getByName("metadata")
                                    .compilations
                                    .flatMap { it.output.classesDirs }
                            } else {
                                runtimeCompilation.output.classesDirs.toList()
                            }
                            // -friend-modules only uses the last value when
                            // passed multiple times; join all paths into one.
                            val joined = friendDirs.joinToString(File.pathSeparator) { it.absolutePath }
                            compilerOptions.freeCompilerArgs.add("-friend-modules=$joined")
                        }
                    }
                }
            }
        }
    }
}
