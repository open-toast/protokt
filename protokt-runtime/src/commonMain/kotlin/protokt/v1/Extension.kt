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

package protokt.v1

@Beta
class Extension<in E : Message, out T>(
    val number: UInt,
    internal val codec: ExtensionCodec<@UnsafeVariance T>
)

@Beta
class RepeatedExtension<in E : Message, out T>(
    val number: UInt,
    internal val codec: ExtensionCodec<@UnsafeVariance T>
)

@Beta
operator fun <E : Message, T> E.get(ext: Extension<E, T>): T? {
    val field = unknownFields[ext.number] ?: return null
    return ext.codec.decodeSingular(field)
}

@Beta
operator fun <E : Message, T> E.get(ext: RepeatedExtension<E, T>): List<T> {
    val field = unknownFields[ext.number] ?: return emptyList()
    return ext.codec.decodeRepeated(field)
}
