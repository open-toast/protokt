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
import com.squareup.kotlinpoet.asClassName
import protokt.v1.gradle.KOTLIN_TARGET
import protokt.v1.gradle.FORMAT_OUTPUT
import protokt.v1.gradle.GENERATE_DESCRIPTORS
import protokt.v1.gradle.GENERATE_GRPC_DESCRIPTORS
import protokt.v1.gradle.GENERATE_GRPC_KOTLIN_STUBS
import protokt.v1.gradle.GENERATE_TYPES
import protokt.v1.gradle.KOTLIN_EXTRA_CLASSPATH
import protokt.v1.gradle.ProtoktExtension
import protokt.v1.gradle.ProtoktExtension.Generate
import protokt.v1.reflect.ClassLookup
import java.net.URLDecoder
import kotlin.reflect.full.declaredMemberProperties

internal class PluginParams(
    params: Map<String, String>
) {
    val classLookup =
        ClassLookup(
            params.getOrDefault(KOTLIN_EXTRA_CLASSPATH, "")
                .split(";")
                .map { URLDecoder.decode(it, "UTF-8") }
        )

    val generateTypes = params.getOrDefault<Generate>(GENERATE_TYPES)
    val generateDescriptors = params.getOrDefault<Generate>(GENERATE_DESCRIPTORS)
    val generateGrpcDescriptors = params.getOrDefault<Generate>(GENERATE_GRPC_DESCRIPTORS)
    val generateGrpcKotlinStubs = params.getOrDefault<Generate>(GENERATE_GRPC_KOTLIN_STUBS)
    val formatOutput = params.getOrDefault<ProtoktExtension>(FORMAT_OUTPUT)
    val kotlinTarget = params[KOTLIN_TARGET]?.toKotlinPluginEnum()
}

private inline fun <reified T> Map<String, String>.getOrDefault(key: String): Boolean {
    val inParams = get(key)?.toBoolean()
    return if (inParams != null) {
        inParams
    } else {
        val default = T::class.constructors.single().call()!!

        val prefix =
            T::class.asClassName()
                .simpleNames
                .filter { it != ProtoktExtension::class.simpleName }
                .joinToString("_") { it.lowercase() }

        default::class.declaredMemberProperties
            .single {
                it.name == CaseFormat.LOWER_UNDERSCORE.to(
                    CaseFormat.LOWER_CAMEL,
                    key.removePrefix(prefix + "_")
                )
            }
            .call(default) as Boolean
    }
}

private fun String.toKotlinPluginEnum() =
    when (this) {
        "common" -> KotlinTarget.COMMON
        "js" -> KotlinTarget.JS
        "jvm" -> KotlinTarget.JVM
        "android" -> KotlinTarget.ANDROID
        else -> null
    }

enum class KotlinTarget {
    COMMON,
    JS,
    JVM,
    ANDROID
}
