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

package com.toasttab.protokt.v1.conformance

import com.toasttab.protokt.v1.testing.util.ProcessOutput
import com.toasttab.protokt.v1.testing.util.projectRoot
import com.toasttab.protokt.v1.testing.util.runCommand
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Path

class ConformanceTest {
    enum class ConformanceRunner(
        val project: String
    ) {
        JVM("jvm") {
            override fun driver() =
                jvmConformanceDriver
        },

        JS_IR("js-ir") {
            override fun driver() =
                jsConformanceDriver(project)
        };

        // js-legacy isn't bundling its dependencies correctly when built
        //
        // JS_LEGACY("js-legacy") {
        //     override fun driver() =
        //        jsConformanceDriver(project)
        // };

        abstract fun driver(): Path
    }

    companion object {
        @JvmStatic
        fun runners() =
            ConformanceRunner.values()
    }

    @ParameterizedTest
    @MethodSource("runners")
    fun `run conformance tests`(runner: ConformanceRunner) {
        command(runner)
            .runCommand(
                projectRoot.toPath(),
                libPathOverride
            )
            .orFail("Conformance tests failed", ProcessOutput.Src.ERR)

        println("Conformance tests passed")
    }
}

private fun <T> pivotOs(mac: T, linux: T) =
    with(System.getProperty("os.name")) {
        when {
            contains("Mac") -> mac
            contains("Linux") -> linux
            else -> fail("Unsupported OS")
        }
    }

private val binDir =
    Path.of("bin", pivotOs("darwin", "ubuntu-16.04-x86_64")).toString()

private val baseCommand =
    Path.of(binDir, "conformance-test-runner")

private val jvmConformanceDriver =
    Path.of(File(projectRoot.parentFile, "jvm").absolutePath, "build", "install", "protokt-conformance", "bin", "protokt-conformance")

private fun jsConformanceDriver(project: String) =
    Path.of(File(projectRoot.parentFile, project).absolutePath, "run.sh")

private fun failureList(project: String) =
    "--failure_list ../$project/failure_list_kt.txt"

private fun command(runner: ConformanceTest.ConformanceRunner) =
    "$baseCommand --enforce_recommended ${failureList(runner.project)} ${runner.driver()}"

private val libPathOverride =
    mapOf(
        pivotOs("DYLD_LIBRARY_PATH", "LD_LIBRARY_PATH") to
            Path.of(binDir, ".libs").toString()
    )
