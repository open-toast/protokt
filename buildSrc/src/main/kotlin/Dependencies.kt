import com.toasttab.protokt.shared.DEFAULT_PROTOBUF_VERSION

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

object versions {
    const val arrow = "0.10.4"
    const val autoService = "1.0-rc6"
    const val jackson = "2.10.2"
    const val kollection = "0.7"
    const val kotlin = "1.3.61"
    const val kotlinxCoroutines = "1.2.1"
    const val protobuf = DEFAULT_PROTOBUF_VERSION
    const val protobufPlugin = "0.8.11"
    const val stringTemplate = "4.3"

    // Test
    const val datasets = "0.1.0"
    const val gradleDownload = "4.0.4"
    const val jmh = "1.23"
    const val junit = "5.6.0"
    const val truth = "1.0.1"
    const val wire = "3.0.0-alpha01"

    // Third Party
    const val protoGoogleCommonProtos = "1.17.0"
}

object libraries {
    val arrow = listOf(
        "io.arrow-kt:arrow-core:${versions.arrow}",
        "io.arrow-kt:arrow-fx:${versions.arrow}",
        "io.arrow-kt:arrow-syntax:${versions.arrow}",
        "io.arrow-kt:arrow-free:${versions.arrow}",
        "io.arrow-kt:arrow-optics:${versions.arrow}"
    )

    const val autoService = "com.google.auto.service:auto-service:${versions.autoService}"
    const val autoServiceAnnotations = "com.google.auto.service:auto-service-annotations:${versions.autoService}"

    const val kollection = "com.github.andrewoma.dexx:kollection:${versions.kollection}"

    const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${versions.kotlin}"
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions.kotlin}"
    const val kotlinxCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlinxCoroutines}"

    const val protobufPlugin = "com.google.protobuf:protobuf-gradle-plugin:${versions.protobufPlugin}"
    const val protobuf = "com.google.protobuf:protobuf-java:${versions.protobuf}"
    const val protoc = "com.google.protobuf:protoc:${versions.protobuf}"

    const val stringTemplate = "org.antlr:ST4:${versions.stringTemplate}"

    // Test
    const val jackson = "com.fasterxml.jackson.module:jackson-module-kotlin:${versions.jackson}"
    const val junit = "org.junit.jupiter:junit-jupiter:${versions.junit}"
    const val truth = "com.google.truth:truth:${versions.truth}"

    // Benchmarks
    const val gradleDownload = "de.undercouch:gradle-download-task:${versions.gradleDownload}"

    const val jmhCore = "org.openjdk.jmh:jmh-core:${versions.jmh}"
    const val jmhGenerator = "org.openjdk.jmh:jmh-generator-annprocess:${versions.jmh}"

    const val wireGradle = "com.squareup.wire:wire-gradle-plugin:${versions.wire}"
    const val wireRuntime = "com.squareup.wire:wire-runtime:${versions.wire}"

    // Third Party
    const val protoGoogleCommonProtos = "com.google.api.grpc:proto-google-common-protos:${versions.protoGoogleCommonProtos}"
}
