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

import com.toasttab.protokt.conformance.ConformanceRequest.Payload.ProtobufPayload
import com.toasttab.protokt.conformance.ConformanceResponse.Result
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt_test_messages.proto3.TestAllTypesProto3

fun main() {
    var counter = 0
    while (true) {
        Platform.printErr("in loop: ${counter++}")
        val result =
            when (val request = nextRequest()) {
                null -> return
                is Failure -> request.failure
                is Proceed -> {
                    if (isSupported(request.value)) {
                        when (val payload = payload(request)) {
                            is Failure -> payload.failure
                            is Proceed -> {
                                when (val result = Platform.serialize(payload.value)) {
                                    is Failure -> result.failure
                                    is Proceed -> Result.ProtobufPayload(Bytes(result.value))
                                }
                            }
                        }
                    } else {
                        Result.Skipped("Only proto3 supported.")
                    }
                }
            }

        Platform.printErr("result: $result")
        Platform.writeToStdOut(conformanceResponse { this.result = result }.serialize())
    }
}

private fun nextRequest() =
    Platform.readMessageFromStdIn(ConformanceRequest)
        .also { Platform.printErr("next request: $it") }

private fun payload(request: Proceed<ConformanceRequest>) =
    Platform.deserialize(
        (request.value.payload as ProtobufPayload).protobufPayload,
        TestAllTypesProto3
    )

private fun isSupported(request: ConformanceRequest) =
    request.messageType == "protobuf_test_messages.proto3.TestAllTypesProto3" &&
        request.requestedOutputFormat == WireFormat.PROTOBUF &&
        request.payload is ProtobufPayload

internal sealed class ConformanceStepResult<T>

internal class Proceed<T>(
    val value: T
) : ConformanceStepResult<T>()

internal class Failure<T>(
    val failure: Result
) : ConformanceStepResult<T>()
