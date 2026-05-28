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

import kotlinx.benchmark.gradle.BenchmarksExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.configureBenchmarks() {
    configure<BenchmarksExtension> {
        configurations.named("main").configure {
            val wi = findProperty("benchmarkWarmups")?.toString()?.toInt() ?: 3
            val mi = findProperty("benchmarkIterations")?.toString()?.toInt() ?: 5
            val f = findProperty("benchmarkForks")?.toString()?.toInt() ?: 2

            warmups = wi
            iterations = mi
            iterationTime = 10
            iterationTimeUnit = "s"
            mode = "avgt"
            outputTimeUnit = "ms"
            advanced("jvmForks", f)
            advanced("jmhIgnoreLock", true)
            reportFormat = "json"

            findProperty("benchmarkInclude")?.toString()?.let { include(it) }
            findProperty("benchmarkExclude")?.toString()?.let { exclude(it) }
            findProperty("benchmarkParam")?.toString()?.split(",")?.forEach {
                val (name, value) = it.split("=", limit = 2)
                param(name, value)
            }
        }
    }
}
