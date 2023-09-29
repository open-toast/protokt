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

import protokt.v1.conformance.ConformanceRequest.Payload.ProtobufPayload
import protokt.v1.conformance.ConformanceResponse.Result
import protokt.v1.protobuf_test_messages.proto3.TestAllTypesProto3

fun main() = Platform.runBlockingMain {
    while (true) {
        val result =
            when (val request = nextRequest()) {
                null -> break
                is Failure -> request.failure
                is Proceed -> {
                    if (isSupported(request.value)) {
                        when (val payload = payload(request)) {
                            is Failure -> payload.failure
                            is Proceed -> {
                                when (val result = Platform.serialize(payload.value)) {
                                    is Failure -> result.failure
                                    is Proceed -> Result.ProtobufPayload(result.value)
                                }
                            }
                        }
                    } else {
                        Result.Skipped("Only proto3 supported.")
                    }
                }
            }

        Platform.writeToStdOut(ConformanceResponse { this.result = result }.serialize())
    }
}

private suspend fun nextRequest() =
    Platform.readMessageFromStdIn(ConformanceRequest)

private fun payload(request: Proceed<ConformanceRequest>) =
    Platform.deserialize(
        (request.value.payload as ProtobufPayload).protobufPayload.bytes,
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
