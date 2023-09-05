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

import com.diffplug.gradle.spotless.SpotlessExtension

allprojects {
    apply(plugin = "com.diffplug.spotless")
    repositories { mavenCentral() }

    configure<SpotlessExtension> {
        val editorConfigOverride =
            mapOf(
                "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                "ktlint_standard_trailing-comma-on-declaration-site" to "disabled"
            )

        kotlin {
            ktlint().editorConfigOverride(editorConfigOverride)
            target("**/*.kt")
            targetExclude(
                "**/build/ci/protobuf/**",
                "buildSrc/build/generated-sources/**",
                "**/generated/**",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/compiler/plugin.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/any.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/api.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/descriptor.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/duration.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/empty.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/field_mask.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/source_context.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/struct.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/timestamp.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/type.kt",
                "protokt-core-lite/src/jvmMain/kotlin/com/toasttab/protokt/wrappers.kt",
                "extensions/protokt-jvm-extensions-lite/src/main/kotlin/com/toasttab/protokt/ext/inet_socket_address.kt"
            )
        }

        kotlinGradle {
            ktlint().editorConfigOverride(editorConfigOverride)
            target("**/*.kts")
            targetExclude("buildSrc/build/**")
            licenseHeaderFile(
                rootProject.file("gradle/license-header-c-style"),
                "(package |@file|import |fun )|buildscript |plugins |subprojects |spotless "
            )
        }

        format("kotlinLicense") {
            target("**/*.kt")
            licenseHeaderFile(
                rootProject.file("gradle/license-header-c-style"),
                "(package |@file|import |fun )"
            )
            targetExclude(
                "**/build/ci/protobuf/**",
                "**/generated/source/proto/*/protokt/**",
                "**/build/generated-sources/kotlin-dsl-*/**",
                "**/protokt/v1/animals/**",
                "**/protokt/v1/helloworld/**",
                "**/protokt/v1/io/grpc/examples/**"
            )
        }

        format("protobufLicense") {
            target("**/*.proto")
            targetExclude(
                listOf(
                    "benchmarks/schema/src/main/resources/schema/benchmarks.proto",
                    "examples/protos/src/main/proto/animals/dog.proto",
                    "examples/protos/src/main/proto/animals/pig.proto",
                    "examples/protos/src/main/proto/animals/sheep.proto",
                    "examples/protos/src/main/proto/helloworld/hello_world.proto",
                    "examples/protos/src/main/proto/io/grpc/examples/route_guide.proto",
                    "testing/conformance/driver/src/main/proto/conformance/conformance.proto",
                    "testing/conformance/driver/src/main/proto/proto3/test_messages_proto3.proto",
                    "testing/interop/src/main/proto/tutorial/addressbook.proto"
                ).map(rootProject::file) +
                    "node_modules/**" +
                    "**/build/extracted-include-protos/**" +
                    "**/build/resources/**" +
                    "**/build/ci/protobuf/**"
            )
            licenseHeaderFile(
                rootProject.file("gradle/license-header-c-style"),
                "(syntax )"
            )
        }
    }
}
