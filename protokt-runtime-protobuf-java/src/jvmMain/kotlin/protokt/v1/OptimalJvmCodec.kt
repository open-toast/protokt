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

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

// Combines ProtoktCodec for byte-array paths (direct array manipulation,
// no intermediate buffers) with ProtobufJavaCodec for streaming paths
// (InputStream/OutputStream/ByteBuffer via protobuf-java's CodedInputStream).
@OptIn(OnlyForUseByGeneratedProtoCode::class)
internal object OptimalJvmCodec : JvmCodec {
    override fun writer(size: Int): Writer =
        ProtoktCodec.writer(size)

    override fun reader(bytes: ByteArray): Reader =
        ProtoktCodec.reader(bytes)

    override fun reader(bytes: ByteArray, offset: Int, length: Int): Reader =
        ProtoktCodec.reader(bytes, offset, length)

    override fun serialize(message: Message, outputStream: OutputStream) =
        ProtobufJavaCodec.serialize(message, outputStream)

    override fun reader(stream: InputStream): Reader =
        ProtobufJavaCodec.reader(stream)

    override fun reader(buffer: ByteBuffer): Reader =
        ProtobufJavaCodec.reader(buffer)
}
