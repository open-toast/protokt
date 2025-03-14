/*
 * Copyright (c) 2023 Toast, Inc.
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

plugins {
    id("com.diffplug.spotless")
}

repositories { mavenCentral() }

spotless {
    val editorConfigOverride =
        mapOf(
            "ktlint_standard_trailing-comma-on-call-site" to "disabled",
            "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
            "ktlint_function_signature_body_expression_wrapping" to "always",
            "ij_kotlin_packages_to_use_import_on_demand" to null,
        )

    kotlin {
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(editorConfigOverride)
        target("src/**/*.kt", "buildSrc/src/**/*.kt")
        targetExclude(
            "third-party/proto-google-common-protos-grpc/src/main/kotlin/com/google/**/*.kt",
            "third-party/proto-google-common-protos-lite/src/jvmMain/kotlin/com/google/**/*.kt",
            "third-party/proto-google-common-protos/src/jvmMain/kotlin/com/google/**/*.kt",
        )
    }

    kotlinGradle {
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(editorConfigOverride)
        target("buildSrc/src/**/*.kts")
        licenseHeaderFile(
            rootProject.file("gradle/license-header-c-style"),
            "(package |@file|import |fun )|buildscript |plugins |subprojects |spotless |group ="
        )
    }

    format("kotlinLicense") {
        target("src/**/*.kt")
        licenseHeaderFile(
            rootProject.file("gradle/license-header-c-style"),
            "(package |@file|import |fun )"
        )
        targetExclude(
            "**/protokt/v1/animals/**",
            "**/protokt/v1/helloworld/**",
            "**/protokt/v1/io/grpc/examples/**",
            "third-party/proto-google-common-protos-grpc/src/main/kotlin/com/google/**/*.kt",
            "third-party/proto-google-common-protos-lite/src/jvmMain/kotlin/com/google/**/*.kt",
            "third-party/proto-google-common-protos/src/jvmMain/kotlin/com/google/**/*.kt",
        )
    }

    format("protobufLicense") {
        target("src/**/*.proto")
        targetExclude(
            listOf(
                "examples/protos/src/main/proto/animals/dog.proto",
                "examples/protos/src/main/proto/animals/pig.proto",
                "examples/protos/src/main/proto/animals/sheep.proto",
                "examples/protos/src/main/proto/helloworld/hello_world.proto",
                "examples/protos/src/main/proto/io/grpc/examples/route_guide.proto",
                "testing/conformance/driver/src/main/proto/conformance/conformance.proto",
                "testing/conformance/driver/src/main/proto/protobuf_test_messages/editions/proto3/test_messages_proto3_editions.proto",
                "testing/conformance/driver/src/main/proto/protobuf_test_messages/editions/test_messages_edition2023.proto",
                "testing/conformance/driver/src/main/proto/protobuf_test_messages/proto3/test_messages_proto3.proto",
                "testing/interop/src/main/proto/google/protobuf/unittest_import.proto",
                "testing/interop/src/main/proto/google/protobuf/unittest_import_public.proto",
                "testing/interop/src/main/proto/google/protobuf/unittest_proto3.proto",
                "testing/interop/src/main/proto/tutorial/addressbook.proto",
            ).map(rootProject::file)
        )
        licenseHeaderFile(
            rootProject.file("gradle/license-header-c-style"),
            "(syntax |edition )"
        )
    }
}
