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

package com.toasttab.protokt.gradle

const val DEFAULT_PROTOBUF_VERSION = "3.19.4"

const val MANIFEST_VERSION_PROPERTY = "Implementation-Version"

const val KOTLIN_EXTRA_CLASSPATH = "kotlin_extra_classpath"
const val RESPECT_JAVA_PACKAGE = "respect_java_package"
const val GENERATE_GRPC = "generate_grpc"
const val ONLY_GENERATE_GRPC = "only_generate_grpc"
const val LITE = "lite"
const val ONLY_GENERATE_DESCRIPTORS = "only_generate_descriptors"

open class ProtoktExtension {
    /**
     * The version of protobuf to use for compilation
     */
    var protocVersion = DEFAULT_PROTOBUF_VERSION

    /**
     * Whether to respect protobuf's `java_package` option while generating code
     */
    var respectJavaPackage = true

    /**
     * Whether to generate gRPC-specific code such as MethodDescriptors and
     * ServiceDescriptors. If enabled, the project will have to import gRPC's
     * stub dependency to compile.
     */
    var generateGrpc = false

    /**
     * Whether to _only_ generate gRPC-specific code. Useful to generate
     * libraries that have already had a version compiled with `generateGrpc`
     * set to `false`.
     */
    var onlyGenerateGrpc = false

    /**
     * Whether to generate embedded descriptors for runtime reflective
     * access. Beware: if this option is enabled and any generated file depends
     * on a file generated in a different run of the code generator in which
     * this option was not enabled, the generated code will fail to compile.
     */
    var lite = false

    /**
     * Whether to _only_ generate descriptor code. Useful to generate
     * libraries that have already had a version compiled with `lite`
     * set to `true`.
     */
    var onlyGenerateDescriptors = false
}
