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

package protokt.v1.benchmarks

import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.io.File
import kotlin.reflect.KClass

fun readData(dataset: String) =
    File("../build/datasets/dataset-$dataset").inputStream().buffered()

actual fun readDatasetBytes(name: String): ByteArray =
    File("../build/datasets/dataset-$name").readBytes()

fun run(self: KClass<*>, args: Array<String> = emptyArray()) {
    var resultSuffix = ""
    val opts = OptionsBuilder()
        .warmupIterations(3)
        .measurementIterations(5)
        .forks(2)
        .resultFormat(ResultFormatType.JSON)

    var hasInclude = false

    args.toList()
        .windowed(2, 2, partialWindows = false)
        .forEach { (flag, spec) ->
            when (flag) {
                "-o" -> resultSuffix = "-$spec"

                "-i" -> {
                    hasInclude = true
                    opts.include(".*" + self.simpleName + "." + spec + ".*")
                }

                "-e" -> opts.exclude(spec)

                "-p" -> {
                    val (name, value) = spec.split("=", limit = 2)
                    opts.param(name, *value.split(",").toTypedArray())
                }

                "-prof" -> {
                    val parts = spec.split(":", limit = 2)
                    if (parts.size == 2) opts.addProfiler(parts[0], parts[1]) else opts.addProfiler(parts[0])
                }

                "-wi" -> opts.warmupIterations(spec.toInt())

                "-mi" -> opts.measurementIterations(spec.toInt())

                "-f" -> opts.forks(spec.toInt())

                "-jvmArgs" -> opts.jvmArgs(spec)
            }
        }

    opts.result("../build/jmh-${self.simpleName}$resultSuffix.json")
    if (!hasInclude) opts.include(".*" + self.simpleName + ".*")
    Runner(opts.build()).run()
}
