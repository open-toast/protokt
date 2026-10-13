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

package protokt.v1.codegen.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.gradle.KOTLIN_TARGET
import protokt.v1.gradle.KotlinTarget

class PluginParamsTest {
    @Test
    fun `codegen uses shared generation defaults`() {
        val params = PluginParams(mapOf(KOTLIN_TARGET to KotlinTarget.Jvm.toString()))

        assertThat(params.generateTypes).isTrue()
        assertThat(params.generateDescriptors).isTrue()
        assertThat(params.generateGrpcDescriptors).isFalse()
        assertThat(params.generateGrpcKotlinStubs).isFalse()
        assertThat(params.generateGrpcKrpc).isFalse()
        assertThat(params.formatOutput).isTrue()
        assertThat(params.kotlinTarget).isSameInstanceAs(KotlinTarget.Jvm)
    }
}
