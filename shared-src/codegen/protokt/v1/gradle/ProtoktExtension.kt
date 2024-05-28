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

package protokt.v1.gradle

const val KOTLIN_EXTRA_CLASSPATH = "kotlin_extra_classpath"
const val GENERATE_TYPES = "generate_types"
const val GENERATE_DESCRIPTORS = "generate_descriptors"
const val GENERATE_GRPC_DESCRIPTORS = "generate_grpc_descriptors"
const val GENERATE_GRPC_KOTLIN_STUBS = "generate_grpc_kotlin_stubs"
const val FORMAT_OUTPUT = "format_output"
const val KOTLIN_TARGET = "kotlin_target"

open class ProtoktExtension {
    /**
     * The version of protobuf to use for compilation
     */
    var protocVersion = DEFAULT_PROTOBUF_VERSION

    internal var generate: Generate = Generate()

    fun generate(configure: Generate.() -> Unit) {
        generate.configure()
    }

    /**
     * Whether to format the generated code.
     */
    var formatOutput = true

    class Generate {
        /**
         * Whether to messages and enums.
         */
        var types = true

        /**
         * Whether to generate embedded descriptors for runtime reflective
         * access. Beware: if this option is enabled and any generated file depends
         * on a file generated in a different run of the code generator in which
         * this option was not enabled, the generated code will fail to compile.
         */
        var descriptors = true

        /**
         * Whether to generate gRPC-specific code such as MethodDescriptors and
         * ServiceDescriptors. If enabled, the project will have to import gRPC's
         * stub dependency to compile.
         */
        var grpcDescriptors = false

        /**
         * Whether to generate Kotlin coroutine-based bindings for gRPC code. If
         * enabled, the project will have to import gRPC's Kotlin stub dependency
         * to compile.
         */
        var grpcKotlinStubs = false

        /**
         * Generates only message and enum types.
         */
        fun lite() {
            types = true
            descriptors = false
            grpcDescriptors = false
            grpcKotlinStubs = false
        }

        /**
         * Generates message and enum types as well as gRPC descriptors not tied
         * to any implementation of gRPC on the target platform.
         */
        fun grpcLite() {
            types = true
            descriptors = false
            grpcDescriptors = true
            grpcKotlinStubs = false
        }

        /**
         * Generates message and enum types as well as gRPC descriptors and Kotlin
         * coroutine-based implementations.
         */
        fun grpcKotlinLite() {
            types = true
            descriptors = false
            grpcDescriptors = true
            grpcKotlinStubs = true
        }

        /**
         * Generates all variations of code.
         */
        fun all() {
            types = true
            descriptors = true
            grpcDescriptors = true
            grpcKotlinStubs = true
        }
    }
}
