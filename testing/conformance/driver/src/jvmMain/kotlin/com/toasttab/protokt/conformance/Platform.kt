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
import com.toasttab.protokt.conformance.ConformanceResponse.Result.RuntimeError
import com.toasttab.protokt.conformance.ConformanceResponse.Result.SerializeError
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtMessage
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal actual object Platform {
    actual fun <T : KtMessage> readMessageFromStdIn(
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T>? =
        try {
            val sizeBuf = ByteArray(4)
            if (System.`in`.read(sizeBuf) == 4) {
                val size = bytesToInt(sizeBuf)
                val requestBuf = ByteArray(size)
                val read = System.`in`.read(requestBuf)
                require(read == size) { "Expected to read $size bytes but read $read" }
                deserialize(Bytes(requestBuf), deserializer)
            } else {
                null
            }
        } catch (ex: Exception) {
            Failure(RuntimeError(ex.stackTraceToString()))
        }

    actual fun writeToStdOut(bytes: ByteArray) {
        System.out.write(intToBytes(bytes.size))
        System.out.write(bytes)
        System.out.flush()
    }

    actual fun <T : KtMessage> deserialize(
        bytes: Bytes,
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T> =
        try {
            Proceed(deserializer.deserialize(bytes))
        } catch (ex: Exception) {
            Failure(ParseError(ex.stackTraceToString()))
        }

    actual fun serialize(message: KtMessage): ConformanceStepResult<ByteArray> =
        try {
            Proceed(message.serialize())
        } catch (ex: Exception) {
            Failure(SerializeError(ex.stackTraceToString()))
        }
}

internal fun intToBytes(size: Int) =
    ByteArray(4).also {
        it[0] = size.toByte()
        it[1] = size.shr(8).toByte()
        it[2] = size.shr(16).toByte()
        it[3] = size.shr(24).toByte()
    }

internal fun bytesToInt(bytes: ByteArray) =
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).int
