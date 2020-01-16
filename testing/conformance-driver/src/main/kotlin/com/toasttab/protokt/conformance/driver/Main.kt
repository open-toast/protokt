/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.conformance.driver

import arrow.core.Either
import arrow.core.None
import arrow.core.identity
import arrow.core.toOption
import arrow.fx.IO
import com.toasttab.protokt.rt.Bytes
import conformance.ConformanceRequest
import conformance.ConformanceResponse
import conformance.ConformanceResponse.Result
import conformance.WireFormat
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.runBlocking
import protobuf_test_messages.proto3.TestAllTypesProto3

const val proto3 = "protobuf_test_messages.proto3.TestAllTypesProto3"

fun main() {
    val stdin = System.`in`.buffered()
    val stdout = System.`out`.buffered()
    while (true) readSizeLE(stdin).fold(
        { return },
        {
            IO {
                ConformanceResponse(io(stdin, it)).serialize().also { bytes ->
                    stdout.write(int2BytesLE(bytes.size))
                    stdout.write(bytes)
                    stdout.flush()
                }
            }.unsafeRunSync()
        })
}

private fun io(stdin: InputStream, pbSize: Int) = pbSize.let {
    runBlocking {
        Either.catch {
            ByteArray(it).let { bytes ->
                require(stdin.read(bytes) == bytes.size)
                ConformanceRequest.deserialize(bytes)
            }
        }
    }.mapLeft { e ->
        Result.ParseError("ParseError, ${e.message}")
    }.fold(
        { identity(it) },
        {
            if (isRequestOk(it)) {
                (it.payload as ConformanceRequest.Payload.ProtobufPayload)
                .protobufPayload.value.let { bytes ->
                    runBlocking {
                        Either.catch {
                            Result.ProtobufPayload(
                                Bytes(
                                    TestAllTypesProto3
                                        .deserialize(bytes)
                                        .serialize()
                                )
                            )
                        }
                    }.mapLeft { e ->
                        Result.ParseError("Parse Error, ${e.message}") as Result
                    }.fold(
                        { f -> identity(f) },
                        { f -> identity(f) }
                    )
                }
            } else {
                Result.Skipped("Only proto3 supported.")
            }
        }
    )
}

private fun readSizeLE(ist: InputStream) = ist.let {
    IO {
        ByteArray(4).let {
            if (ist.read(it) == 4) bytes2IntLE(it).toOption()
            else None
        }
    }.unsafeRunSync()
}

private fun isRequestOk(request: ConformanceRequest) = request.let {
    it.messageType == proto3 &&
    it.requestedOutputFormat == WireFormat.PROTOBUF &&
    it.payload is ConformanceRequest.Payload.ProtobufPayload
}

private fun int2BytesLE(size: Int) = ByteArray(4).also {
    it[0] = size.toByte()
    it[1] = size.shr(8).toByte()
    it[2] = size.shr(16).toByte()
    it[3] = size.shr(24).toByte()
}

private fun bytes2IntLE(ba: ByteArray) = ba.let {
    ByteBuffer.wrap(it).order(ByteOrder.LITTLE_ENDIAN).int
}
