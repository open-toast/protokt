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
    "protokt-core-lite",
    "protokt-runtime",
    "protokt-runtime-grpc",
    "protokt-gradle-plugin",
    "protokt-util",

    "examples",
    "examples:grpc-java",
    "examples:grpc-java-lite",
    "examples:grpc-kotlin",
    "examples:grpc-kotlin-lite",
    "examples:protos",

    "extensions",
    "extensions:protokt-extensions-api",
    "extensions:protokt-extensions-simple",
    "extensions:protokt-extensions",
    "extensions:protokt-extensions-lite",

    "testing:android",
    "testing:android-test-configurations",
    "testing:conformance-driver",
    "testing:conformance-tests",
    "testing:options",
    "testing:options-api",
    "testing:options-test-configurations",
    "testing:plugin-options",
    "testing:plugin-options:ignore-java-package",
    "testing:plugin-options:lite",
    "testing:protobuf-java",
    "testing:runtime-tests",
    "testing:testing-util",

    "benchmarks",
    "benchmarks:benchmarks-util",
    "benchmarks:protobuf-java-benchmarks",
    "benchmarks:protokt-benchmarks",
    "benchmarks:schema",
    "benchmarks:wire-benchmarks",

    "third-party",
    "third-party:proto-google-common-protos",
    "third-party:proto-google-common-protos-extensions",
    "third-party:proto-google-common-protos-extensions-lite",
    "third-party:proto-google-common-protos-grpc",
    "third-party:proto-google-common-protos-lite"
)
