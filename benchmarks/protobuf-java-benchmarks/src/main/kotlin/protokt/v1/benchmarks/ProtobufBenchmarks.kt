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

import com.google.protobuf.ByteString
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Random
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

    private lateinit var byteValues: Array<ByteString>

    private lateinit var stringHeavyPayloads: List<ByteString>
    private lateinit var stringOneofPayloads: List<ByteString>
    private lateinit var stringOneofVeryHeavyPayloads: List<ByteString>
    private lateinit var stringVeryHeavyPayloads: List<ByteString>
    private lateinit var stringOneof20kPayloads: List<ByteString>

    private lateinit var largePayloadArrays: List<ByteArray>
    private lateinit var mediumPayloadArrays: List<ByteArray>
    private lateinit var smallPayloadArrays: List<ByteArray>

    @Setup
    fun setup() {
        byteValues = Array(1000) { i -> UnsafeByteOperations.unsafeWrap(byteArrayOf(i.toByte())) }

        val random = Random(42)
        stringHeavyPayloads = (0 until 100).map {
            GenericMessage.GenericMessage1.newBuilder()
                .setFieldString1(randomUtf8String(random, 10_000))
                .setFieldString2(randomUtf8String(random, 10_000))
                .setFieldString3000(randomUtf8String(random, 10_000))
                .build()
        }.map { UnsafeByteOperations.unsafeWrap(it.toByteArray()) }

        stringOneofPayloads = (0 until 100).map {
            GenericMessage.StringOneofMessage.newBuilder()
                .setStringVal1(randomUtf8String(random, 10_000))
                .setStringVal2(randomUtf8String(random, 10_000))
                .setStringVal3(randomUtf8String(random, 10_000))
                .build()
        }.map { UnsafeByteOperations.unsafeWrap(it.toByteArray()) }

        stringOneof20kPayloads = (0 until 100).map {
            GenericMessage.StringOneofMessage.newBuilder()
                .setStringVal1(randomUtf8String(random, 20_000))
                .setStringVal2(randomUtf8String(random, 20_000))
                .setStringVal3(randomUtf8String(random, 20_000))
                .build()
        }.map { UnsafeByteOperations.unsafeWrap(it.toByteArray()) }

        stringOneofVeryHeavyPayloads = (0 until 10).map {
            GenericMessage.StringOneofMessage.newBuilder()
                .setStringVal1(randomUtf8String(random, 1_000_000))
                .setStringVal2(randomUtf8String(random, 1_000_000))
                .setStringVal3(randomUtf8String(random, 1_000_000))
                .build()
        }.map { UnsafeByteOperations.unsafeWrap(it.toByteArray()) }

        stringVeryHeavyPayloads = (0 until 10).map {
            GenericMessage.GenericMessage1.newBuilder()
                .setFieldString1(randomUtf8String(random, 1_000_000))
                .setFieldString2(randomUtf8String(random, 1_000_000))
                .setFieldString3000(randomUtf8String(random, 1_000_000))
                .build()
        }.map { UnsafeByteOperations.unsafeWrap(it.toByteArray()) }

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

        largePayloadArrays = largeDataset.payloadList.map { it.toByteArray() }
        mediumPayloadArrays = mediumDataset.payloadList.map { it.toByteArray() }
        smallPayloadArrays = smallDataset.payloadList.map { it.toByteArray() }
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

    @Benchmark
    fun serializeLargeStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        largeParsedDataset.forEach { msg ->
            msg.writeTo(baos)
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun serializeMediumStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        mediumParsedDataset.forEach { msg ->
            msg.writeTo(baos)
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun serializeSmallStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        smallParsedDataset.forEach { msg ->
            msg.writeTo(baos)
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun passThroughLargeFromMemory(bh: Blackhole) {
        largeDataset.payloadList.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage1.parseFrom(bytes).toByteArray())
        }
    }

    @Benchmark
    fun passThroughMediumFromMemory(bh: Blackhole) {
        mediumDataset.payloadList.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage1.parseFrom(bytes).toByteArray())
        }
    }

    @Benchmark
    fun passThroughSmallFromMemory(bh: Blackhole) {
        smallDataset.payloadList.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage4.parseFrom(bytes).toByteArray())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringHeavy(bh: Blackhole) {
        stringHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage.GenericMessage1.parseFrom(bytes)
            val mutated = msg.toBuilder().setFieldString1(msg.fieldString1 + "x").setFieldString2(msg.fieldString2 + "x").setFieldString3000(msg.fieldString3000 + "x").build()
            bh.consume(mutated.toByteArray())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringHeavyStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        stringHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage.GenericMessage1.parseFrom(bytes)
            val mutated = msg.toBuilder().setFieldString1(msg.fieldString1 + "x").setFieldString2(msg.fieldString2 + "x").setFieldString3000(msg.fieldString3000 + "x").build()
            mutated.writeTo(baos)
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun passThroughStringHeavy(bh: Blackhole) {
        stringHeavyPayloads.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage1.parseFrom(bytes).toByteArray())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneof(bh: Blackhole) {
        stringOneofPayloads.forEach { bytes ->
            val msg = GenericMessage.StringOneofMessage.parseFrom(bytes)
            val mutated = msg.toBuilder()
                .setStringVal1(msg.stringVal1 + "x")
                .setStringVal2(msg.stringVal2 + "x")
                .setStringVal3(msg.stringVal3 + "x")
                .build()
            bh.consume(mutated.toByteArray())
        }
    }

    @Benchmark
    fun passThroughStringOneof(bh: Blackhole) {
        stringOneofPayloads.forEach { bytes ->
            bh.consume(GenericMessage.StringOneofMessage.parseFrom(bytes).toByteArray())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneofStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        stringOneofPayloads.forEach { bytes ->
            val msg = GenericMessage.StringOneofMessage.parseFrom(bytes)
            val mutated = msg.toBuilder()
                .setStringVal1(msg.stringVal1 + "x")
                .setStringVal2(msg.stringVal2 + "x")
                .setStringVal3(msg.stringVal3 + "x")
                .build()
            mutated.writeTo(baos)
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneof20k(bh: Blackhole) {
        stringOneof20kPayloads.forEach { bytes ->
            val msg = GenericMessage.StringOneofMessage.parseFrom(bytes)
            val mutated = msg.toBuilder()
                .setStringVal1(msg.stringVal1 + "x")
                .setStringVal2(msg.stringVal2 + "x")
                .setStringVal3(msg.stringVal3 + "x")
                .build()
            bh.consume(mutated.toByteArray())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneof20kStreaming(bh: Blackhole) {
        val baos = ByteArrayOutputStream()
        stringOneof20kPayloads.forEach { bytes ->
            val msg = GenericMessage.StringOneofMessage.parseFrom(bytes)
            val mutated = msg.toBuilder()
                .setStringVal1(msg.stringVal1 + "x")
                .setStringVal2(msg.stringVal2 + "x")
                .setStringVal3(msg.stringVal3 + "x")
                .build()
            mutated.writeTo(baos)
            bh.consume(baos.size())
            baos.reset()
        }
    }

    @Benchmark
    fun mutateAndSerializeStringOneofVeryHeavy(bh: Blackhole) {
        stringOneofVeryHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage.StringOneofMessage.parseFrom(bytes)
            val mutated = msg.toBuilder()
                .setStringVal1(msg.stringVal1 + "x")
                .setStringVal2(msg.stringVal2 + "x")
                .setStringVal3(msg.stringVal3 + "x")
                .build()
            bh.consume(mutated.toByteArray())
        }
    }

    @Benchmark
    fun mutateAndSerializeStringVeryHeavy(bh: Blackhole) {
        stringVeryHeavyPayloads.forEach { bytes ->
            val msg = GenericMessage.GenericMessage1.parseFrom(bytes)
            val mutated = msg.toBuilder()
                .setFieldString1(msg.fieldString1 + "x")
                .setFieldString2(msg.fieldString2 + "x")
                .setFieldString3000(msg.fieldString3000 + "x")
                .build()
            bh.consume(mutated.toByteArray())
        }
    }

    @Benchmark
    fun deserializeLargeStreaming(bh: Blackhole) {
        largePayloadArrays.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage1.parseFrom(ByteArrayInputStream(bytes)))
        }
    }

    @Benchmark
    fun deserializeMediumStreaming(bh: Blackhole) {
        mediumPayloadArrays.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage1.parseFrom(ByteArrayInputStream(bytes)))
        }
    }

    @Benchmark
    fun deserializeSmallStreaming(bh: Blackhole) {
        smallPayloadArrays.forEach { bytes ->
            bh.consume(GenericMessage.GenericMessage4.parseFrom(ByteArrayInputStream(bytes)))
        }
    }
}

fun main(args: Array<String>) {
    run(ProtobufBenchmarks::class, args)
}
