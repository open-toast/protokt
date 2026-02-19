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

import protokt.v1.Collections
import protokt.v1.OnlyForUseByGeneratedProtoCode
import protokt.v1.codec
import protokt.v1.conformance.ConformanceRequest.Payload.ProtobufPayload
import protokt.v1.conformance.ConformanceResponse.Result
import protokt.v1.protobuf_test_messages.proto3.TestAllTypesProto3

@OptIn(OnlyForUseByGeneratedProtoCode::class)
fun main() =
    Platform.runBlockingMain {
        val builderResult = Collections.listBuilder<Any>().build()
        Platform.printErr("protoktPersistentCollectionType=${Platform.className(builderResult)}")
        Platform.printErr("protoktCodec=${Platform.className(codec)}")
        while (true) {
            val request = Platform.readMessageFromStdIn(ConformanceRequest) ?: break

            Platform.writeToStdOut(
                ConformanceResponse {
                    result =
                        when (val result = request.flatMap(::processRequest)) {
                            is Stop -> result.result
                            is Proceed -> result.value
                        }
                }.serialize()
            )
        }
    }

private fun processRequest(request: ConformanceRequest): ConformanceStepResult<Result> {
    val skipReason = skipReason(request)
    if (skipReason != null) {
        // Platform.printErr("Received unsupported request for message type ${request.messageType}: $skipReason")
        return ConformanceStepResult.skip()
    }

    return when (val payload = request.payload) {
        is ProtobufPayload -> Platform.deserializeProtobuf(payload.protobufPayload.bytes, TestAllTypesProto3)
        else -> ConformanceStepResult.skip<TestAllTypesProto3>()
    }.flatMap {
        when (request.requestedOutputFormat) {
            WireFormat.PROTOBUF -> Platform.serializeProtobuf(it).map(Result::ProtobufPayload)
            else -> ConformanceStepResult.skip()
        }
    }
}

private val supportedMessageTypes =
    setOf(
        "protobuf_test_messages.proto3.TestAllTypesProto3",
        "protobuf_test_messages.editions.proto3.TestAllTypesProto3",
        "protobuf_test_messages.editions.TestAllTypesEdition2023"
    )

private fun skipReason(request: ConformanceRequest): SkipReason? =
    when {
        request.messageType !in supportedMessageTypes ->
            SkipReason.UNSUPPORTED_MESSAGE_TYPE
        request.requestedOutputFormat != WireFormat.PROTOBUF ->
            SkipReason.UNSUPPORTED_OUTPUT_FORMAT
        else ->
            null
    }

private enum class SkipReason {
    UNSUPPORTED_MESSAGE_TYPE,
    UNSUPPORTED_OUTPUT_FORMAT
}

internal sealed class ConformanceStepResult<T> {
    fun <R> map(action: (T) -> R) =
        when (this) {
            is Proceed<T> -> Proceed(action(value))
            is Stop<T> -> Stop(result)
        }

    fun <R> flatMap(action: (T) -> ConformanceStepResult<R>) =
        when (this) {
            is Proceed<T> -> action(value)
            is Stop<T> -> Stop(result)
        }

    companion object {
        fun <T> skip() =
            Stop<T>(Result.Skipped("unsupported request"))
    }
}

internal class Proceed<T>(
    val value: T
) : ConformanceStepResult<T>()

internal class Stop<T>(
    val result: Result
) : ConformanceStepResult<T>()
