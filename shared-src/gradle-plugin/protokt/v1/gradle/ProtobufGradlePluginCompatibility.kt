/*
 * Copyright (c) 2026 Toast, Inc.
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

import com.google.protobuf.gradle.ProtobufPlugin
import org.gradle.api.GradleException

internal object ProtobufGradlePluginCompatibility {
    private const val KOTLIN_MULTIPLATFORM_PLUGIN = "org.jetbrains.kotlin.multiplatform"
    private var enabled = false

    @Synchronized
    fun enableMultiplatform() {
        if (enabled) {
            return
        }

        val prerequisitePluginsUntyped =
            try {
                val field = ProtobufPlugin::class.java.getDeclaredField("PREREQ_PLUGIN_OPTIONS")
                field.isAccessible = true
                field.get(null)
            } catch (e: Exception) {
                throw GradleException("protokt requires protobuf-gradle-plugin 0.10.0 for Kotlin Multiplatform projects", e)
            }

        if (prerequisitePluginsUntyped !is MutableList<*> || prerequisitePluginsUntyped.any { it !is String }) {
            throw GradleException("protokt requires protobuf-gradle-plugin 0.10.0 for Kotlin Multiplatform projects")
        }

        @Suppress("UNCHECKED_CAST")
        val prerequisitePlugins = prerequisitePluginsUntyped as MutableList<String>

        if (KOTLIN_MULTIPLATFORM_PLUGIN !in prerequisitePlugins) {
            prerequisitePlugins.add(KOTLIN_MULTIPLATFORM_PLUGIN)
        }
        enabled = true
    }
}
