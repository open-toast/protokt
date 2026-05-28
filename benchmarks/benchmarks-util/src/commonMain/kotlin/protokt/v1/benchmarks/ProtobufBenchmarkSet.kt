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

interface ProtobufBenchmarkSet<T> {
    fun deserializeLargeFromMemory(bh: T)
    fun deserializeMediumFromMemory(bh: T)
    fun deserializeSmallFromMemory(bh: T)
    fun serializeLargeToMemory(bh: T)
    fun serializeMediumToMemory(bh: T)
    fun serializeSmallToMemory(bh: T)
    fun serializeLargeStreaming(bh: T)
    fun serializeMediumStreaming(bh: T)
    fun serializeSmallStreaming(bh: T)
    fun copyAppendListLarge(bh: T)
    fun copyAppendMapLarge(bh: T)
    fun copyAppendListMedium(bh: T)
    fun copyAppendMapMedium(bh: T)
    fun copyAppendListSmall(bh: T)
    fun copyAppendMapSmall(bh: T)
    fun passThroughLargeFromMemory(bh: T)
    fun passThroughMediumFromMemory(bh: T)
    fun passThroughSmallFromMemory(bh: T)
    fun mutateAndSerializeStringHeavy(bh: T)
    fun mutateAndSerializeStringHeavyStreaming(bh: T)
    fun passThroughStringHeavy(bh: T)
    fun mutateAndSerializeStringOneof(bh: T)
    fun passThroughStringOneof(bh: T)
    fun mutateAndSerializeStringOneofStreaming(bh: T)
    fun mutateAndSerializeStringOneof20k(bh: T)
    fun mutateAndSerializeStringOneof20kStreaming(bh: T)
    fun mutateAndSerializeStringOneofVeryHeavy(bh: T)
    fun mutateAndSerializeStringVeryHeavy(bh: T)
    fun passThroughStringRepeated(bh: T)
    fun passThroughStringMap(bh: T)
    fun deserializeStringRepeated(bh: T)
    fun deserializeStringMap(bh: T)
    fun copyAppendRepeatedString(bh: T)
    fun copyAppendMapStringString(bh: T)
}
