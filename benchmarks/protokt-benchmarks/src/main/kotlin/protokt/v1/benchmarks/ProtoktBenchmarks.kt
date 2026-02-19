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

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import protokt.v1.Bytes
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
open class ProtoktBenchmarks {
    @Param("protokt.v1.DefaultCollectionFactory", "protokt.v1.PersistentCollectionFactory")
    var collectionFactory: String = "protokt.v1.DefaultCollectionFactory"

    private lateinit var largeDataset: BenchmarkDataset
    private lateinit var largeParsedDataset: List<GenericMessage1>

    private lateinit var mediumDataset: BenchmarkDataset
    private lateinit var mediumParsedDataset: List<GenericMessage1>

    private lateinit var smallDataset: BenchmarkDataset
    private lateinit var smallParsedDataset: List<GenericMessage4>

    private lateinit var byteValues: Array<Bytes>

    @Setup
    fun setup() {
        byteValues = Array(1000) { i -> Bytes.from(byteArrayOf(i.toByte())) }

        System.setProperty("protokt.collection.factory", collectionFactory)

        readData("large").use { stream ->
            largeDataset = BenchmarkDataset.deserialize(stream)
        }
        largeParsedDataset = largeDataset.payload.map { GenericMessage1.deserialize(it) }

        readData("medium").use { stream ->
            mediumDataset = BenchmarkDataset.deserialize(stream)
        }
        mediumParsedDataset = mediumDataset.payload.map { GenericMessage1.deserialize(it) }

        readData("small").use { stream ->
            smallDataset = BenchmarkDataset.deserialize(stream)
        }
        smallParsedDataset = smallDataset.payload.map { GenericMessage4.deserialize(it) }
    }

    @Benchmark
    fun deserializeLargeFromMemory(bh: Blackhole) {
        largeDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.deserialize(bytes))
        }
    }

    @Benchmark
    fun deserializeMediumFromMemory(bh: Blackhole) {
        mediumDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.deserialize(bytes))
        }
    }

    @Benchmark
    fun deserializeSmallFromMemory(bh: Blackhole) {
        smallDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage4.deserialize(bytes))
        }
    }

    @Benchmark
    fun serializeLargeToMemory(bh: Blackhole) {
        largeParsedDataset.forEach { msg -> bh.consume(msg.serialize()) }
    }

    @Benchmark
    fun serializeMediumToMemory(bh: Blackhole) {
        mediumParsedDataset.forEach { msg -> bh.consume(msg.serialize()) }
    }

    @Benchmark
    fun serializeSmallToMemory(bh: Blackhole) {
        smallParsedDataset.forEach { msg -> bh.consume(msg.serialize()) }
    }

    @Benchmark
    fun copyAppendListLarge(bh: Blackhole) {
        var msg = largeParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.copy { fieldBytes1500 = fieldBytes1500 + byteValues[i] }
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapLarge(bh: Blackhole) {
        var msg = largeParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.copy { fieldMap5000 = fieldMap5000 + ("key$i" to i.toLong()) }
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendListMedium(bh: Blackhole) {
        var msg = mediumParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.copy { fieldBytes1500 = fieldBytes1500 + byteValues[i] }
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapMedium(bh: Blackhole) {
        var msg = mediumParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.copy { fieldMap5000 = fieldMap5000 + ("key$i" to i.toLong()) }
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendListSmall(bh: Blackhole) {
        var msg = mediumParsedDataset.first().copy { fieldBytes1500 = emptyList() }
        repeat(1000) { i ->
            msg = msg.copy { fieldBytes1500 = fieldBytes1500 + byteValues[i] }
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapSmall(bh: Blackhole) {
        var msg = mediumParsedDataset.first().copy { fieldMap5000 = emptyMap() }
        repeat(1000) { i ->
            msg = msg.copy { fieldMap5000 = fieldMap5000 + ("key$i" to i.toLong()) }
        }
        bh.consume(msg)
    }

    @Benchmark
    fun passThroughLargeFromMemory(bh: Blackhole) {
        largeDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.deserialize(bytes).serialize())
        }
    }

    @Benchmark
    fun passThroughMediumFromMemory(bh: Blackhole) {
        mediumDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.deserialize(bytes).serialize())
        }
    }

    @Benchmark
    fun passThroughSmallFromMemory(bh: Blackhole) {
        smallDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage4.deserialize(bytes).serialize())
        }
    }
}

fun main() {
    run(ProtoktBenchmarks::class)
}
