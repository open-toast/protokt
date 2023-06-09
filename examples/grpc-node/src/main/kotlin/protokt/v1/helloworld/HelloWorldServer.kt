/*
 * Copyright 2020 gRPC authors.
 * Copyright 2023 Toast, Inc.
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

import protokt.v1.grpc.Server
import protokt.v1.grpc.ServerCredentials
import protokt.v1.grpc.addService
import protokt.v1.grpc.start

class HelloWorldServer {
    val port = 50051
    val server = Server()

    suspend fun start() {
        server
            .addService(GreeterGrpc.getServiceDescriptor(), HelloWorldService())
            .start("0.0.0.0:$port", ServerCredentials.createInsecure())
        println("Server started, listening on $port")
    }

    internal class HelloWorldService : GreeterCoroutineImplBase() {
        override suspend fun sayHello(request: HelloRequest) = HelloReply {
            message = "Hello ${request.name}"
        }
    }
}
