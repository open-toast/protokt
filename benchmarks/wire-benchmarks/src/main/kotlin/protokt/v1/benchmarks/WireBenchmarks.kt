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

import com.google.protobuf.benchmarks.BenchmarkDataset
import com.toasttab.protokt.v1.benchmarks.GenericMessage1
import com.toasttab.protokt.v1.benchmarks.GenericMessage4
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
open class WireBenchmarks {
    private lateinit var largeDataset: BenchmarkDataset
    private lateinit var largeParsedDataset: List<GenericMessage1>
    private lateinit var mediumDataset: BenchmarkDataset
    private lateinit var mediumParsedDataset: List<GenericMessage1>
    private lateinit var smallDataset: BenchmarkDataset
    private lateinit var smallParsedDataset: List<GenericMessage4>

    @Setup
    fun setup() {
        readData("large").use { stream ->
            largeDataset = BenchmarkDataset.ADAPTER.decode(stream)
        }
        largeParsedDataset = largeDataset.payload.map { GenericMessage1.ADAPTER.decode(it) }
        readData("medium").use { stream ->
            mediumDataset = BenchmarkDataset.ADAPTER.decode(stream)
        }
        mediumParsedDataset = mediumDataset.payload.map { GenericMessage1.ADAPTER.decode(it) }
        readData("small").use { stream ->
            smallDataset = BenchmarkDataset.ADAPTER.decode(stream)
        }
        smallParsedDataset = smallDataset.payload.map { GenericMessage4.ADAPTER.decode(it) }
    }

    @Benchmark
    fun deserializeLargeFromMemory(bh: Blackhole) {
        largeDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.ADAPTER.decode(bytes))
        }
    }

    @Benchmark
    fun deserializeMediumFromMemory(bh: Blackhole) {
        mediumDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.ADAPTER.decode(bytes))
        }
    }

    @Benchmark
    fun deserializeSmallFromMemory(bh: Blackhole) {
        smallDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage4.ADAPTER.decode(bytes))
        }
    }

    @Benchmark
    fun serializeLargeToMemory(bh: Blackhole) {
        largeParsedDataset.forEach { msg -> bh.consume(GenericMessage1.ADAPTER.encode(msg)) }
    }

    @Benchmark
    fun serializeMediumToMemory(bh: Blackhole) {
        mediumParsedDataset.forEach { msg -> bh.consume(GenericMessage1.ADAPTER.encode(msg)) }
    }

    @Benchmark
    fun serializeSmallToMemory(bh: Blackhole) {
        smallParsedDataset.forEach { msg -> bh.consume(GenericMessage4.ADAPTER.encode(msg)) }
    }
}

fun main() {
    run(WireBenchmarks::class)
}
