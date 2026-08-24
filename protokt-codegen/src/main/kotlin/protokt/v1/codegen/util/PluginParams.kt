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

package protokt.v1.codegen.util

import protokt.v1.gradle.DEFAULT_FORMAT_OUTPUT
import protokt.v1.gradle.DEFAULT_GENERATE_DESCRIPTORS
import protokt.v1.gradle.DEFAULT_GENERATE_GRPC_DESCRIPTORS
import protokt.v1.gradle.DEFAULT_GENERATE_GRPC_KOTLIN_STUBS
import protokt.v1.gradle.DEFAULT_GENERATE_GRPC_KRPC
import protokt.v1.gradle.DEFAULT_GENERATE_TYPES
import protokt.v1.gradle.FORMAT_OUTPUT
import protokt.v1.gradle.GENERATE_DESCRIPTORS
import protokt.v1.gradle.GENERATE_GRPC_DESCRIPTORS
import protokt.v1.gradle.GENERATE_GRPC_KOTLIN_STUBS
import protokt.v1.gradle.GENERATE_GRPC_KRPC
import protokt.v1.gradle.GENERATE_TYPES
import protokt.v1.gradle.KOTLIN_EXTRA_CLASSPATH
import protokt.v1.gradle.KOTLIN_TARGET
import protokt.v1.gradle.KotlinTarget
import protokt.v1.reflect.ClassLookup
import java.net.URLDecoder

internal class PluginParams(
    params: Map<String, String>
) {
    val classLookup =
        ClassLookup(
            params.getOrDefault(KOTLIN_EXTRA_CLASSPATH, "")
                .split(";")
                .map { URLDecoder.decode(it, "UTF-8") }
        )

    val generateTypes = params.boolean(GENERATE_TYPES, DEFAULT_GENERATE_TYPES)
    val generateDescriptors = params.boolean(GENERATE_DESCRIPTORS, DEFAULT_GENERATE_DESCRIPTORS)
    val generateGrpcDescriptors = params.boolean(GENERATE_GRPC_DESCRIPTORS, DEFAULT_GENERATE_GRPC_DESCRIPTORS)
    val generateGrpcKotlinStubs = params.boolean(GENERATE_GRPC_KOTLIN_STUBS, DEFAULT_GENERATE_GRPC_KOTLIN_STUBS)
    val generateGrpcKrpc = params.boolean(GENERATE_GRPC_KRPC, DEFAULT_GENERATE_GRPC_KRPC)
    val formatOutput = params.boolean(FORMAT_OUTPUT, DEFAULT_FORMAT_OUTPUT)
    val kotlinTarget = KotlinTarget.fromPluginOptionString(params.getValue(KOTLIN_TARGET))
}

private fun Map<String, String>.boolean(key: String, default: Boolean) =
    get(key)?.toBoolean() ?: default
