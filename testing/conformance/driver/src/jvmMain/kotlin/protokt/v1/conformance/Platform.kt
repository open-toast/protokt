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

package protokt.v1.conformance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import protokt.v1.Bytes
import protokt.v1.KtDeserializer
import protokt.v1.KtMessage
import protokt.v1.conformance.ConformanceRequest.Payload.ProtobufPayload
import protokt.v1.conformance.ConformanceResponse.Result.ParseError
import protokt.v1.conformance.ConformanceResponse.Result.RuntimeError
import protokt.v1.conformance.ConformanceResponse.Result.SerializeError
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal actual object Platform {
    actual fun printErr(message: String) {
        System.err.println(message)
    }

    actual fun runBlockingMain(block: suspend CoroutineScope.() -> Unit) {
        runBlocking(block = block)
    }

    actual suspend fun <T : KtMessage> readMessageFromStdIn(
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T>? =
        try {
            val sizeBuf = ByteArray(4)
            if (System.`in`.read(sizeBuf) == 4) {
                val size = bytesToInt(sizeBuf)
                val requestBuf = ByteArray(size)
                val read = System.`in`.read(requestBuf)
                require(read == size) { "Expected to read $size bytes but read $read" }
                deserializeProtobuf(requestBuf, deserializer)
            } else {
                null
            }
        } catch (t: Throwable) {
            Failure(RuntimeError(t.stackTraceToString()))
        }

    actual fun writeToStdOut(bytes: ByteArray) {
        System.out.write(intToBytes(bytes.size))
        System.out.write(bytes)
        System.out.flush()
    }

    actual fun isSupported(request: ConformanceRequest) =
        request.messageType == "protobuf_test_messages.proto3.TestAllTypesProto3" &&
            request.requestedOutputFormat == WireFormat.PROTOBUF &&
            request.payload is ProtobufPayload

    actual fun <T : KtMessage> deserializeProtobuf(
        bytes: ByteArray,
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T> =
        try {
            Proceed(deserializer.deserialize(bytes))
        } catch (t: Throwable) {
            Failure(ParseError(t.stackTraceToString()))
        }

    actual fun serializeProtobuf(message: KtMessage): ConformanceStepResult<Bytes> =
        try {
            Proceed(Bytes.from(message))
        } catch (t: Throwable) {
            Failure(SerializeError(t.stackTraceToString()))
        }

    actual fun <T : KtMessage> deserializeJson(
        json: String,
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T> =
        throw UnsupportedOperationException("unsupported payload format")

    actual fun serializeJson(message: KtMessage): ConformanceStepResult<String> =
        throw UnsupportedOperationException("unsupported output format")
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
