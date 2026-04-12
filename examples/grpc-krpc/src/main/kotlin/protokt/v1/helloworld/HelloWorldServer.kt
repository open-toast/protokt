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

import io.grpc.InsecureServerCredentials
import kotlinx.rpc.grpc.marshaller.GrpcMarshallerResolver
import kotlinx.rpc.grpc.server.GrpcServer
import protokt.v1.grpc.krpc.ProtoktGrpcMarshaller
import protokt.v1.grpc.krpc.ProtoktMarshallerResolver
import kotlin.reflect.typeOf

internal class GreeterService : Greeter {
    override suspend fun SayHello(message: HelloRequest) =
        HelloReply { this.message = "Hello ${message.name}" }
}

val helloWorldMarshallerResolver: GrpcMarshallerResolver =
    ProtoktMarshallerResolver(
        mapOf(
            typeOf<HelloRequest>() to ProtoktGrpcMarshaller(HelloRequest),
            typeOf<HelloReply>() to ProtoktGrpcMarshaller(HelloReply),
        )
    )

fun helloWorldServer(port: Int): GrpcServer {
    val server = GrpcServer(port) {
        messageMarshallerResolver = helloWorldMarshallerResolver
        credentials = InsecureServerCredentials.create()
        services {
            registerService(Greeter::class) { GreeterService() }
        }
    }
    return server.start()
}

suspend fun main() {
    val port = 50051
    val server = helloWorldServer(port)
    println("Server started, listening on $port")
    server.awaitTermination()
}
