/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt.conformance

import com.toasttab.protokt.conformance.ConformanceResponse.Result.ParseError
import com.toasttab.protokt.conformance.ConformanceResponse.Result.SerializeError
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal actual object Platform {
    actual fun printErr(message: String) {
        process.stderr.write(message + "\n")
    }

    actual fun runBlockingMain(block: suspend CoroutineScope.() -> Unit) {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(block = block)
    }

    actual suspend fun <T : KtMessage> readMessageFromStdIn(
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T>? {
        val size = readSize() ?: return null
        return deserialize(Bytes(readBytes(size)), deserializer)
    }

    private suspend fun readBytes(size: Int) =
        readBuffer(size)?.asByteArray() ?: error("Failed to read $size bytes from stdin")

    private suspend fun readSize() =
        readBuffer(4)?.readInt32LE(0)

    private suspend fun readBuffer(size: Int): Buffer? =
        suspendCoroutine { continuation ->
            val buffer = Buffer.alloc(size)
            readSync(size)?.also {
                buffer.set(it, 0)
                if (it.length == size) {
                    continuation.resume(buffer)
                    return@suspendCoroutine
                }
            }
            process.stdin.once("readable") {
                continuation.resume(readSync(size))
            }
        }

    private fun readSync(size: Int): Buffer? {
        val buffer = Buffer.alloc(size)
        var total = 0
        while (total < size) {
            val chunk = process.stdin.read(size - total) ?: return null
            buffer.set(chunk, total)
            total += chunk.length
        }
        return buffer
    }

    actual fun writeToStdOut(bytes: ByteArray) {
        writeToStdOut(Buffer.alloc(4).also { it.writeInt32LE(bytes.size, 0) })
        writeToStdOut(bytes.asUint8Array())
    }

    private fun writeToStdOut(buf: Uint8Array) {
        var total = 0
        while (total < buf.length) {
            total += fs.writeSync(process.stdout.fd, buf, total, buf.length - total)
        }
    }

    actual fun <T : KtMessage> deserialize(
        bytes: Bytes,
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T> =
        try {
            Proceed(deserializer.deserialize(bytes))
        } catch (t: Throwable) {
            Failure(ParseError(t.stackTraceToString()))
        } catch (d: dynamic) {
            Failure(ParseError(d.toString()))
        }

    actual fun serialize(message: KtMessage): ConformanceStepResult<ByteArray> =
        try {
            Proceed(message.serialize())
        } catch (t: Throwable) {
            Failure(SerializeError(t.stackTraceToString()))
        } catch (d: dynamic) {
            Failure(SerializeError(d.toString()))
        }
}

@JsModule("process")
@JsNonModule
internal external object process {
    val stdin: StdStream
    val stdout: StdStream
    val stderr: StdStream
}

internal external interface StdStream {
    val fd: Int

    fun once(event: String, callback: () -> Unit)

    // Readable streams only
    fun read(size: Int = definedExternally): Buffer?

    // Writeable streams only
    fun write(chunk: String, encoding: String = definedExternally): Boolean
}

external class Buffer : Uint8Array {
    fun readInt32LE(offset: Int): Int
    fun writeInt32LE(value: Int, offset: Int): Int

    companion object {
        fun alloc(size: Int): Buffer
    }
}

@JsModule("fs")
@JsNonModule
external object fs {
    fun writeSync(
        fd: Int,
        buffer: ArrayBufferView,
        offset: Int,
        length: Int,
        position: Int? = definedExternally
    ): Int
}

private fun ByteArray.asUint8Array() =
    Uint8Array(
        unsafeCast<Int8Array>().buffer,
        unsafeCast<Int8Array>().byteOffset,
        unsafeCast<Int8Array>().length
    )

private fun Uint8Array.asByteArray() =
    Int8Array(buffer, byteOffset, length).unsafeCast<ByteArray>()