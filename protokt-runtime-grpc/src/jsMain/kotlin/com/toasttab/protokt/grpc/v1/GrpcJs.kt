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

package com.toasttab.protokt.grpc.v1

external class Server {
    internal fun addService(service: dynamic, implementation: dynamic)

    fun bindAsync(
        port: String,
        creds: ServerCredentials,
        callback: (error: Throwable?, port: Int) -> Unit
    )

    fun start()
}

external class ServerCredentials {
    companion object {
        fun createInsecure(): ServerCredentials
    }
}

external class Metadata

external interface ServerUnaryCall<ReqT, RespT> : ServerSurfaceCall, EventEmitter<RespT> {
    val request: ReqT
}

external interface ServerSurfaceCall {
    val cancelled: Boolean
    val metadata: Metadata

    fun getPeer(): String
    fun sendMetadata(responseMetadata: Metadata)
    fun getDeadline(): Deadline
    fun getPath(): String
}

external interface Deadline

external interface EventEmitter<T>

external interface ServerWritableStream<ReqT, RespT> : ServerSurfaceCall, ObjectWritable<RespT> {
    val request: ReqT

    fun end(metadata: Metadata?)
}

external interface ObjectWritable<T> : Writable {
    fun _write(chunk: T, encoding: String, callback: Any)
    fun write(chunk: T, callback: Any?): Boolean
    fun write(chunk: T, encoding: Any?, callback: Any?): Boolean
    fun setDefaultEncoding(encoding: String): ObjectWritable<T>
    fun end()
    fun end(chunk: T, callback: Any?)
    fun end(chunk: T, encoding: Any?, callback: Any?)
}

external interface ServerReadableStream<ReqT, RespT> : ServerSurfaceCall, ObjectReadable<ReqT>

external interface ObjectReadable<T> : Readable {
    fun read(size: Int?): T
}

external interface ServerDuplexStream<ReqT, RespT> :
    ServerSurfaceCall,
    ObjectReadable<ReqT>,
    ObjectWritable<RespT> {

    fun end(metadata: Metadata?)
}
