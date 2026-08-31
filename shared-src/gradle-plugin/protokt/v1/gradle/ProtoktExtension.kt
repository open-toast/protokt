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

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Configures the protobuf compiler, generated output, runtime codec, and collection implementation.
 */
open class ProtoktExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * The version of protobuf to use for compilation.
     */
    val protocVersion: Property<String> = objects.property(String::class.java).convention(DEFAULT_PROTOBUF_VERSION)

    internal val generate: Generate = objects.newInstance(Generate::class.java)

    fun generate(configure: Generate.() -> Unit) {
        generate.configure()
    }

    internal val codec: Codec = objects.newInstance(Codec::class.java)

    fun codec(configure: Codec.() -> Unit) {
        codec.configure()
    }

    internal val collections: Collections = objects.newInstance(Collections::class.java)

    fun collections(configure: Collections.() -> Unit) {
        collections.configure()
    }

    /**
     * Whether to format the generated code.
     */
    val formatOutput: Property<Boolean> = objects.property(Boolean::class.java).convention(DEFAULT_FORMAT_OUTPUT)

    internal fun finalizeValues() {
        protocVersion.finalizeValue()
        formatOutput.finalizeValue()
        generate.finalizeValues()
        codec.selection.finalizeValue()
        collections.selection.finalizeValue()
    }

    open class Generate @Inject constructor(objects: ObjectFactory) {
        /**
         * Whether to generate messages and enums.
         */
        val types: Property<Boolean> = objects.property(Boolean::class.java).convention(DEFAULT_GENERATE_TYPES)

        /**
         * Whether to generate embedded descriptors for runtime reflective access.
         *
         * Every generated dependency must also contain embedded descriptors.
         */
        val descriptors: Property<Boolean> = objects.property(Boolean::class.java).convention(DEFAULT_GENERATE_DESCRIPTORS)

        /**
         * Whether to generate platform-neutral gRPC descriptors.
         */
        val grpcDescriptors: Property<Boolean> = objects.property(Boolean::class.java).convention(DEFAULT_GENERATE_GRPC_DESCRIPTORS)

        /**
         * Whether to generate Kotlin coroutine gRPC bindings.
         */
        val grpcKotlinStubs: Property<Boolean> = objects.property(Boolean::class.java).convention(DEFAULT_GENERATE_GRPC_KOTLIN_STUBS)

        /**
         * Whether to generate kotlinx-rpc service interfaces.
         */
        val grpcKrpc: Property<Boolean> = objects.property(Boolean::class.java).convention(DEFAULT_GENERATE_GRPC_KRPC)

        /**
         * Generates only message and enum types.
         */
        fun lite() {
            set(types = true, descriptors = false, grpcDescriptors = false, grpcKotlinStubs = false, grpcKrpc = false)
        }

        /**
         * Generates message and enum types with platform-neutral gRPC descriptors.
         */
        fun grpcLite() {
            set(types = true, descriptors = false, grpcDescriptors = true, grpcKotlinStubs = false, grpcKrpc = false)
        }

        /**
         * Generates messages, descriptors, and Kotlin coroutine gRPC bindings.
         */
        fun grpcKotlin() {
            set(types = true, descriptors = true, grpcDescriptors = true, grpcKotlinStubs = true, grpcKrpc = false)
        }

        /**
         * Generates messages and Kotlin coroutine gRPC bindings without embedded descriptors.
         */
        fun grpcKotlinLite() {
            set(types = true, descriptors = false, grpcDescriptors = true, grpcKotlinStubs = true, grpcKrpc = false)
        }

        /**
         * Generates messages and kotlinx-rpc service interfaces without embedded descriptors.
         */
        fun grpcKrpcLite() {
            set(types = true, descriptors = false, grpcDescriptors = false, grpcKotlinStubs = false, grpcKrpc = true)
        }

        internal fun finalizeValues() {
            types.finalizeValue()
            descriptors.finalizeValue()
            grpcDescriptors.finalizeValue()
            grpcKotlinStubs.finalizeValue()
            grpcKrpc.finalizeValue()
        }

        private fun set(
            types: Boolean,
            descriptors: Boolean,
            grpcDescriptors: Boolean,
            grpcKotlinStubs: Boolean,
            grpcKrpc: Boolean
        ) {
            this.types.set(types)
            this.descriptors.set(descriptors)
            this.grpcDescriptors.set(grpcDescriptors)
            this.grpcKotlinStubs.set(grpcKotlinStubs)
            this.grpcKrpc.set(grpcKrpc)
        }
    }

    open class Codec @Inject constructor(objects: ObjectFactory) {
        internal val selection: Property<CodecSelection> =
            objects.property(CodecSelection::class.java).convention(CodecSelection.OPTIMAL)

        fun optimal() {
            selection.set(CodecSelection.OPTIMAL)
        }

        fun optimalKmp() {
            selection.set(CodecSelection.OPTIMAL_KMP)
        }

        fun optimalJvm() {
            selection.set(CodecSelection.OPTIMAL_JVM)
        }

        fun optimalJvmLite() {
            selection.set(CodecSelection.OPTIMAL_JVM_LITE)
        }

        fun protobufJava() {
            selection.set(CodecSelection.PROTOBUF_JAVA)
        }

        fun protobufJavalite() {
            selection.set(CodecSelection.PROTOBUF_JAVALITE)
        }

        fun minimal() {
            selection.set(CodecSelection.MINIMAL)
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

    open class Collections @Inject constructor(objects: ObjectFactory) {
        internal val selection: Property<CollectionsSelection> =
            objects.property(CollectionsSelection::class.java).convention(CollectionsSelection.PERSISTENT)

        fun persistent() {
            selection.set(CollectionsSelection.PERSISTENT)
        }

        fun minimal() {
            selection.set(CollectionsSelection.MINIMAL)
        }
    }

    enum class CollectionsSelection {
        PERSISTENT,
        MINIMAL
    }
}
