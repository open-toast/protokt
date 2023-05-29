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

import com.toasttab.protokt.v1.grpc.MethodDescriptor.MethodType.BIDI_STREAMING
import com.toasttab.protokt.v1.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING
import com.toasttab.protokt.v1.grpc.MethodDescriptor.MethodType.SERVER_STREAMING
import com.toasttab.protokt.v1.grpc.MethodDescriptor.MethodType.UNARY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object ServerCalls {
    fun <ReqT, RespT> unaryServerMethodDefinition(
        context: CoroutineContext,
        descriptor: MethodDescriptor<ReqT, RespT>,
        implementation: suspend (request: ReqT) -> RespT
    ): ServerMethodDefinition<ReqT, RespT> {
        require(descriptor.type == UNARY) {
            "Expected a unary method descriptor but got $descriptor"
        }

        val handler = { call: ServerUnaryCall<ReqT, RespT>, callback: (error: Any?, value: RespT?, trailer: Metadata?, flags: Int?) -> Unit ->
            CoroutineScope(context).launch {
                try {
                    callback(null, implementation(call.request), null, null)
                } catch (t: Throwable) {
                    callback(t, null, null, null)
                }
            }
        }

        return ServerMethodDefinition(descriptor, handler)
    }

    fun <ReqT, RespT> clientStreamingServerMethodDefinition(
        context: CoroutineContext,
        descriptor: MethodDescriptor<ReqT, RespT>,
        implementation: suspend (requests: Flow<ReqT>) -> RespT
    ): ServerMethodDefinition<ReqT, RespT> {
        require(descriptor.type == CLIENT_STREAMING) {
            "Expected a client streaming method descriptor but got $descriptor"
        }

        val handler = { call: ServerReadableStream<ReqT, RespT>, callback: (error: Any?, value: RespT?, trailer: Metadata?, flags: Int?) -> Unit ->
            val scope = CoroutineScope(context)
            val requests = callbackFlow {
                call.on("data") {
                    scope.launch { send(it as ReqT) }
                }
                call.on("end") {
                    close()
                }
                awaitClose()
            }
            scope.launch {
                try {
                    callback(null, implementation(requests), null, null)
                } catch (t: Throwable) {
                    callback(t, null, null, null)
                }
            }
        }
        return ServerMethodDefinition(descriptor, handler)
    }

    fun <ReqT, RespT> serverStreamingServerMethodDefinition(
        context: CoroutineContext,
        descriptor: MethodDescriptor<ReqT, RespT>,
        implementation: (request: ReqT) -> Flow<RespT>
    ): ServerMethodDefinition<ReqT, RespT> {
        require(descriptor.type == SERVER_STREAMING) {
            "Expected a server streaming method descriptor but got $descriptor"
        }
        val handler = { call: ServerWritableStream<ReqT, RespT> ->
            CoroutineScope(context).launch {
                implementation(call.request).collect { call.write(it, null) }
                call.end()
            }
        }
        return ServerMethodDefinition(descriptor, handler)
    }

    // todo: use Http2ServerCallStream and propagate errors properly
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <ReqT, RespT> bidiStreamingServerMethodDefinition(
        context: CoroutineContext,
        descriptor: MethodDescriptor<ReqT, RespT>,
        implementation: (requests: Flow<ReqT>) -> Flow<RespT>
    ): ServerMethodDefinition<ReqT, RespT> {
        require(descriptor.type == BIDI_STREAMING) {
            "Expected a bidi streaming method descriptor but got $descriptor"
        }
        val handler = { call: ServerDuplexStream<ReqT, RespT> ->
            val scope = CoroutineScope(context)
            val requests = callbackFlow<ReqT> {
                call.on("data") {
                    scope.launch { send(it) }
                }
                call.on("end") {
                    close()
                }
                awaitClose()
            }
            scope.launch {
                try {
                    implementation(requests).collect { call.write(it, null) }
                    call.end()
                } catch (t: Throwable) {
                    call.end()
                }
            }
        }
        return ServerMethodDefinition(descriptor, handler)
    }
}
