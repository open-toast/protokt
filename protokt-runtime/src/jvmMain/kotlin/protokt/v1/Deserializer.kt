/*
 * Copyright (c) 2023 Toast, Inc.
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
import kotlinx.io.Source
import java.io.InputStream
import java.nio.ByteBuffer

@OptIn(OnlyForUseByGeneratedProtoCode::class)
actual interface Deserializer<T : Message> {
    actual fun deserialize(bytes: Bytes): T

    actual fun deserialize(bytes: ByteArray): T

    actual fun deserialize(bytes: BytesSlice): T

    actual fun deserialize(reader: Reader): T

    @Beta
    actual fun deserialize(source: Source): T

    fun deserialize(stream: InputStream): T =
        (codec as? JvmCodec)?.let { deserialize(it.reader(stream)) }
            ?: deserialize(stream.readBytes())

    fun deserialize(stream: CodedInputStream): T =
        deserialize(ProtobufJavaReader(stream))

    fun deserialize(buffer: ByteBuffer): T =
        (codec as? JvmCodec)?.let { deserialize(it.reader(buffer)) }
            ?: run {
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                deserialize(bytes)
            }
}
