/*
 * Copyright (c) 2023 Toast, Inc.
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

package protokt.v1.grpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import protokt.v1.Beta
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Beta
object ClientCalls {
    suspend fun <ReqT, RespT> unaryRpc(
        client: Client,
        method: MethodDescriptor<ReqT, RespT>,
        request: ReqT
    ): RespT =
        suspendCoroutine { continuation ->
            val onResponse = { error: dynamic, resp: RespT ->
                if (error != null) {
                    continuation.resumeWithException(toStatusException(error))
                } else {
                    continuation.resume(resp)
                }
            }
            executeCall<Unit>(client, method, request, onResponse)
        }

    fun <ReqT, RespT> serverStreamingRpc(
        client: Client,
        method: MethodDescriptor<ReqT, RespT>,
        request: ReqT
    ): Flow<RespT> =
        callbackFlow {
            val call = executeCall<ClientReadableStream<RespT>>(client, method, request)
            call.on("data") { launch { send(it as RespT) } }
            call.on("error") { close(toStatusException(it)) }
            call.on("end") { close() }
            awaitClose()
        }

    suspend fun <ReqT, RespT> clientStreamingRpc(
        client: Client,
        method: MethodDescriptor<ReqT, RespT>,
        requests: Flow<ReqT>
    ): RespT {
        val context = currentCoroutineContext()
        return suspendCoroutine { continuation ->
            CoroutineScope(context).launch {
                val onResponse = { error: dynamic, resp: RespT ->
                    if (error != null) {
                        continuation.resumeWithException(toStatusException(error))
                    } else {
                        continuation.resume(resp)
                    }
                }
                val call = executeCall<ClientWritableStream<ReqT>>(client, method, null, onResponse)
                requests.collect { call.write(it, null) }
                call.end()
            }
        }
    }

    fun <ReqT, RespT> bidiStreamingRpc(
        client: Client,
        method: MethodDescriptor<ReqT, RespT>,
        requests: Flow<ReqT>
    ): Flow<RespT> =
        callbackFlow {
            val call = executeCall<ClientDuplexStream<ReqT, RespT>>(client, method)
            call.on("data") { launch { send(it as RespT) } }
            call.on("error") { close(toStatusException(it)) }
            call.on("end") { close() }

            launch {
                requests.collect { call.write(it, null) }
                call.end()
            }
            awaitClose()
        }

    private fun <T> executeCall(
        @Suppress("UNUSED_PARAMETER") client: Client,
        method: MethodDescriptor<*, *>,
        @Suppress("UNUSED_PARAMETER") request: Any? = null,
        @Suppress("UNUSED_PARAMETER") onResponse: Any? = null
    ): T {
        @Suppress("UNUSED_VARIABLE")
        val methodName = method.lowerBareMethodName

        return when (method.type) {
            MethodDescriptor.MethodType.UNARY ->
                js("client[methodName](request, onResponse)")

            MethodDescriptor.MethodType.SERVER_STREAMING ->
                js("client[methodName](request)")

            MethodDescriptor.MethodType.CLIENT_STREAMING ->
                js("client[methodName](onResponse)")

            MethodDescriptor.MethodType.BIDI_STREAMING ->
                js("client[methodName]()")

            MethodDescriptor.MethodType.UNKNOWN ->
                error("unsupported call type")
        } as T
    }
}

private fun toStatusException(error: dynamic): StatusException {
    val code = (error.code as? Int) ?: Status.Code.UNKNOWN.value
    val message = (error.details as? String) ?: (error.message as? String) ?: "Unknown error"
    val status = Status.fromCodeValue(code).withDescription(message)
    return StatusException(status)
}

fun newClient(
    service: ServiceDescriptor,
    @Suppress("UNUSED_PARAMETER") address: String,
    @Suppress("UNUSED_PARAMETER") credentials: ChannelCredentials
): Client {
    @Suppress("UNUSED_VARIABLE")
    val constructor = makeClientConstructor(service.toServiceDefinition())
    return js("new constructor(address, credentials)") as Client
}
