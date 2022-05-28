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
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

internal actual object Platform {
    actual fun <T : KtMessage> readMessageFromStdIn(
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T>? =
        Failure(ParseError("foo"))

    actual fun writeToStdOut(bytes: ByteArray) {
        val buf = bytes.asUint8Array()
        var total = 0
        while (total < buf.length) {
            total += Fs.writeSync(Process.stdout.fd, buf, total, buf.length - total)
        }
    }

    actual fun <T : KtMessage> deserialize(
        bytes: Bytes,
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T> =
        Failure(ParseError("foo"))

    actual fun serialize(message: KtMessage): ConformanceStepResult<ByteArray> =
        Failure(ParseError("foo"))
}

@JsModule("process")
@JsNonModule
internal external class Process {
    companion object {
        val stdin: StdStream
        val stdout: StdStream
        val stderr: StdStream
    }
}

internal external interface StdStream {
    val fd: Int

    fun once(event: String, callback: () -> Unit)

    // Readable streams only
    fun read(size: Int = definedExternally): Buffer?

    // Writeable streams only
    fun write(chunk: String, encoding: String = definedExternally): Boolean
    fun write(chunk: Uint8Array): Boolean
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
external class Fs {
    companion object {
        fun writeSync(
            fd: Int,
            buffer: ArrayBufferView,
            offset: Int,
            length: Int,
            position: Int? = definedExternally
        ): Int
    }
}

private fun ByteArray.asUint8Array() =
    Uint8Array(
        unsafeCast<Int8Array>().buffer,
        unsafeCast<Int8Array>().byteOffset,
        unsafeCast<Int8Array>().length
    )
