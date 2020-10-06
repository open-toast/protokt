/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.conformance

import com.toasttab.protokt.testing.util.ProcessOutput.Src.ERR
import com.toasttab.protokt.testing.util.projectRoot
import com.toasttab.protokt.testing.util.runCommand
import java.nio.file.Paths
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class ConformanceTest {
    @Test
    fun `run conformance tests`() {
        command
            .runCommand(
                Paths.get(projectRoot, "conformance-driver"),
                libPathOverride
            )
            .orFail("Conformance tests failed", ERR)

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
    Paths.get("bin", pivotOs("darwin", "ubuntu-16.04-x86_64")).toString()

private val baseCommand =
    Paths.get(binDir, "conformance-test-runner")

private val conformanceDriver =
    Paths.get(
        "build", "install", "protokt-conformance", "bin", "protokt-conformance"
    )

private val failureList =
    "--failure_list failure_list_kt.txt"

private val command =
    "$baseCommand --enforce_recommended $failureList $conformanceDriver"

private val libPathOverride =
    mapOf(
        pivotOs("DYLD_LIBRARY_PATH", "LD_LIBRARY_PATH") to
            Paths.get(binDir, ".libs").toString()
    )
