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

import com.google.protobuf.UnsafeByteOperations
import com.google.protobuf.benchmarks.Benchmarks
import com.toasttab.protokt.v1.benchmarks.GenericMessage
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
open class ProtobufBenchmarks {
    private lateinit var largeDataset: Benchmarks.BenchmarkDataset
    private lateinit var largeParsedDataset: List<GenericMessage.GenericMessage1>
    private lateinit var mediumDataset: Benchmarks.BenchmarkDataset
    private lateinit var mediumParsedDataset: List<GenericMessage.GenericMessage1>
    private lateinit var smallDataset: Benchmarks.BenchmarkDataset
    private lateinit var smallParsedDataset: List<GenericMessage.GenericMessage4>

    private lateinit var byteValues: Array<com.google.protobuf.ByteString>

    @Setup
    fun setup() {
        byteValues = Array(1000) { i -> UnsafeByteOperations.unsafeWrap(byteArrayOf(i.toByte())) }
        readData("large").use { stream ->
            largeDataset = Benchmarks.BenchmarkDataset.parseFrom(stream)
        }
        largeParsedDataset = largeDataset.payloadList.map { GenericMessage.GenericMessage1.parseFrom(it) }
        readData("medium").use { stream ->
            mediumDataset = Benchmarks.BenchmarkDataset.parseFrom(stream)
        }
        mediumParsedDataset = mediumDataset.payloadList.map { GenericMessage.GenericMessage1.parseFrom(it) }
        readData("small").use { stream ->
            smallDataset = Benchmarks.BenchmarkDataset.parseFrom(stream)
        }
        smallParsedDataset = smallDataset.payloadList.map { GenericMessage.GenericMessage4.parseFrom(it) }
    }

    @Benchmark
    fun deserializeLargeFromMemory(bh: Blackhole) {
        largeDataset.payloadList.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage1.parseFrom(bytes))
        }
    }

    @Benchmark
    fun deserializeMediumFromMemory(bh: Blackhole) {
        mediumDataset.payloadList.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage1.parseFrom(bytes))
        }
    }

    @Benchmark
    fun deserializeSmallFromMemory(bh: Blackhole) {
        smallDataset.payloadList.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage4.parseFrom(bytes))
        }
    }

    @Benchmark
    fun serializeLargeToMemory(bh: Blackhole) {
        largeParsedDataset.forEach { msg -> bh.consume(msg.toByteArray()) }
    }

    @Benchmark
    fun serializeMediumToMemory(bh: Blackhole) {
        mediumParsedDataset.forEach { msg -> bh.consume(msg.toByteArray()) }
    }

    @Benchmark
    fun serializeSmallToMemory(bh: Blackhole) {
        smallParsedDataset.forEach { msg -> bh.consume(msg.toByteArray()) }
    }

    @Benchmark
    fun copyAppendListLarge(bh: Blackhole) {
        var msg = largeParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.toBuilder().addFieldBytes1500(byteValues[i]).build()
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapLarge(bh: Blackhole) {
        var msg = largeParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.toBuilder().putFieldMap5000("key$i", i.toLong()).build()
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendListMedium(bh: Blackhole) {
        var msg = mediumParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.toBuilder().addFieldBytes1500(byteValues[i]).build()
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapMedium(bh: Blackhole) {
        var msg = mediumParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.toBuilder().putFieldMap5000("key$i", i.toLong()).build()
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendListSmall(bh: Blackhole) {
        var msg = mediumParsedDataset.first().toBuilder().clearFieldBytes1500().build()
        repeat(1000) { i ->
            msg = msg.toBuilder().addFieldBytes1500(byteValues[i]).build()
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapSmall(bh: Blackhole) {
        var msg = mediumParsedDataset.first().toBuilder().clearFieldMap5000().build()
        repeat(1000) { i ->
            msg = msg.toBuilder().putFieldMap5000("key$i", i.toLong()).build()
        }
        bh.consume(msg)
    }
}

fun main() {
    run(ProtobufBenchmarks::class)
}
