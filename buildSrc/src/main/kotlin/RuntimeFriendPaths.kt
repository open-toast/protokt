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
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.tasks.BaseKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.io.File

// Configures friend-module paths so that extension modules (protokt-runtime-kotlinx-io,
// protokt-runtime-protobufjs, etc.) can access internal members of :protokt-runtime.
//
// Two quirks stem from protokt-runtime's custom nonJvmMain intermediate source set:
//
//   1. Metadata-target KotlinNativeCompile tasks (e.g. compileNativeMainKotlinMetadata)
//      must friend the matching compilation plus everything in its dependsOn closure,
//      not just the matching one, because internal members like codecOverride live in
//      nonJvmMain while the consuming code is in nativeMain. Friending compilations
//      outside the closure trips Kotlin 2.4's warning about friend modules that are
//      not on the library path.
//
//   2. -friend-modules only honours the last occurrence when passed multiple times,
//      so all paths must be joined into a single argument.
fun Project.runtimeFriendPaths() {
    friendPaths(":protokt-runtime")

    configure<KotlinMultiplatformExtension> {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=protokt.v1.OnlyForUseByGeneratedProtoCode")
        }
    }
}

fun Project.friendPaths(vararg friendProjectPaths: String) {
    configure<KotlinMultiplatformExtension> {
        targets.all {
            compilations.all {
                val compilation = this
                val friendCompilations = resolveFriendCompilations(compilation, friendProjectPaths)
                if (friendCompilations.isEmpty()) return@all

                compileTaskProvider.configure {
                    when (this) {
                        is BaseKotlinCompile -> {
                            for ((friendProject, _, friendCompilation) in friendCompilations) {
                                friendPaths.from(friendCompilation.output.classesDirs, friendCompilation.output.allOutputs)
                                if (compilation.platformType == KotlinPlatformType.jvm) {
                                    val jarTaskName = "${compilation.target.name}Jar"
                                    if (jarTaskName in friendProject.tasks.names) {
                                        friendPaths.from(friendProject.tasks.named(jarTaskName))
                                    }
                                }
                            }
                        }

                        is KotlinNativeCompile -> {
                            val allDirs = friendCompilations.flatMap { (_, friendKmp, friendCompilation) ->
                                if (compilation.target.name == "metadata") {
                                    val visibleSourceSets = dependsOnClosure(friendCompilation.defaultSourceSet).map { it.name }
                                    friendKmp.targets.getByName("metadata")
                                        .compilations
                                        .filter { it.name != KotlinCompilation.MAIN_COMPILATION_NAME }
                                        .filter { it.defaultSourceSet.name in visibleSourceSets }
                                        .flatMap { it.output.classesDirs }
                                } else {
                                    friendCompilation.output.classesDirs.toList()
                                }
                            }
                            val joined = allDirs.joinToString(File.pathSeparator) { it.absolutePath }
                            compilerOptions.freeCompilerArgs.add("-friend-modules=$joined")
                        }
                    }
                }
            }
        }
    }
}

private fun dependsOnClosure(sourceSet: org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet): Set<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet> =
    setOf(sourceSet) + sourceSet.dependsOn.flatMap { dependsOnClosure(it) }

private fun Project.resolveFriendCompilations(
    compilation: KotlinCompilation<*>,
    friendProjectPaths: Array<out String>
) =
    friendProjectPaths.mapNotNull { path ->
        val friendProject = project(path)
        val friendKmp = friendProject
            .extensions
            .getByType(KotlinMultiplatformExtension::class.java)
        val friendTarget = friendKmp
            .targets
            .findByName(compilation.target.name) ?: return@mapNotNull null
        val name =
            if (friendTarget.compilations.findByName(compilation.compilationName) != null) {
                compilation.compilationName
            } else {
                KotlinCompilation.MAIN_COMPILATION_NAME
            }
        Triple(friendProject, friendKmp, friendTarget.compilations.getByName(name))
    }
