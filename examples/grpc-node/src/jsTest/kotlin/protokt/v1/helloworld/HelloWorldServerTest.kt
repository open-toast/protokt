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

package protokt.v1.helloworld

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import protokt.v1.grpc.ChannelCredentials
import protokt.v1.helloworld.GreeterGrpcKt.GreeterCoroutineStub
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HelloWorldServerTest {
    private val server = HelloWorldServer(0)

    @AfterTest
    fun after() {
        server.server.forceShutdown()
    }

    @Test
    fun sayHello() =
        runTest {
            server.start()

            val stub = GreeterCoroutineStub("localhost:${server.port}", ChannelCredentials.createInsecure())
            val testName = "test name"

            val reply = stub.sayHello(HelloRequest { this.name = testName })
            assertEquals("Hello $testName", reply.message)
        }
}
