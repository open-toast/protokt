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

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.configure

fun Project.protokt(cfg: ProtoktExtension.() -> Unit) = project.configure(cfg)

fun DependencyHandler.protoktExtensions(dependencyNotation: Any): Dependency? =
    add(EXTENSIONS, dependencyNotation)

fun <T : ModuleDependency> DependencyHandler.protoktExtensions(
    dependencyNotation: T,
    dependencyConfiguration: T.() -> Unit
): Dependency =
    add(EXTENSIONS, dependencyNotation, dependencyConfiguration)

fun DependencyHandler.protoktExtensions(
    dependencyNotation: String,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit
): ExternalModuleDependency =
    add(
        EXTENSIONS,
        create(dependencyNotation) as ExternalModuleDependency,
        dependencyConfiguration
    )

fun DependencyHandler.testProtoktExtensions(dependencyNotation: Any): Dependency? =
    add(TEST_EXTENSIONS, dependencyNotation)

fun <T : ModuleDependency> DependencyHandler.testProtoktExtensions(
    dependencyNotation: T,
    dependencyConfiguration: T.() -> Unit
): Dependency =
    add(TEST_EXTENSIONS, dependencyNotation, dependencyConfiguration)

fun DependencyHandler.testProtoktExtensions(
    dependencyNotation: String,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit
): ExternalModuleDependency =
    add(
        TEST_EXTENSIONS,
        create(dependencyNotation) as ExternalModuleDependency,
        dependencyConfiguration
    )
