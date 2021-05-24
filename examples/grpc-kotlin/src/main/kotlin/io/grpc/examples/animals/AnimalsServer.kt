/*
 * Copyright (c) 2021 Toast Inc.
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

import io.grpc.ServerBuilder
import io.grpc.ServerServiceDefinition
import io.grpc.examples.animals.DogGrpc.barkMethod
import io.grpc.examples.animals.PigGrpc.oinkMethod
import io.grpc.examples.animals.SheepGrpc.baaMethod
import io.grpc.kotlin.AbstractCoroutineServerImpl
import io.grpc.kotlin.ServerCalls.unaryServerMethodDefinition

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

    private class DogService : AbstractCoroutineServerImpl() {
        override fun bindService() =
            ServerServiceDefinition.builder(DogGrpc.serviceDescriptor)
                .addMethod(unaryServerMethodDefinition(context, barkMethod, ::bark))
                .build()

        suspend fun bark(@Suppress("UNUSED_PARAMETER") request: BarkRequest) =
            BarkReply { message = "Bark!" }
    }

    private class PigService : AbstractCoroutineServerImpl() {
        override fun bindService() =
            ServerServiceDefinition.builder(PigGrpc.serviceDescriptor)
                .addMethod(unaryServerMethodDefinition(context, oinkMethod, ::oink))
                .build()

        suspend fun oink(@Suppress("UNUSED_PARAMETER") request: OinkRequest) =
            OinkReply { message = "Oink!" }
    }

    private class SheepService : AbstractCoroutineServerImpl() {
        override fun bindService() =
            ServerServiceDefinition.builder(SheepGrpc.serviceDescriptor)
                .addMethod(unaryServerMethodDefinition(context, baaMethod, ::baa))
                .build()

        suspend fun baa(@Suppress("UNUSED_PARAMETER") request: BaaRequest) =
            BaaReply { message = "Baa!" }
    }
}

fun main() {
    val port = 50051
    val server = AnimalsServer(port)
    server.start()
    server.blockUntilShutdown()
}
