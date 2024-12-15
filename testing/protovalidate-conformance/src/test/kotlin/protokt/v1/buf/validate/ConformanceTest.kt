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

package protokt.v1.buf.validate

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.testing.projectRoot
import protokt.v1.testing.runCommand
import java.nio.file.Path
import java.time.Duration

class ConformanceTest {
    @Test
    fun `run conformance test`() {
        try {
            val output = command().runCommand(
                projectRoot.toPath(),
                env = mapOf(
                    "JAVA_OPTS" to "-Xmx128M",
                    "GOMEMLIMIT" to "24MiB"
                ),
                timeout = Duration.ofMinutes(20)
            )
            println(output.stderr)
            assertThat(output.stderr).startsWith("PASS")
            assertThat(output.stderr).contains("failed: 0")
            assertThat(output.exitCode).isEqualTo(0)
        } catch (t: Throwable) {
            t.printStackTrace()
            throw t
        }
    }
}

private fun command() =
    "${System.getProperty("conformance-runner")} --strict_message --strict_error --expected_failures expected_failures.yaml $driver"

private val driver =
    Path.of(projectRoot.absolutePath, "build", "install", "protovalidate-conformance", "bin", "protovalidate-conformance")
