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

@file:JsModule("@grpc/grpc-js")
@file:JsNonModule

package protokt.v1.grpc

import protokt.v1.Beta

@Beta
external class Server {
    internal fun addService(service: dynamic, implementation: dynamic)

    fun bindAsync(
        port: String,
        creds: ServerCredentials,
        callback: (error: Throwable?, port: Int) -> Unit
    )

    fun start()

    fun forceShutdown()
}

@Beta
external class ServerCredentials {
    companion object {
        fun createInsecure(): ServerCredentials
    }
}

@Beta
external class Metadata

@Beta
external interface ServerUnaryCall<ReqT, RespT> : ServerSurfaceCall, EventEmitter {
    val request: ReqT
}

@Beta
external interface ServerSurfaceCall : EventEmitter {
    val cancelled: Boolean
    val metadata: Metadata

    fun getPeer(): String
    fun sendMetadata(responseMetadata: Metadata)
    fun getDeadline(): Deadline
    fun getPath(): String
}

@Beta
external interface Deadline

@Beta
external interface EventEmitter {
    fun emit(event: String, arg: dynamic): Boolean
}

@Beta
external interface ServerWritableStream<ReqT, RespT> : ServerSurfaceCall, ObjectWritable<RespT> {
    val request: ReqT

    fun end(metadata: Metadata?)
}

@Beta
external interface ObjectWritable<T> : Writable {
    @Suppress("ktlint:standard:function-naming")
    fun _write(chunk: T, encoding: String, callback: Any)
    fun write(chunk: T, callback: Any?): Boolean
    fun write(chunk: T, encoding: Any?, callback: Any?): Boolean
    fun setDefaultEncoding(encoding: String): ObjectWritable<T>
    fun end()
    fun end(chunk: T, callback: Any?)
    fun end(chunk: T, encoding: Any?, callback: Any?)
}

@Beta
external interface ServerReadableStream<ReqT, RespT> : ServerSurfaceCall, ObjectReadable<ReqT>

@Beta
external interface ObjectReadable<T> : Readable {
    fun read(size: Int?): T
}

@Beta
external interface ServerDuplexStream<ReqT, RespT> :
    ServerSurfaceCall,
    ObjectReadable<ReqT>,
    ObjectWritable<RespT> {

    fun end(metadata: Metadata?)
}

@Beta
external interface SurfaceCall : EventEmitter {
    val call: dynamic

    fun cancel()
    fun getPeer(): String
}

@Beta
external interface ClientReadableStream<RespT> :
    SurfaceCall,
    ObjectReadable<RespT>

@Beta
external interface ClientWritableStream<ReqT> :
    SurfaceCall,
    ObjectWritable<ReqT>

@Beta
external interface ClientDuplexStream<ReqT, RespT> :
    SurfaceCall,
    ObjectReadable<RespT>,
    ObjectWritable<ReqT> {

    fun end(metadata: Metadata?)
}

@Beta
external class Client

@Beta
external fun makeClientConstructor(serviceDefinition: dynamic): (address: String, credentials: ChannelCredentials) -> dynamic

@Beta
external class ChannelCredentials {
    companion object {
        fun createInsecure(): ChannelCredentials
    }
}
