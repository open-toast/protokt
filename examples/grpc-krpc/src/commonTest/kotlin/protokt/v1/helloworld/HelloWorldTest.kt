/*
 * Copyright (c) 2026 Toast, Inc.
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

import kotlinx.coroutines.runBlocking
import kotlinx.rpc.grpc.client.GrpcClient
import kotlinx.rpc.grpc.server.GrpcServer
import kotlinx.rpc.registerService
import kotlinx.rpc.withService
import kotlin.test.Test
import kotlin.test.assertEquals

class HelloWorldTest {
    @Test
    fun sayHello() =
        runBlocking {
            val port = 50079

            val server =
                GrpcServer(port) {
                    messageMarshallerResolver = helloWorldMarshallerResolver
                    services {
                        registerService<Greeter> { GreeterService() }
                    }
                }
            server.start()

            try {
                val client =
                    GrpcClient("localhost", port) {
                        messageMarshallerResolver = helloWorldMarshallerResolver
                        credentials = plaintext()
                    }

                val greeter = client.withService<Greeter>()
                val reply = greeter.SayHello(HelloRequest { name = "test name" })
                assertEquals("Hello test name", reply.message)
            } finally {
                server.shutdownNow()
            }
        }
}
