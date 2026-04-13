/*
 * Copyright (c) 2022 Toast, Inc.
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

import kotlinx.benchmark.Blackhole

interface ProtobufBenchmarkSet {
    fun deserializeLargeFromMemory(bh: Blackhole)
    fun deserializeMediumFromMemory(bh: Blackhole)
    fun deserializeSmallFromMemory(bh: Blackhole)
    fun serializeLargeToMemory(bh: Blackhole)
    fun serializeMediumToMemory(bh: Blackhole)
    fun serializeSmallToMemory(bh: Blackhole)
    fun serializeLargeStreaming(bh: Blackhole)
    fun serializeMediumStreaming(bh: Blackhole)
    fun serializeSmallStreaming(bh: Blackhole)
    fun copyAppendListLarge(bh: Blackhole)
    fun copyAppendMapLarge(bh: Blackhole)
    fun copyAppendListMedium(bh: Blackhole)
    fun copyAppendMapMedium(bh: Blackhole)
    fun copyAppendListSmall(bh: Blackhole)
    fun copyAppendMapSmall(bh: Blackhole)
    fun passThroughLargeFromMemory(bh: Blackhole)
    fun passThroughMediumFromMemory(bh: Blackhole)
    fun passThroughSmallFromMemory(bh: Blackhole)
    fun mutateAndSerializeStringHeavy(bh: Blackhole)
    fun mutateAndSerializeStringHeavyStreaming(bh: Blackhole)
    fun passThroughStringHeavy(bh: Blackhole)
    fun mutateAndSerializeStringOneof(bh: Blackhole)
    fun passThroughStringOneof(bh: Blackhole)
    fun mutateAndSerializeStringOneofStreaming(bh: Blackhole)
    fun mutateAndSerializeStringOneof20k(bh: Blackhole)
    fun mutateAndSerializeStringOneof20kStreaming(bh: Blackhole)
    fun mutateAndSerializeStringOneofVeryHeavy(bh: Blackhole)
    fun mutateAndSerializeStringVeryHeavy(bh: Blackhole)
    fun passThroughStringRepeated(bh: Blackhole)
    fun passThroughStringMap(bh: Blackhole)
    fun deserializeStringRepeated(bh: Blackhole)
    fun deserializeStringMap(bh: Blackhole)
    fun copyAppendRepeatedString(bh: Blackhole)
    fun copyAppendMapStringString(bh: Blackhole)
}
