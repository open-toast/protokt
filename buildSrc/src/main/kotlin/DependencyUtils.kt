/*
 * Copyright (c) 2022 Toast Inc.
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

import com.toasttab.protokt.gradle.DEFAULT_PROTOBUF_VERSION
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

// needed until IntelliJ supports version catalogs for buildSrc
fun Project.findLibrary(name: String): String =
    versionCatalog
        .findLibrary(name)
        .get() // optional
        .get() // provider
        .run { "$module:$versionConstraint" }

private val Project.versionCatalog
    get() = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")

fun protobufDep(lib: Provider<MinimalExternalModuleDependency>) =
    "${lib.get()}:$DEFAULT_PROTOBUF_VERSION"
