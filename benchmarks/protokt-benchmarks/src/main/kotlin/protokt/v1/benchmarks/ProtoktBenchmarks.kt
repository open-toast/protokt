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

import kotlinx.io.asSink
import kotlinx.io.buffered
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
import java.io.ByteArrayOutputStream
import java.util.Random
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
open class ProtoktBenchmarks {
    @Param("protokt.v1.DefaultCollectionFactory", "protokt.v1.PersistentCollectionFactory")
    var collectionFactory: String = "protokt.v1.DefaultCollectionFactory"

    @Param("protokt.v1.ProtobufJavaCodec", "protokt.v1.ProtoktCodec")
    var codec: String = "protokt.v1.ProtobufJavaCodec"

    private lateinit var largeDataset: BenchmarkDataset
    private lateinit var largeParsedDataset: List<GenericMessage1>

    private lateinit var mediumDataset: BenchmarkDataset
    private lateinit var mediumParsedDataset: List<GenericMessage1>

    private lateinit var smallDataset: BenchmarkDataset
    private lateinit var smallParsedDataset: List<GenericMessage4>

    private lateinit var byteValues: Array<Bytes>

    private lateinit var stringHeavyPayloads: List<Bytes>
    private lateinit var stringOneofPayloads: List<Bytes>
    private lateinit var stringOneofVeryHeavyPayloads: List<Bytes>
    private lateinit var stringVeryHeavyPayloads: List<Bytes>

    // 20K chars: UTF-8 byte count ranges from 20,000 (all ASCII) to 60,000 (all 3-byte),
    // both requiring a 3-byte varint length prefix. This lets the writer skip the UTF-8
    // length measurement pass and encode directly (reserve-and-backtrack), unlike 10K chars
    // where the varint could be 2 or 3 bytes.
    private lateinit var stringOneof20kPayloads: List<Bytes>

