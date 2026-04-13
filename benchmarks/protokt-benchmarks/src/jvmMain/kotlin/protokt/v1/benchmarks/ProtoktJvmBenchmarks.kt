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

package protokt.v1.benchmarks

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import protokt.v1.Bytes
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
class ProtoktJvmBenchmarks {
    @Param("protokt.v1.DefaultCollectionFactory", "protokt.v1.PersistentCollectionFactory")
    var collectionFactory: String = "protokt.v1.DefaultCollectionFactory"

    @Param("protokt.v1.ProtobufJavaCodec", "protokt.v1.KotlinxIoCodec", "protokt.v1.ProtoktCodec", "protokt.v1.OptimalKmpCodec", "protokt.v1.OptimalJvmCodec")
    var codec: String = "protokt.v1.ProtobufJavaCodec"

    private lateinit var largeDataset: BenchmarkDataset
    private lateinit var mediumDataset: BenchmarkDataset
    private lateinit var smallDataset: BenchmarkDataset
    private lateinit var stringHeavyPayloads: List<Bytes>
    private lateinit var stringOneofPayloads: List<Bytes>

    @Setup
    fun setup() {
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

        largeDataset = BenchmarkDataset.deserialize(readDatasetBytes("large"))
        mediumDataset = BenchmarkDataset.deserialize(readDatasetBytes("medium"))
        smallDataset = BenchmarkDataset.deserialize(readDatasetBytes("small"))
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
}
