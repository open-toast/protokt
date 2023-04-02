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

package io.grpc.examples.animals

import io.grpc.CallOptions
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.MethodDescriptor
import io.grpc.stub.ClientCalls
import java.io.Closeable
import java.util.concurrent.TimeUnit

class AnimalsClient(
    private val channel: ManagedChannel
) : Closeable {
    fun bark() {
        val request = BarkRequest { }
        val response = unaryCall(DogGrpc.getBarkMethod(), request)
        println("Received: ${response.message}")
    }

    fun oink() {
        val request = OinkRequest { }
        val response = unaryCall(PigGrpc.getOinkMethod(), request)
        println("Received: ${response.message}")
    }

    fun baa() {
        val request = BaaRequest { }
        val response = unaryCall(SheepGrpc.getBaaMethod(), request)
        println("Received: ${response.message}")
    }

    private fun <T, R> unaryCall(method: MethodDescriptor<T, R>, request: T): R =
        ClientCalls.blockingUnaryCall(
            channel,
            method,
            CallOptions.DEFAULT,
            request
        )

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

/**
 * Talk to the animals. Fluent in dog, pig and sheep.
 */
fun main(args: Array<String>) {
    val usage = "usage: ./gradlew :examples:grpc-java:AnimalsClient --args={dog|pig|sheep}"

    if (args.isEmpty()) {
        println("No animals specified.")
        println(usage)
    }

    val port = 50051

    val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()

    val client = AnimalsClient(channel)

    args.forEach {
        when (it) {
            "dog" -> client.bark()
            "pig" -> client.oink()
            "sheep" -> client.baa()
            else -> {
                println("Unknown animal type: \"$it\". Try \"dog\", \"pig\" or \"sheep\".")
                println(usage)
            }
        }
    }
}
