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

import protokt.v1.conformance.ConformanceRequest.Payload.JsonPayload
import protokt.v1.conformance.ConformanceRequest.Payload.ProtobufPayload
import protokt.v1.conformance.ConformanceResponse.Result
import protokt.v1.protobuf_test_messages.proto3.TestAllTypesProto3

fun main() = Platform.runBlockingMain {
    while (true) {
        val result =
            when (val request = Platform.readMessageFromStdIn(ConformanceRequest)) {
                null -> break
                is Stop -> request.result
                is Proceed ->
                    when (val result = processRequest(request.value)) {
                        is Stop -> result.result
                        is Proceed -> result.value
                    }
            }

        Platform.writeToStdOut(ConformanceResponse { this.result = result }.serialize())
    }
}

private suspend fun processRequest(request: ConformanceRequest): ConformanceStepResult<Result> {
    if (!isSupported(request)) {
        return ConformanceStepResult.skip()
    }

    return when (val payload = request.payload) {
        is ProtobufPayload -> Platform.deserializeProtobuf(payload.protobufPayload.bytes, TestAllTypesProto3)
        is JsonPayload -> Platform.deserializeJson(payload.jsonPayload, TestAllTypesProto3)
        else -> ConformanceStepResult.skip<TestAllTypesProto3>()
    }.flatMap {
        when (request.requestedOutputFormat) {
            WireFormat.PROTOBUF -> Platform.serializeProtobuf(it).map(Result::ProtobufPayload)
            WireFormat.JSON -> Platform.serializeJson(it).map(Result::JsonPayload)
            else -> ConformanceStepResult.skip()
        }
    }
}

private fun isSupported(request: ConformanceRequest) =
    request.messageType == "protobuf_test_messages.proto3.TestAllTypesProto3" &&
        // unclear why we have to filter this, but if we don't then JS and JVM impls throw on:
        //   Recommended.Proto3.ProtobufInput.GroupUnknownFields_Drop.TextFormatOutput
        //   Recommended.Proto3.ProtobufInput.GroupUnknownFields_Print.TextFormatOutput
        //   Recommended.Proto3.ProtobufInput.RepeatedUnknownFields_Drop.TextFormatOutput
        //   Recommended.Proto3.ProtobufInput.RepeatedUnknownFields_Print.TextFormatOutput
        request.requestedOutputFormat != WireFormat.TEXT_FORMAT

internal sealed class ConformanceStepResult<T> {
    suspend fun <R> map(action: suspend (T) -> R): ConformanceStepResult<R> =
        when (this) {
            is Proceed<T> -> Proceed(action(value))
            is Stop<T> -> Stop(result)
        }

    suspend fun <R> flatMap(action: suspend (T) -> ConformanceStepResult<R>): ConformanceStepResult<R> =
        when (this) {
            is Proceed<T> -> action(value)
            is Stop<T> -> Stop(result)
        }

    companion object {
        val SKIP = Result.Skipped("unsupported request")

        fun <T> skip() =
            Stop<T>(SKIP)
    }
}

internal class Proceed<T>(
    val value: T
) : ConformanceStepResult<T>()

internal class Stop<T>(
    val result: Result
) : ConformanceStepResult<T>()
