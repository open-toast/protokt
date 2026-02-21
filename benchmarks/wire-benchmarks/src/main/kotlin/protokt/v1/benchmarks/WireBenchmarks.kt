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
import com.toasttab.protokt.v1.benchmarks.StringOneofMessage
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.buffer
import okio.sink
import okio.source
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Random
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

    private lateinit var byteValues: Array<ByteString>

    private lateinit var stringHeavyPayloads: List<ByteString>
    private lateinit var stringOneofPayloads: List<ByteString>
    private lateinit var stringOneofVeryHeavyPayloads: List<ByteString>
    private lateinit var stringVeryHeavyPayloads: List<ByteString>

    private lateinit var largePayloadArrays: List<ByteArray>
    private lateinit var mediumPayloadArrays: List<ByteArray>
    private lateinit var smallPayloadArrays: List<ByteArray>

    @Setup
    fun setup() {
        byteValues = Array(1000) { i -> byteArrayOf(i.toByte()).toByteString() }

        val random = Random(42)
        stringHeavyPayloads = (0 until 100).map {
            GenericMessage1(
                field_string1 = randomUtf8String(random, 10_000),
                field_string2 = randomUtf8String(random, 10_000),
                field_string3000 = randomUtf8String(random, 10_000)
            )
        }.map { ByteString.of(*GenericMessage1.ADAPTER.encode(it)) }

        stringOneofPayloads = (0 until 100).map {
            StringOneofMessage(
                string_val1 = randomUtf8String(random, 10_000),
                string_val2 = randomUtf8String(random, 10_000),
                string_val3 = randomUtf8String(random, 10_000)
            )
        }.map { ByteString.of(*StringOneofMessage.ADAPTER.encode(it)) }

        stringOneofVeryHeavyPayloads = (0 until 10).map {
            StringOneofMessage(
                string_val1 = randomUtf8String(random, 1_000_000),
                string_val2 = randomUtf8String(random, 1_000_000),
                string_val3 = randomUtf8String(random, 1_000_000)
            )
        }.map { ByteString.of(*StringOneofMessage.ADAPTER.encode(it)) }

        stringVeryHeavyPayloads = (0 until 10).map {
            GenericMessage1(
                field_string1 = randomUtf8String(random, 1_000_000),
                field_string2 = randomUtf8String(random, 1_000_000),
                field_string3000 = randomUtf8String(random, 1_000_000)
            )
        }.map { ByteString.of(*GenericMessage1.ADAPTER.encode(it)) }

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

        largePayloadArrays = largeDataset.payload.map { it.toByteArray() }
        mediumPayloadArrays = mediumDataset.payload.map { it.toByteArray() }
        smallPayloadArrays = smallDataset.payload.map { it.toByteArray() }
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

    @Benchmark
    fun copyAppendListLarge(bh: Blackhole) {
        var msg = largeParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.copy(field_bytes1500 = msg.field_bytes1500 + byteValues[i])
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapLarge(bh: Blackhole) {
        var msg = largeParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.copy(field_map5000 = msg.field_map5000 + ("key$i" to i.toLong()))
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendListMedium(bh: Blackhole) {
        var msg = mediumParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.copy(field_bytes1500 = msg.field_bytes1500 + byteValues[i])
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapMedium(bh: Blackhole) {
        var msg = mediumParsedDataset.first()
        repeat(1000) { i ->
            msg = msg.copy(field_map5000 = msg.field_map5000 + ("key$i" to i.toLong()))
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendListSmall(bh: Blackhole) {
        var msg = mediumParsedDataset.first().copy(field_bytes1500 = emptyList())
        repeat(1000) { i ->
            msg = msg.copy(field_bytes1500 = msg.field_bytes1500 + byteValues[i])
        }
        bh.consume(msg)
    }

    @Benchmark
    fun copyAppendMapSmall(bh: Blackhole) {
        var msg = mediumParsedDataset.first().copy(field_map5000 = emptyMap())
        repeat(1000) { i ->
            msg = msg.copy(field_map5000 = msg.field_map5000 + ("key$i" to i.toLong()))
        }
        bh.consume(msg)
    }

    @Benchmark
    fun serializeLargeStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.sink().buffer()
        largeParsedDataset.forEach { msg ->
            GenericMessage1.ADAPTER.encode(sink, msg)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun serializeMediumStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.sink().buffer()
        mediumParsedDataset.forEach { msg ->
            GenericMessage1.ADAPTER.encode(sink, msg)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun serializeSmallStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.sink().buffer()
        smallParsedDataset.forEach { msg ->
            GenericMessage4.ADAPTER.encode(sink, msg)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun passThroughLargeFromMemory(bh: Blackhole) {
        largeDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.ADAPTER.encode(GenericMessage1.ADAPTER.decode(bytes)))
        }
    }

    @Benchmark
    fun passThroughMediumFromMemory(bh: Blackhole) {
        mediumDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.ADAPTER.encode(GenericMessage1.ADAPTER.decode(bytes)))
        }
    }

    @Benchmark
    fun passThroughSmallFromMemory(bh: Blackhole) {
        smallDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage4.ADAPTER.encode(GenericMessage4.ADAPTER.decode(bytes)))
        }
    }

    @Benchmark
    fun mutateAndSerializeStringHeavy(bh: Blackhole) {
        stringHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage1.ADAPTER.decode(bytes)
            val mutated = msg.copy(field_string1 = msg.field_string1 + "x", field_string2 = msg.field_string2 + "x", field_string3000 = msg.field_string3000 + "x")
            bh.consume(GenericMessage1.ADAPTER.encode(mutated))
        }
    }

    @Benchmark
    fun passThroughStringHeavy(bh: Blackhole) {
        stringHeavyPayloads.forEach { bytes ->
            bh.consume(GenericMessage1.ADAPTER.encode(GenericMessage1.ADAPTER.decode(bytes)))
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneof(bh: Blackhole) {
        stringOneofPayloads.forEach { bytes ->
            val msg = StringOneofMessage.ADAPTER.decode(bytes)
            val mutated = msg.copy(
                string_val1 = msg.string_val1!! + "x",
                string_val2 = msg.string_val2!! + "x",
                string_val3 = msg.string_val3!! + "x"
            )
            bh.consume(StringOneofMessage.ADAPTER.encode(mutated))
        }
    }

    @Benchmark
    fun passThroughStringOneof(bh: Blackhole) {
        stringOneofPayloads.forEach { bytes ->
            bh.consume(StringOneofMessage.ADAPTER.encode(StringOneofMessage.ADAPTER.decode(bytes)))
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneofVeryHeavy(bh: Blackhole) {
        stringOneofVeryHeavyPayloads.forEach { bytes ->
            val msg = StringOneofMessage.ADAPTER.decode(bytes)
            val mutated = msg.copy(
                string_val1 = msg.string_val1!! + "x",
                string_val2 = msg.string_val2!! + "x",
                string_val3 = msg.string_val3!! + "x"
            )
            bh.consume(StringOneofMessage.ADAPTER.encode(mutated))
        }
    }

    @Benchmark
    fun mutateAndSerializeStringVeryHeavy(bh: Blackhole) {
        stringVeryHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage1.ADAPTER.decode(bytes)
            val mutated = msg.copy(
                field_string1 = msg.field_string1 + "x",
                field_string2 = msg.field_string2 + "x",
                field_string3000 = msg.field_string3000 + "x"
            )
            bh.consume(GenericMessage1.ADAPTER.encode(mutated))
        }
    }

    @Benchmark
    fun deserializeLargeStreaming(bh: Blackhole) {
        largePayloadArrays.forEach { bytes ->
            bh.consume(GenericMessage1.ADAPTER.decode(ByteArrayInputStream(bytes).source().buffer()))
        }
    }

    @Benchmark
    fun deserializeMediumStreaming(bh: Blackhole) {
        mediumPayloadArrays.forEach { bytes ->
            bh.consume(GenericMessage1.ADAPTER.decode(ByteArrayInputStream(bytes).source().buffer()))
        }
    }

    @Benchmark
    fun deserializeSmallStreaming(bh: Blackhole) {
        smallPayloadArrays.forEach { bytes ->
            bh.consume(GenericMessage4.ADAPTER.decode(ByteArrayInputStream(bytes).source().buffer()))
        }
    }
}

fun main(args: Array<String>) {
    run(WireBenchmarks::class, args)
}
