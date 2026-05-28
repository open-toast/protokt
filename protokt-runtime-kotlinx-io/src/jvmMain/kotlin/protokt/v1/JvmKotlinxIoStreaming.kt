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

import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

@OptIn(OnlyForUseByGeneratedProtoCode::class)
internal interface JvmKotlinxIoStreaming :
    StreamingCodec,
    JvmCodec {

    override fun serialize(message: Message, outputStream: OutputStream) {
        val sink = outputStream.asSink().buffered()
        serialize(message, sink)
        sink.flush()
    }

    override fun reader(stream: InputStream): Reader =
        KotlinxIoSourceReader(stream.asSource().buffered())

    override fun reader(buffer: ByteBuffer): Reader =
        if (buffer.hasArray()) {
            reader(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())
        } else {
            reader(
                object : InputStream() {
                    override fun read(): Int =
                        if (buffer.hasRemaining()) {
                            buffer.get().toInt() and 0xff
                        } else {
                            -1
                        }

                    override fun read(b: ByteArray, off: Int, len: Int): Int {
                        if (!buffer.hasRemaining()) {
                            return -1
                        }
                        val n = minOf(len, buffer.remaining())
                        buffer.get(b, off, n)
                        return n
                    }
                }
            )
        }
}