    @Setup
    fun setup() {
        byteValues = Array(1000) { i -> Bytes.from(byteArrayOf(i.toByte())) }

        System.setProperty("protokt.collection.factory", collectionFactory)
        System.setProperty("protokt.codec", codec)

        val random = Random(42)
        stringHeavyPayloads = (0 until 100).map {
            GenericMessage1 {
                fieldString1 = randomUtf8String(random, 10_000)
                fieldString2 = randomUtf8String(random, 10_000)
                fieldString3000 = randomUtf8String(random, 10_000)
            }
        }.map { Bytes.from(it.serialize()) }

        stringOneofPayloads = (0 until 100).map {
            StringOneofMessage {
                content1 = StringOneofMessage.Content1.StringVal1(randomUtf8String(random, 10_000))
                content2 = StringOneofMessage.Content2.StringVal2(randomUtf8String(random, 10_000))
                content3 = StringOneofMessage.Content3.StringVal3(randomUtf8String(random, 10_000))
            }
        }.map { Bytes.from(it.serialize()) }

        stringOneof20kPayloads = (0 until 100).map {
            StringOneofMessage {
                content1 = StringOneofMessage.Content1.StringVal1(randomUtf8String(random, 20_000))
                content2 = StringOneofMessage.Content2.StringVal2(randomUtf8String(random, 20_000))
                content3 = StringOneofMessage.Content3.StringVal3(randomUtf8String(random, 20_000))
            }
        }.map { Bytes.from(it.serialize()) }

        stringOneofVeryHeavyPayloads = (0 until 10).map {
            StringOneofMessage {
                content1 = StringOneofMessage.Content1.StringVal1(randomUtf8String(random, 1_000_000))
                content2 = StringOneofMessage.Content2.StringVal2(randomUtf8String(random, 1_000_000))
                content3 = StringOneofMessage.Content3.StringVal3(randomUtf8String(random, 1_000_000))
            }
        }.map { Bytes.from(it.serialize()) }

        stringVeryHeavyPayloads = (0 until 10).map {
            GenericMessage1 {
                fieldString1 = randomUtf8String(random, 1_000_000)
                fieldString2 = randomUtf8String(random, 1_000_000)
                fieldString3000 = randomUtf8String(random, 1_000_000)
            }
        }.map { Bytes.from(it.serialize()) }

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
    fun serializeLargeStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.asSink().buffered()
        largeParsedDataset.forEach { msg ->
            msg.serialize(sink)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun serializeMediumStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.asSink().buffered()
        mediumParsedDataset.forEach { msg ->
            msg.serialize(sink)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun serializeSmallStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.asSink().buffered()
        smallParsedDataset.forEach { msg ->
            msg.serialize(sink)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
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

    @Benchmark
    fun mutateAndSerializeStringHeavy(bh: Blackhole) {
        stringHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage1.deserialize(bytes)
            val mutated = msg.copy {
                fieldString1 = msg.fieldString1 + "x"
                fieldString2 = msg.fieldString2 + "x"
                fieldString3000 = msg.fieldString3000 + "x"
            }
            bh.consume(mutated.serialize())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringHeavyStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.asSink().buffered()
        stringHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage1.deserialize(bytes)
            val mutated = msg.copy {
                fieldString1 = msg.fieldString1 + "x"
                fieldString2 = msg.fieldString2 + "x"
                fieldString3000 = msg.fieldString3000 + "x"
            }
            mutated.serialize(sink)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun passThroughStringHeavy(bh: Blackhole) {
        stringHeavyPayloads.forEach { bytes ->
            bh.consume(GenericMessage1.deserialize(bytes).serialize())
        }
    }

    @Benchmark
    fun deserializeStringHeavyStreaming(bh: Blackhole) {
        stringHeavyPayloads.forEach { bytes ->
            bh.consume(GenericMessage1.deserialize(bytes.inputStream()))
        }
    }

    @Benchmark
    fun deserializeStringOneofStreaming(bh: Blackhole) {
        stringOneofPayloads.forEach { bytes ->
            bh.consume(StringOneofMessage.deserialize(bytes.inputStream()))
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneof(bh: Blackhole) {
        stringOneofPayloads.forEach { bytes ->
            val msg = StringOneofMessage.deserialize(bytes)
            val mutated = msg.copy {
                content1 = StringOneofMessage.Content1.StringVal1(
                    (msg.content1 as StringOneofMessage.Content1.StringVal1).stringVal1 + "x"
                )
                content2 = StringOneofMessage.Content2.StringVal2(
                    (msg.content2 as StringOneofMessage.Content2.StringVal2).stringVal2 + "x"
                )
                content3 = StringOneofMessage.Content3.StringVal3(
                    (msg.content3 as StringOneofMessage.Content3.StringVal3).stringVal3 + "x"
                )
            }
            bh.consume(mutated.serialize())
        }
    }

    @Benchmark
    fun passThroughStringOneof(bh: Blackhole) {
        stringOneofPayloads.forEach { bytes ->
            bh.consume(StringOneofMessage.deserialize(bytes).serialize())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneofStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.asSink().buffered()
        stringOneofPayloads.forEach { bytes ->
            val msg = StringOneofMessage.deserialize(bytes)
            val mutated = msg.copy {
                content1 = StringOneofMessage.Content1.StringVal1(
                    (msg.content1 as StringOneofMessage.Content1.StringVal1).stringVal1 + "x"
                )
                content2 = StringOneofMessage.Content2.StringVal2(
                    (msg.content2 as StringOneofMessage.Content2.StringVal2).stringVal2 + "x"
                )
                content3 = StringOneofMessage.Content3.StringVal3(
                    (msg.content3 as StringOneofMessage.Content3.StringVal3).stringVal3 + "x"
                )
            }
            mutated.serialize(sink)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneof20k(bh: Blackhole) {
        stringOneof20kPayloads.forEach { bytes ->
            val msg = StringOneofMessage.deserialize(bytes)
            val mutated = msg.copy {
                content1 = StringOneofMessage.Content1.StringVal1(
                    (msg.content1 as StringOneofMessage.Content1.StringVal1).stringVal1 + "x"
                )
                content2 = StringOneofMessage.Content2.StringVal2(
                    (msg.content2 as StringOneofMessage.Content2.StringVal2).stringVal2 + "x"
                )
                content3 = StringOneofMessage.Content3.StringVal3(
                    (msg.content3 as StringOneofMessage.Content3.StringVal3).stringVal3 + "x"
                )
            }
            bh.consume(mutated.serialize())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneof20kStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        val sink = baos.asSink().buffered()
        stringOneof20kPayloads.forEach { bytes ->
            val msg = StringOneofMessage.deserialize(bytes)
            val mutated = msg.copy {
                content1 = StringOneofMessage.Content1.StringVal1(
                    (msg.content1 as StringOneofMessage.Content1.StringVal1).stringVal1 + "x"
                )
                content2 = StringOneofMessage.Content2.StringVal2(
                    (msg.content2 as StringOneofMessage.Content2.StringVal2).stringVal2 + "x"
                )
                content3 = StringOneofMessage.Content3.StringVal3(
                    (msg.content3 as StringOneofMessage.Content3.StringVal3).stringVal3 + "x"
                )
            }
            mutated.serialize(sink)
            sink.flush()
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneofVeryHeavy(bh: Blackhole) {
        stringOneofVeryHeavyPayloads.forEach { bytes ->
            val msg = StringOneofMessage.deserialize(bytes)
            val mutated = msg.copy {
                content1 = StringOneofMessage.Content1.StringVal1(
                    (msg.content1 as StringOneofMessage.Content1.StringVal1).stringVal1 + "x"
                )
                content2 = StringOneofMessage.Content2.StringVal2(
                    (msg.content2 as StringOneofMessage.Content2.StringVal2).stringVal2 + "x"
                )
                content3 = StringOneofMessage.Content3.StringVal3(
                    (msg.content3 as StringOneofMessage.Content3.StringVal3).stringVal3 + "x"
                )
            }
            bh.consume(mutated.serialize())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringVeryHeavy(bh: Blackhole) {
        stringVeryHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage1.deserialize(bytes)
            val mutated = msg.copy {
                fieldString1 = msg.fieldString1 + "x"
                fieldString2 = msg.fieldString2 + "x"
                fieldString3000 = msg.fieldString3000 + "x"
            }
            bh.consume(mutated.serialize())
        }
    }

    @Benchmark
    fun deserializeLargeStreaming(bh: Blackhole) {
        largeDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.deserialize(bytes.inputStream()))
        }
    }

    @Benchmark
    fun deserializeMediumStreaming(bh: Blackhole) {
        mediumDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage1.deserialize(bytes.inputStream()))
        }
    }

    @Benchmark
    fun deserializeSmallStreaming(bh: Blackhole) {
        smallDataset.payload.forEach { bytes ->
            bh.consume(GenericMessage4.deserialize(bytes.inputStream()))
        }
    }
}

fun main(args: Array<String>) {
    run(ProtoktBenchmarks::class, args)
}
