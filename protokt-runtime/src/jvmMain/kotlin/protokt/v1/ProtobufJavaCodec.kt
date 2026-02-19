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

import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

@OptIn(OnlyForUseByGeneratedProtoCode::class)
object ProtobufJavaCodec : JvmCodec {
    override fun writer(size: Int): Writer {
        val bytes = ByteArray(size)
        return ProtobufJavaWriter(CodedOutputStream.newInstance(bytes), bytes)
    }

    override fun reader(bytes: ByteArray): Reader =
        ProtobufJavaReader(CodedInputStream.newInstance(bytes), bytes)

    override fun reader(bytes: ByteArray, offset: Int, length: Int): Reader =
        ProtobufJavaReader(CodedInputStream.newInstance(bytes, offset, length))

    override fun serialize(message: Message, outputStream: OutputStream) {
        CodedOutputStream.newInstance(outputStream).run {
            message.serialize(ProtobufJavaWriter(this))
            flush()
        }
    }

    override fun reader(stream: InputStream): Reader =
        ProtobufJavaReader(CodedInputStream.newInstance(stream))

    override fun reader(buffer: ByteBuffer): Reader =
        ProtobufJavaReader(CodedInputStream.newInstance(buffer))
}
