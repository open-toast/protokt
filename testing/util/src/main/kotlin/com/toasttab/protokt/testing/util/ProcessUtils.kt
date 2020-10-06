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

package com.toasttab.protokt.testing.util

import com.toasttab.protokt.testing.util.ProcessOutput.Src.ERR
import com.toasttab.protokt.testing.util.ProcessOutput.Src.OUT
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.fail

val projectRoot =
    File(System.getProperty("user.dir")).parentFile.absolutePath

fun String.runCommand(
    workingDir: Path,
    env: Map<String, String> = emptyMap()
): ProcessOutput {
    println("Executing $this in $workingDir with $env")

    val proc =
        ProcessBuilder(*split("\\s".toRegex()).toTypedArray())
            .directory(workingDir.toFile())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .apply { environment().putAll(env) }
            .start()

    if (!proc.waitFor(60, TimeUnit.SECONDS)) {
        fail("Process '$this' took too long")
    }

    return ProcessOutput(
        proc.inputStream.bufferedReader().use { it.readText() },
        proc.errorStream.bufferedReader().use { it.readText() },
        proc.exitValue()
    )
}

data class ProcessOutput(
    val stdout: String,
    val stderr: String,
    val exitCode: Int
) {
    fun orFail(msg: String, src: Src) {
        if (exitCode == 0) {
            println(stdout)
        } else {
            fail(
                "$msg. Output:\n" +
                    when (src) {
                        OUT -> stdout
                        ERR -> stderr
                    }
            )
        }
    }

    enum class Src {
        OUT, ERR
    }
}
