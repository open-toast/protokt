/*
 * Copyright (c) 2024 Toast, Inc.
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

internal const val KOTLIN_EXTRA_CLASSPATH = "kotlin_extra_classpath"
internal const val GENERATE_TYPES = "generate_types"
internal const val GENERATE_DESCRIPTORS = "generate_descriptors"
internal const val GENERATE_GRPC_DESCRIPTORS = "generate_grpc_descriptors"
internal const val GENERATE_GRPC_KOTLIN_STUBS = "generate_grpc_kotlin_stubs"
internal const val FORMAT_OUTPUT = "format_output"
internal const val KOTLIN_TARGET = "kotlin_target"

private val namesByKotlinTarget =
    mapOf(
        KotlinTarget.Android to "android",
        KotlinTarget.Jvm to "jvm",
        KotlinTarget.MultiplatformAndroid to "android-mp",
        KotlinTarget.MultiplatformCommon to "common-mp",
        KotlinTarget.MultiplatformJs to "js-mp",
        KotlinTarget.MultiplatformJvm to "jvm-mp"
    )

private val kotlinTargetsByName =
    namesByKotlinTarget.entries.associate { (k, v) -> v to k }

internal sealed class KotlinTarget(
    val isPrimaryTarget: Boolean,
    val treatTargetAsJvm: Boolean,
    val name: String
) {
    val protocPluginName = "protokt" + if (this::class.java.simpleName.startsWith("Multiplatform")) "-$name" else ""

    object MultiplatformCommon : KotlinTarget(true, false, "common")
    object MultiplatformJs : KotlinTarget(false, false, "js")
    object MultiplatformJvm : KotlinTarget(false, true, "jvm")
    object MultiplatformAndroid : KotlinTarget(false, true, "android")
    class MultiplatformOther(name: String) : KotlinTarget(false, false, name)

    object Jvm : KotlinTarget(true, true, "jvm")
    object Android : KotlinTarget(true, true, "android")

    override fun toString() =
        namesByKotlinTarget[this] ?: name

    companion object {
        fun fromPluginOptionString(target: String) =
            kotlinTargetsByName[target] ?: MultiplatformOther(target.removeSuffix("-mp"))

        fun fromMultiplatformTargetString(target: String) =
            fromPluginOptionString("$target-mp")
    }
}
