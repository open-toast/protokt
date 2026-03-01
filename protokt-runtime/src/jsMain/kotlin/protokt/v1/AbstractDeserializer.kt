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

package protokt.v1

import kotlinx.io.Source

@OptIn(OnlyForUseByGeneratedProtoCode::class)
actual abstract class AbstractDeserializer<T : Message> actual constructor() : Deserializer<T> {
    actual abstract override fun deserialize(reader: Reader): T

    actual final override fun deserialize(bytes: Bytes) =
        deserialize(bytes.value)

    actual final override fun deserialize(bytes: ByteArray): T =
        deserialize(codec.reader(bytes))

    actual final override fun deserialize(bytes: BytesSlice): T =
        deserialize(codec.reader(bytes.array, bytes.offset, bytes.length))

    actual final override fun deserialize(source: Source): T {
        val c = codec
        check(c is StreamingCodec) { "Configured codec ${c::class.simpleName} does not support streaming deserialization" }
        return deserialize(c.reader(source))
    }
}
