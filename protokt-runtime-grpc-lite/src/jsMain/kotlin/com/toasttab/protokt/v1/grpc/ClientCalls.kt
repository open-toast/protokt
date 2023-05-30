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

package com.toasttab.protokt.v1.grpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ClientCalls {
    suspend fun <ReqT, RespT> unaryRpc(
        @Suppress("UNUSED_PARAMETER") client: dynamic,
        method: MethodDescriptor<ReqT, RespT>,
        @Suppress("UNUSED_PARAMETER") request: ReqT
    ): RespT {
        return suspendCoroutine { continuation ->
            @Suppress("UNUSED_VARIABLE")
            val onResponse = { _: dynamic, resp: RespT ->
                continuation.resume(resp)
            }

            @Suppress("UNUSED_VARIABLE")
            val methodName = method.lowerBareMethodName
            js("client[methodName](request, onResponse)")
            Unit
        }
    }

    fun <ReqT, RespT> serverStreamingRpc(
        @Suppress("UNUSED_PARAMETER") client: dynamic,
        method: MethodDescriptor<ReqT, RespT>,
        @Suppress("UNUSED_PARAMETER") request: ReqT
    ): Flow<RespT> {
        @Suppress("UNUSED_VARIABLE")
        val methodName = method.lowerBareMethodName

        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        val call = js("client[methodName](request)") as ClientReadableStream<RespT>

        return callbackFlow {
            call.on("data") {
                launch { send(it as RespT) }
            }
            call.on("end") {
                close()
            }
            awaitClose()
        }
    }

    suspend fun <ReqT, RespT> clientStreamingRpc(
        @Suppress("UNUSED_PARAMETER") client: dynamic,
        method: MethodDescriptor<ReqT, RespT>,
        requests: Flow<ReqT>
    ): RespT {
        val context = currentCoroutineContext()
        return suspendCoroutine { continuation ->
            @Suppress("UNUSED_VARIABLE")
            val onResponse = { _: dynamic, resp: RespT ->
                continuation.resume(resp)
            }

            @Suppress("UNUSED_VARIABLE")
            val methodName = method.lowerBareMethodName

            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            val call = js("client[methodName](onResponse)") as ClientWritableStream<ReqT>

            CoroutineScope(context).launch {
                requests.collect { call.write(it, null) }
            }
        }
    }

    fun <ReqT, RespT> bidiStreamingRpc(
        @Suppress("UNUSED_PARAMETER") client: dynamic,
        method: MethodDescriptor<ReqT, RespT>,
        requests: Flow<ReqT>
    ): Flow<RespT> {
        @Suppress("UNUSED_VARIABLE")
        val methodName = method.lowerBareMethodName

        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        val call = js("client[methodName]()") as ClientDuplexStream<ReqT, RespT>

        return callbackFlow {
            call.on("data") {
                launch { send(it as RespT) }
            }
            call.on("end") {
                close()
            }

            launch {
                requests.collect {
                    call.write(it, null)
                }
                call.end()
            }
            awaitClose()
        }
    }
}

@Suppress("UNUSED_PARAMETER")
fun newClient(
    service: ServiceDescriptor,
    address: String,
    credentials: ChannelCredentials
): dynamic {
    @Suppress("UNUSED_VARIABLE")
    val constructor = makeClientConstructor(service.toServiceDefinition())
    return js("new constructor(address, credentials)")
}
