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
import java.util.Random
import kotlin.reflect.KClass

fun readData(dataset: String) =
    File("../build/datasets/dataset-$dataset").inputStream().buffered()

/** Mix of 1-byte (ASCII), 2-byte (Latin Extended), and 3-byte (CJK) UTF-8 characters. */
fun randomUtf8String(random: Random, charCount: Int): String {
    val sb = StringBuilder(charCount)
    repeat(charCount) {
        sb.append(
            when (random.nextInt(3)) {
                0 -> ('a' + random.nextInt(26)).toChar()
                1 -> (0x00C0 + random.nextInt(64)).toChar()
                else -> (0x4E00 + random.nextInt(0x5000)).toChar()
            }
        )
    }
    return sb.toString()
}

fun run(self: KClass<*>, args: Array<String> = emptyArray()) {
    val opts = OptionsBuilder()
        .warmupIterations(3)
        .measurementIterations(5)
        .forks(2)
        .resultFormat(ResultFormatType.JSON)
        .result("../build/jmh-${self.simpleName}.json")

    var hasInclude = false

    args.toList()
        .windowed(2, 2, partialWindows = false)
        .forEach { (flag, spec) ->
            when (flag) {
                "-i" -> {
                    hasInclude = true
                    opts.include(".*" + self.simpleName + "." + spec + ".*")
                }
                "-e" -> opts.exclude(spec)
                "-p" -> {
                    val (name, value) = spec.split("=", limit = 2)
                    opts.param(name, value)
                }
            }
        }

    if (!hasInclude) {
        opts.include(".*" + self.simpleName + ".*")
    }

    Runner(opts.build()).run()
}
