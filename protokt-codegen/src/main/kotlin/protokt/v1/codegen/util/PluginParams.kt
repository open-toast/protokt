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

import com.google.common.base.CaseFormat
import protokt.v1.gradle.APPLIED_KOTLIN_PLUGIN
import protokt.v1.gradle.FORMAT_OUTPUT
import protokt.v1.gradle.GENERATE_GRPC
import protokt.v1.gradle.KOTLIN_EXTRA_CLASSPATH
import protokt.v1.gradle.LITE
import protokt.v1.gradle.ONLY_GENERATE_DESCRIPTORS
import protokt.v1.gradle.ONLY_GENERATE_GRPC
import protokt.v1.gradle.ONLY_GENERATE_GRPC_DESCRIPTORS
import protokt.v1.gradle.ProtoktExtension
import java.net.URLDecoder
import kotlin.reflect.full.declaredMemberProperties

class PluginParams(
    params: Map<String, String>
) {
    val classLookup =
        ClassLookup(
            params.getOrDefault(KOTLIN_EXTRA_CLASSPATH, "")
                .split(";")
                .map { URLDecoder.decode(it, "UTF-8") }
        )

    val generateGrpc = params.getOrDefault(GENERATE_GRPC)
    val onlyGenerateGrpc = params.getOrDefault(ONLY_GENERATE_GRPC)
    val lite = params.getOrDefault(LITE)
    val onlyGenerateDescriptors = params.getOrDefault(ONLY_GENERATE_DESCRIPTORS)
    val onlyGenerateGrpcDescriptors = params.getOrDefault(ONLY_GENERATE_GRPC_DESCRIPTORS)
    val formatOutput = params.getOrDefault(FORMAT_OUTPUT)
    val appliedKotlinPlugin = params[APPLIED_KOTLIN_PLUGIN]?.toKotlinPluginEnum()
}

private fun Map<String, String>.getOrDefault(key: String): Boolean {
    val defaultExtension = ProtoktExtension()

    val defaultValue =
        defaultExtension::class.declaredMemberProperties
            .single { it.name == CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key) }
            .call(defaultExtension) as Boolean

    return get(key)?.toBoolean() ?: defaultValue
}

private fun String.toKotlinPluginEnum() =
    when (this) {
        "org.jetbrains.kotlin.multiplatform" -> KotlinPlugin.MULTIPLATFORM
        "org.jetbrains.kotlin.js" -> KotlinPlugin.JS
        "org.jetbrains.kotlin.jvm" -> KotlinPlugin.JVM
        "org.jetbrains.kotlin.android" -> KotlinPlugin.ANDROID
        else -> null
    }

enum class KotlinPlugin {
    MULTIPLATFORM,
    JS,
    JVM,
    ANDROID
}
