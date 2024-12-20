/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1.conformance

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import protokt.v1.testing.projectRoot
import protokt.v1.testing.runCommand
import java.io.File
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText

class ConformanceTest {
    enum class ConformanceRunner(
        val project: String,
        val timeout: Duration
    ) {
        JVM("jvm", Duration.ofSeconds(10)) {
            override fun driver() =
                jvmConformanceDriver
        },

        JS_IR("js-ir", Duration.ofSeconds(100)) {
            override fun driver() =
                jsConformanceDriver(project)

            override fun onFailure() {
                val stderr = jsStderrLog(project).toFile()
                if (stderr.exists() && stderr.readText().isNotEmpty()) {
                    println("stderr:\n" + stderr.readText())
                }
            }
        }, // https://github.com/pinterest/ktlint/issues/1933
        ;

        abstract fun driver(): Path

        open fun onFailure() =
            Unit
    }

    @BeforeEach
    fun deleteFailingTests() {
        failingTests.deleteIfExists()
    }

    @ParameterizedTest
    @EnumSource
    fun `run conformance tests`(runner: ConformanceRunner) {
        try {
            val output = command(runner).runCommand(projectRoot.toPath(), timeout = runner.timeout)
            println(output.stderr)

            assertThat(output.stderr).contains("CONFORMANCE SUITE PASSED")
            val matches = " (\\d+) unexpected failures".toRegex().findAll(output.stderr).toList()
            // the current implementation runs two conformance suites
            assertThat(matches).hasSize(2)
            matches.forEach { assertThat(it.groupValues[1].toInt()).isEqualTo(0) }
            assertThat(output.exitCode).isEqualTo(0)
        } catch (t: Throwable) {
            if (failingTests.exists()) {
                println("Failing tests:\n" + failingTests.readText())
            }
            runner.onFailure()
            throw t
        }

        println("Conformance tests passed")
    }
}

private val jvmConformanceDriver =
    Path.of(File(projectRoot.parentFile, "jvm").absolutePath, "build", "install", "protokt-conformance", "bin", "protokt-conformance")

private fun jsConformanceDriver(project: String) =
    Path.of(File(projectRoot.parentFile, project).absolutePath, "run.sh")

private fun jsStderrLog(project: String) =
    Path.of(File(projectRoot.parentFile, project).absolutePath, "build", "conformance-run")

private val failingTests =
    Path.of(projectRoot.absolutePath, "failing_tests.txt")

private fun failureList(project: String) =
    "--failure_list ../$project/failure_list_kt.txt"

private fun command(runner: ConformanceTest.ConformanceRunner) =
    "${System.getProperty("conformance-runner")} --enforce_recommended ${failureList(runner.project)} ${runner.driver()}"
