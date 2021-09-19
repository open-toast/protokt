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

package com.toasttab.protokt.benchmarks

import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.io.File
import kotlin.reflect.KClass

fun readData(dataset: String) = File("../build/datasets/dataset-$dataset").inputStream().buffered()

fun run(self: KClass<*>) = Runner(
    OptionsBuilder()
        .include(".*" + self.simpleName + ".*")
        .warmupIterations(1)
        .measurementIterations(5)
        .forks(1)
        .resultFormat(ResultFormatType.JSON)
        .result("../build/jmh-${self.simpleName}.json")
        .build()
).run()
