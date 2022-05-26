/*
 * Copyright (c) 2021 Toast Inc.
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

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.toasttab.protokt.gradle.protokt

localProtokt()
pureKotlin()

protokt {
    generateGrpc = true
    lite = true
}

protobuf {
    plugins {
        id("grpckt") {
            artifact = libraries.grpcKotlinGenerator
        }
    }

    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpckt")
            }
        }
    }
}

dependencies {
    protobuf(project(":examples:protos"))

    implementation(libraries.grpcKotlin)
    implementation(libraries.jackson)
    implementation(libraries.kotlinxCoroutinesCore)

    runtimeOnly(libraries.protobufLite)

    testImplementation(kotlin("test-junit"))
    testImplementation("io.grpc:grpc-testing:${versions.grpc}")
}

sourceSets {
    main {
        java {
            srcDir("../grpc-kotlin/src/main/kotlin")
            srcDir("../protos/src/main/kotlin")
        }
    }

    test {
        java {
            srcDir("../grpc-kotlin/src/test/kotlin")
        }
    }
}
