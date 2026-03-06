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
                    val runtimeCompilation = runtimeProject
                        .extensions
                        .getByType(KotlinMultiplatformExtension::class.java)
                        .targets
                        .getByName(compilation.target.name)
                        .compilations
                        .getByName(compilation.compilationName)
                    (this as BaseKotlinCompile).friendPaths.from(
                        runtimeCompilation.output.classesDirs,
                        runtimeCompilation.output.allOutputs
                    )
                    val jarTaskName = "${compilation.target.name}Jar"
                    if (jarTaskName in runtimeProject.tasks.names) {
                        friendPaths.from(runtimeProject.tasks.named(jarTaskName))
                    }
                }
            }
        }
    }
}
