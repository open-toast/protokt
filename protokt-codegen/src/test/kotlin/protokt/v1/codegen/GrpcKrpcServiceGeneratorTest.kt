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

package protokt.v1.codegen

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.gradle.ProtoktExtension

class GrpcKrpcServiceGeneratorTest : AbstractProtoktCodegenTest() {
    @Test
    fun `method names use lower camel case`() {
        val extension = ProtoktExtension().apply { generate { grpcKrpcLite() } }
        val generated =
            runPlugin("grpc_krpc_method_names.proto", extension)
                .orFail()
                .response
                .fileList
                .single { it.content.contains("interface NamingService") }
                .content

        assertThat(generated).contains("fun getURL(")
        assertThat(generated).contains("fun x(")
        assertThat(generated).contains("fun ordinaryName(")
        assertThat(generated).doesNotContain("fun GetURL(")
        assertThat(generated).doesNotContain("fun X(")
        assertThat(generated).doesNotContain("fun OrdinaryName(")
    }
}
