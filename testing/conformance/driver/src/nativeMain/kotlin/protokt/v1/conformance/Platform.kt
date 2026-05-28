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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package protokt.v1.conformance

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import platform.posix.STDERR_FILENO
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.fflush
import platform.posix.getenv
import platform.posix.read
import platform.posix.stdout
import platform.posix.write
import protokt.v1.Bytes
import protokt.v1.Deserializer
import protokt.v1.Message
import protokt.v1.conformance.ConformanceResponse.Result.ParseError
import protokt.v1.conformance.ConformanceResponse.Result.RuntimeError
import protokt.v1.conformance.ConformanceResponse.Result.SerializeError
import protokt.v1.deserialize
import protokt.v1.serialize

internal actual object Platform {
    actual val streaming = getenv("PROTOKT_STREAMING")?.toKString()?.toBoolean() ?: false

    actual fun printErr(message: String) {
        val bytes = (message + "\n").encodeToByteArray()
        bytes.usePinned { pinned ->
            write(STDERR_FILENO, pinned.addressOf(0), bytes.size.convert())
        }
    }

    actual fun className(obj: Any): String =
        obj::class.qualifiedName ?: obj::class.toString()

    actual fun runBlockingMain(block: suspend CoroutineScope.() -> Unit) =
        runBlocking(block = block)

    actual suspend fun <T : Message> readMessageFromStdIn(
        deserializer: Deserializer<T>
    ): ConformanceStepResult<T>? =
        try {
            val sizeBuf = ByteArray(4)
            if (readExact(STDIN_FILENO, sizeBuf, 4) == 4) {
                val size = bytesToInt(sizeBuf)
                val requestBuf = ByteArray(size)
                val bytesRead = readExact(STDIN_FILENO, requestBuf, size)
                require(bytesRead == size) { "Expected to read $size bytes but read $bytesRead" }
                deserializeProtobuf(requestBuf, deserializer)
            } else {
                null
            }
        } catch (t: Throwable) {
            Stop(RuntimeError(t.stackTraceToString()))
        }

    actual fun writeToStdOut(bytes: ByteArray) {
        val sizeBytes = intToBytes(bytes.size)
        sizeBytes.usePinned { pinned ->
            write(STDOUT_FILENO, pinned.addressOf(0), sizeBytes.size.convert())
        }
        bytes.usePinned { pinned ->
            write(STDOUT_FILENO, pinned.addressOf(0), bytes.size.convert())
        }
        fflush(stdout)
    }

    actual fun <T : Message> deserializeProtobuf(
        bytes: ByteArray,
        deserializer: Deserializer<T>
    ): ConformanceStepResult<T> =
        try {
            if (streaming) {
                val source = Buffer()
                source.write(bytes)
                Proceed(deserializer.deserialize(source))
            } else {
                Proceed(deserializer.deserialize(bytes))
            }
        } catch (t: Throwable) {
            Stop(ParseError(t.stackTraceToString()))
        }

    actual fun serializeProtobuf(message: Message): ConformanceStepResult<Bytes> =
        try {
            if (streaming) {
                val buffer = Buffer()
                message.serialize(buffer)
                Proceed(Bytes.from(buffer.readByteArray()))
            } else {
                Proceed(Bytes.from(message))
            }
        } catch (t: Throwable) {
            Stop(SerializeError(t.stackTraceToString()))
        }
}

private fun readExact(fd: Int, buf: ByteArray, count: Int): Int {
    var totalRead = 0
    buf.usePinned { pinned ->
        while (totalRead < count) {
            val n = read(fd, pinned.addressOf(totalRead), (count - totalRead).convert())
            if (n <= 0) break
            totalRead += n.convert<Int>()
        }
    }
    return totalRead
}

private fun intToBytes(size: Int) =
    ByteArray(4).also {
        it[0] = size.toByte()
        it[1] = size.shr(8).toByte()
        it[2] = size.shr(16).toByte()
        it[3] = size.shr(24).toByte()
    }

private fun bytesToInt(bytes: ByteArray) =
    (bytes[0].toInt() and 0xFF) or
        ((bytes[1].toInt() and 0xFF) shl 8) or
        ((bytes[2].toInt() and 0xFF) shl 16) or
        ((bytes[3].toInt() and 0xFF) shl 24)
