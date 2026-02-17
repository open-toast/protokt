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

buildscript {
    repositories {
        maven(url = "https://repo1.maven.org/maven2")
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
    "protokt-gradle-plugin",
    "protokt-protovalidate",
    "protokt-reflect",
    "protokt-runtime",
    "protokt-runtime-grpc",
    "protokt-runtime-grpc-lite",

    "grpc-kotlin-shim",

    "examples",
    "examples:grpc-java",
    "examples:grpc-java-lite",
    "examples:grpc-kotlin",
    "examples:grpc-kotlin-lite",
    "examples:grpc-node",
    "examples:protos",

    "extensions",
    "extensions:protokt-extensions",
    "extensions:protokt-extensions-lite",

    "testing:android",
    "testing:android-test-configurations",
    "testing:conformance",
    "testing:conformance:driver",
    "testing:conformance:js-ir",
    "testing:conformance:jvm",
    "testing:conformance:runner",
    "testing:interop",
    "testing:persistent-collections-testing",
    "testing:multiplatform-testing",
    "testing:options",
    "testing:options-api",
    "testing:options-test-configurations",
    "testing:plugin-options",
    "testing:plugin-options:lite",
    "testing:protokt-generation",
    "testing:protokt-generation-2",
    "testing:protobuf-java",
    "testing:protovalidate-conformance",
    "testing:protobufjs",
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
    "third-party:proto-google-common-protos-grpc-kotlin",
    "third-party:proto-google-common-protos-lite"
)
