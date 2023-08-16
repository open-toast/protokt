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

import com.toasttab.protokt.gradle.DEFAULT_PROTOBUF_VERSION

object versions {
    const val arrow = "1.1.3"
    const val autoService = "1.0.1"
    const val grpc = "1.49.1"
    const val grpcKotlin = "1.3.0"
    const val kotlinPoet = "1.12.0"
    const val kotlinxCollections = "0.3.5"
    const val kotlinxCoroutines = "1.6.4"
    const val protobuf = DEFAULT_PROTOBUF_VERSION
    const val protobufPlugin = "0.9.4"

    // Test
    const val jackson = "2.13.0"
    const val junit = "5.8.2"
    const val truth = "1.1.3"

    // Benchmarks
    const val datasets = "0.1.0"
    const val jmh = "1.26"
    const val wire = "4.2.0"

    // Third Party
    const val protoGoogleCommonProtos = "2.8.2"
}

object libraries {
    const val arrow = "io.arrow-kt:arrow-core:${versions.arrow}"

    const val autoService = "com.google.auto.service:auto-service:${versions.autoService}"
    const val autoServiceAnnotations = "com.google.auto.service:auto-service-annotations:${versions.autoService}"

    const val grpcKotlin = "io.grpc:grpc-kotlin-stub:${versions.grpcKotlin}"
    const val grpcKotlinGenerator = "io.grpc:protoc-gen-grpc-kotlin:${versions.grpcKotlin}:jdk8@jar"
    const val grpcNetty = "io.grpc:grpc-netty:${versions.grpc}"
    const val grpcStub = "io.grpc:grpc-stub:${versions.grpc}"

    const val kotlinPoet = "com.squareup:kotlinpoet:${versions.kotlinPoet}"
    const val kotlinxCollections = "org.jetbrains.kotlinx:kotlinx-collections-immutable:${versions.kotlinxCollections}"
    const val kotlinxCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlinxCoroutines}"

    const val protobufPlugin = "com.google.protobuf:protobuf-gradle-plugin:${versions.protobufPlugin}"
    const val protobufJava = "com.google.protobuf:protobuf-java:${versions.protobuf}"
    const val protobufLite = "com.google.protobuf:protobuf-javalite:${versions.protobuf}"
    const val protoc = "com.google.protobuf:protoc:${versions.protobuf}"

    // Test
    const val jackson = "com.fasterxml.jackson.module:jackson-module-kotlin:${versions.jackson}"
    const val junit = "org.junit.jupiter:junit-jupiter:${versions.junit}"
    const val truth = "com.google.truth:truth:${versions.truth}"

    // Benchmarks
    const val jmhCore = "org.openjdk.jmh:jmh-core:${versions.jmh}"
    const val jmhGenerator = "org.openjdk.jmh:jmh-generator-annprocess:${versions.jmh}"

    const val wireGradle = "com.squareup.wire:wire-gradle-plugin:${versions.wire}"
    const val wireRuntime = "com.squareup.wire:wire-runtime:${versions.wire}"

    // Third Party
    const val protoGoogleCommonProtos = "com.google.api.grpc:proto-google-common-protos:${versions.protoGoogleCommonProtos}"
}
