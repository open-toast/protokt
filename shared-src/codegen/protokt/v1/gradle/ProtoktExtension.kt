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

open class ProtoktExtension {
    /**
     * The version of protobuf to use for compilation
     */
    var protocVersion = DEFAULT_PROTOBUF_VERSION

    internal var generate: Generate = Generate()

    fun generate(configure: Generate.() -> Unit) {
        generate.configure()
    }

    internal var codec: Codec = Codec()

    fun codec(configure: Codec.() -> Unit) {
        codec.configure()
    }

    internal var collections: Collections = Collections()

    fun collections(configure: Collections.() -> Unit) {
        collections.configure()
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
         * Whether to generate @Grpc-annotated interfaces for use with the
         * kotlinx-rpc compiler plugin. If enabled, the project must apply the
         * kotlinx-rpc Gradle plugin and depend on kotlinx-rpc-grpc-core.
         */
        var grpcKrpc = false

        /**
         * Generates only message and enum types.
         */
        fun lite() {
            types = true
            descriptors = false
            grpcDescriptors = false
            grpcKotlinStubs = false
            grpcKrpc = false
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
            grpcKrpc = false
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
            grpcKrpc = false
        }

        /**
         * Generates message and enum types and @Grpc-annotated service
         * interfaces for use with the kotlinx-rpc compiler plugin.
         */
        fun grpcKrpcLite() {
            types = true
            descriptors = false
            grpcDescriptors = false
            grpcKotlinStubs = false
            grpcKrpc = true
        }
    }

    class Codec {
        internal var selection: CodecSelection = CodecSelection.OPTIMAL

        fun optimal() {
            selection = CodecSelection.OPTIMAL
        }

        fun optimalKmp() {
            selection = CodecSelection.OPTIMAL_KMP
        }

        fun optimalJvm() {
            selection = CodecSelection.OPTIMAL_JVM
        }

        fun optimalJvmLite() {
            selection = CodecSelection.OPTIMAL_JVM_LITE
        }

        fun protobufJava() {
            selection = CodecSelection.PROTOBUF_JAVA
        }

        fun protobufJavalite() {
            selection = CodecSelection.PROTOBUF_JAVALITE
        }

        fun minimal() {
            selection = CodecSelection.MINIMAL
        }
    }

    enum class CodecSelection {
        OPTIMAL,
        OPTIMAL_KMP,
        OPTIMAL_JVM,
        OPTIMAL_JVM_LITE,
        PROTOBUF_JAVA,
        PROTOBUF_JAVALITE,
        MINIMAL
    }

    class Collections {
        internal var selection: CollectionsSelection = CollectionsSelection.PERSISTENT

        fun persistent() {
            selection = CollectionsSelection.PERSISTENT
        }

        fun minimal() {
            selection = CollectionsSelection.MINIMAL
        }
    }

    enum class CollectionsSelection {
        PERSISTENT,
        MINIMAL
    }
}
