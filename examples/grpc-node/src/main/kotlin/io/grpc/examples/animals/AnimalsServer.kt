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

package io.grpc.examples.animals

import com.toasttab.protokt.v1.grpc.Server
import com.toasttab.protokt.v1.grpc.ServerCredentials
import com.toasttab.protokt.v1.grpc.addService

class AnimalsServer {
    val port = 50051
    val server = Server()

    fun start() {
        server.apply {
            addService(DogGrpc.getServiceDescriptor(), DogService())
            addService(PigGrpc.getServiceDescriptor(), PigService())
            addService(SheepGrpc.getServiceDescriptor(), SheepService())
            bindAsync(
                "0.0.0.0:$port",
                ServerCredentials.createInsecure()
            ) { _, _ ->
                start()
                println("Server started, listening on $port")
            }
        }
    }

    internal class DogService : DogCoroutineImplBase() {
        override suspend fun bark(request: BarkRequest) = BarkReply {
            message = "Bark!"
        }
    }

    internal class PigService : PigCoroutineImplBase() {
        override suspend fun oink(request: OinkRequest) = OinkReply {
            message = "Oink!"
        }
    }

    internal class SheepService : SheepCoroutineImplBase() {
        override suspend fun baa(request: BaaRequest) = BaaReply {
            message = "Baa!"
        }
    }
}
