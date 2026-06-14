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

package protokt.v1.testing

import org.junit.jupiter.api.fail
import protokt.v1.testing.ProcessOutput.Src.ERR
import protokt.v1.testing.ProcessOutput.Src.OUT
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

val projectRoot =
    File(System.getProperty("user.dir"))

fun String.runCommand(
    workingDir: Path,
    env: Map<String, String> = emptyMap(),
    timeout: Duration = Duration.ofSeconds(60)
): ProcessOutput {
    println("Executing $this in $workingDir with $env")

    val proc =
        ProcessBuilder(*split("\\s".toRegex()).toTypedArray())
            .directory(workingDir.toFile())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .apply { environment().putAll(env) }
            .start()

    // Drain stdout/stderr concurrently so the child doesn't block on a full
    // pipe buffer (macOS/Linux pipes are typically 64KB).
    val stdoutFuture = readStreamAsync(proc.inputStream)
    val stderrFuture = readStreamAsync(proc.errorStream)

    if (!proc.waitFor(timeout.toSeconds(), TimeUnit.SECONDS)) {
        proc.destroyForcibly()
        fail("Process '$this' took too long")
    }

    return ProcessOutput(
        stdoutFuture.get(),
        stderrFuture.get(),
        proc.exitValue()
    )
}

private fun readStreamAsync(stream: InputStream): Future<String> {
    val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r).apply { isDaemon = true }
    }
    return executor.submit<String> { stream.bufferedReader().use { it.readText() } }
        .also { executor.shutdown() }
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
        OUT,
        ERR
    }
}
