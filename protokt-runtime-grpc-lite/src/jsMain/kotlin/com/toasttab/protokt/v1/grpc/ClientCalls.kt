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

import kotlinx.coroutines.flow.Flow
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

    @Suppress("UNUSED_PARAMETER")
    fun <ReqT, RespT> serverStreamingRpc(
        client: dynamic,
        method: MethodDescriptor<ReqT, RespT>,
        request: ReqT
    ): Flow<RespT> {
        TODO()
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun <ReqT, RespT> clientStreamingRpc(
        client: dynamic,
        method: MethodDescriptor<ReqT, RespT>,
        requests: Flow<ReqT>
    ): RespT {
        TODO()
    }

    @Suppress("UNUSED_PARAMETER")
    fun <ReqT, RespT> bidiStreamingRpc(
        client: dynamic,
        method: MethodDescriptor<ReqT, RespT>,
        requests: Flow<ReqT>
    ): Flow<RespT> {
        TODO()
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
