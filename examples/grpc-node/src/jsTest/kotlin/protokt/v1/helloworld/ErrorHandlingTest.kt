/*
 * Copyright (c) 2024 Toast, Inc.
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

package protokt.v1.helloworld

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import protokt.v1.grpc.ChannelCredentials
import protokt.v1.grpc.Server
import protokt.v1.grpc.ServerCredentials
import protokt.v1.grpc.Status
import protokt.v1.grpc.StatusException
import protokt.v1.grpc.addService
import protokt.v1.grpc.start
import protokt.v1.helloworld.GreeterGrpcKt.GreeterCoroutineImplBase
import protokt.v1.helloworld.GreeterGrpcKt.GreeterCoroutineStub
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class ErrorHandlingTest {
    private val server = Server()

    @AfterTest
    fun after() {
        server.forceShutdown()
    }

    @Test
    fun statusExceptionPropagation() =
        runTest {
            val port = server
                .addService(GreeterGrpc.getServiceDescriptor(), NotFoundService())
                .start("0.0.0.0:0", ServerCredentials.createInsecure())

            val stub = GreeterCoroutineStub("localhost:$port", ChannelCredentials.createInsecure())

            val exception = assertFailsWith<StatusException> {
                stub.sayHello(HelloRequest { name = "test" })
            }
            assertEquals(Status.Code.NOT_FOUND, exception.status.code)
        }

    @Test
    fun runtimeExceptionBecomesInternal() =
        runTest {
            val port = server
                .addService(GreeterGrpc.getServiceDescriptor(), RuntimeErrorService())
                .start("0.0.0.0:0", ServerCredentials.createInsecure())

            val stub = GreeterCoroutineStub("localhost:$port", ChannelCredentials.createInsecure())

            val exception = assertFailsWith<StatusException> {
                stub.sayHello(HelloRequest { name = "test" })
            }
            assertEquals(Status.Code.INTERNAL, exception.status.code)
        }

    private class NotFoundService : GreeterCoroutineImplBase() {
        override suspend fun sayHello(request: HelloRequest): HelloReply =
            throw StatusException(Status.NOT_FOUND.withDescription("not found"))
    }

    private class RuntimeErrorService : GreeterCoroutineImplBase() {
        override suspend fun sayHello(request: HelloRequest): HelloReply =
            throw RuntimeException("something went wrong")
    }
}
