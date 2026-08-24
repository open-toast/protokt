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

class GrpcNodeServiceGeneratorTest : AbstractProtoktCodegenTest() {
    @Test
    fun `coroutine stub accepts a caller-owned channel`() {
        val extension = ProtoktExtension().apply { generate { grpcKotlinLite() } }
        val generated =
            runJsPlugin("grpc_krpc_method_names.proto", extension)
                .orFail()
                .response
                .fileList
                .single { it.content.contains("object NamingServiceGrpcKt") }
                .content

        assertThat(generated).contains("public class NamingServiceCoroutineStub(\n    channel: Channel\n")
        assertThat(generated).doesNotContain("address: String")
        assertThat(generated).doesNotContain("credentials: ChannelCredentials")
    }
}
