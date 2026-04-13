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

// Configures friend-module paths so that extension modules (protokt-runtime-kotlinx-io,
// protokt-runtime-protobufjs, etc.) can access internal members of :protokt-runtime.
//
// Two quirks stem from protokt-runtime's custom nonJvmMain intermediate source set:
//
//   1. Metadata-target KotlinNativeCompile tasks (e.g. compileNativeMainKotlinMetadata)
//      must friend ALL metadata compilations, not just the matching one, because
//      internal members like codecOverride live in nonJvmMain while the consuming
//      code is in nativeMain.
//
//   2. -friend-modules only honours the last occurrence when passed multiple times,
//      so all paths must be joined into a single argument.
fun Project.runtimeFriendPaths() {
    configure<KotlinMultiplatformExtension> {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=protokt.v1.OnlyForUseByGeneratedProtoCode")
        }

        targets.all {
            compilations.matching { it.compilationName in setOf("main", "test") }.all {
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
                            val friendDirs =
                                if (compilation.target.name == "metadata") {
                                    runtimeKmp.targets.getByName("metadata")
                                        .compilations
                                        .flatMap { it.output.classesDirs }
                                } else {
                                    runtimeCompilation.output.classesDirs.toList()
                                }
                            val joined = friendDirs.joinToString(File.pathSeparator) { it.absolutePath }
                            compilerOptions.freeCompilerArgs.add("-friend-modules=$joined")
                        }
                    }
                }
            }
        }
    }
}
