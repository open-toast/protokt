/*
 * Copyright (c) 2022 Toast, Inc.
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

import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.protobuf
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.getByType

fun Project.findLibrary(name: String): String =
    rootProject
        .extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")
        .findLibrary(name)
        .get() // optional
        .get() // provider
        .run { "$module:$versionConstraint" }

fun Project.findVersion(name: String): String =
    rootProject
        .extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")
        .findVersion(name)
        .get() // optional
        .requiredVersion

fun Project.protobufExcludingProtobufJava(dependency: Provider<MinimalExternalModuleDependency>) {
    dependencies {
        protobuf(dependency.get().toString()) {
            exclude(group = "com.google.protobuf", module = "protobuf-java")
        }
    }
}

fun Project.defaultProtoc() {
    configure<ProtobufExtension> {
        protoc {
            artifact = findLibrary("protoc")
        }
    }
}
