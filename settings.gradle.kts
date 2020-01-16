/*
 * Copyright (c) 2019 Toast Inc.
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

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("gradle.plugin.net.vivin:gradle-semantic-build-versioning:4.0.0")
    }
}

apply(plugin = "net.vivin.gradle-semantic-build-versioning")

rootProject.name = "protokt"

include(
    "protokt-codegen",
    "protokt-core",
    "protokt-runtime",
    "protokt-gradle-plugin",

    "extensions",
    "extensions:protokt-extensions-api",
    "extensions:protokt-extensions-simple",
    "extensions:protokt-extensions-proto-based",
    "extensions:protokt-extensions-wrappers",
    "extensions:publish",

    "testing:conformance-driver",
    "testing:conformance-tests",
    "testing:options-api",
    "testing:options",
    "testing:protobuf-java",
    "testing:runtime-tests",

    "benchmarks",
    "benchmarks:schema",
    "benchmarks:wire",
    "benchmarks:protokt",
    "benchmarks:protobuf-java",
    "benchmarks:util",

    "third-party",
    "third-party:proto-google-common-protos"
)
