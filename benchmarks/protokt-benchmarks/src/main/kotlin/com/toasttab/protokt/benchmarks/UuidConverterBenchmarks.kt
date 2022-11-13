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

import com.toasttab.protokt.ext.UuidConverter
import com.toasttab.protokt.ext.UuidConverter2
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.UUID
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
open class UuidConverterBenchmarks {
    private val uuids = List(100_000) { UUID.randomUUID() }
    private val unwrapped = uuids.map { UuidConverter.unwrap(it) }
    private val unwrappedAsSequence = unwrapped.map { it.asSequence() }

    @Benchmark
    fun unwrapWithByteBuffer(bh: Blackhole) {
        uuids.forEach {
            bh.consume(UuidConverter.unwrap(it).clone())
        }
    }

    @Benchmark
    fun unwrapWithBitShift(bh: Blackhole) {
        uuids.forEach {
            val arr = ByteArray(16)
            UuidConverter2.unwrap(it).forEachIndexed { index, byte ->
                arr[index] = byte
            }
            bh.consume(arr)
        }
    }

    @Benchmark
    fun wrapWithByteBuffer(bh: Blackhole) {
        unwrapped.forEach {
            bh.consume(UuidConverter.wrap(it))
        }
    }

    @Benchmark
    fun wrapWithBitShift(bh: Blackhole) {
        unwrappedAsSequence.forEach {
            bh.consume(UuidConverter2.wrap(it))
        }
    }
}

fun main() {
    run(UuidConverterBenchmarks::class)
}
