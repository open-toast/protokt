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

import com.google.protobuf.CodedInputStream

@OptIn(OnlyForUseByGeneratedProtoCode::class)
actual abstract class AbstractDeserializer<T : Message> actual constructor() : Deserializer<T> {
    actual abstract override fun deserialize(reader: Reader): T

    actual final override fun deserialize(bytes: Bytes) =
        deserialize(bytes.value)

    actual final override fun deserialize(bytes: ByteArray) =
        deserialize(reader(CodedInputStream.newInstance(bytes), bytes))

    actual final override fun deserialize(bytes: BytesSlice) =
        deserialize(reader(CodedInputStream.newInstance(bytes.array, bytes.offset, bytes.length)))
}
