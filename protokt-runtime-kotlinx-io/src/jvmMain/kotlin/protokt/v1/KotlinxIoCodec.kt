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

import kotlinx.io.Sink
import kotlinx.io.Source
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

actual object KotlinxIoCodec : StreamingCodec, JvmCodec {
    actual override fun writer(size: Int): Writer =
        KotlinxIoCodecImplementation.writer(size)

    actual override fun reader(bytes: ByteArray): Reader =
        KotlinxIoCodecImplementation.reader(bytes)

    actual override fun reader(bytes: ByteArray, offset: Int, length: Int): Reader =
        KotlinxIoCodecImplementation.reader(bytes, offset, length)

    actual override fun reader(source: Source): Reader =
        KotlinxIoCodecImplementation.reader(source)

    override fun reader(stream: InputStream): Reader =
        KotlinxIoCodecImplementation.reader(stream)

    override fun reader(buffer: ByteBuffer): Reader =
        KotlinxIoCodecImplementation.reader(buffer)

    actual override fun serialize(message: Message, sink: Sink) {
        KotlinxIoCodecImplementation.serialize(message, sink)
    }

    override fun serialize(message: Message, outputStream: OutputStream) {
        KotlinxIoCodecImplementation.serialize(message, outputStream)
    }
}

private object KotlinxIoCodecImplementation : AbstractKotlinxIoCodec(), JvmKotlinxIoStreaming
