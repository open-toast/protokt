/*
 * Copyright (c) 2020 Toast, Inc.
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

package protokt.v1.animals

import io.grpc.BindableService
import io.grpc.ServerBuilder
import io.grpc.ServerServiceDefinition
import io.grpc.stub.ServerCalls.asyncUnaryCall
import io.grpc.stub.StreamObserver

class AnimalsServer(
    private val port: Int
) {
    private val server =
        ServerBuilder
            .forPort(port)
            .addService(DogService())
            .addService(PigService())
            .addService(SheepService())
            .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@AnimalsServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    private class DogService : BindableService {
        override fun bindService() =
            ServerServiceDefinition.builder(DogGrpc.getServiceDescriptor())
                .addMethod(DogGrpc.getBarkMethod(), asyncUnaryCall(::bark))
                .build()

        fun bark(
            @Suppress("UNUSED_PARAMETER") request: BarkRequest,
            responseObserver: StreamObserver<BarkReply>
        ) {
            responseObserver.onNext(BarkReply { message = "Bark!" })
            responseObserver.onCompleted()
        }
    }

    private class PigService : BindableService {
        override fun bindService() =
            ServerServiceDefinition.builder(PigGrpc.getServiceDescriptor())
                .addMethod(PigGrpc.getOinkMethod(), asyncUnaryCall(::oink))
                .build()

        fun oink(
            @Suppress("UNUSED_PARAMETER") request: OinkRequest,
            responseObserver: StreamObserver<OinkReply>
        ) {
            responseObserver.onNext(OinkReply { message = "Oink!" })
            responseObserver.onCompleted()
        }
    }

    private class SheepService : BindableService {
        override fun bindService() =
            ServerServiceDefinition.builder(SheepGrpc.getServiceDescriptor())
                .addMethod(SheepGrpc.getBaaMethod(), asyncUnaryCall(::baa))
                .build()

        fun baa(
            @Suppress("UNUSED_PARAMETER") request: BaaRequest,
            responseObserver: StreamObserver<BaaReply>
        ) {
            responseObserver.onNext(BaaReply { message = "Baa!" })
            responseObserver.onCompleted()
        }
    }
}

fun main() {
    val port = 50051
    val server = AnimalsServer(port)
    server.start()
    server.blockUntilShutdown()
}
