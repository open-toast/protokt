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

import protokt.v1.animals.DogGrpcKt.DogCoroutineStub
import protokt.v1.animals.PigGrpcKt.PigCoroutineStub
import protokt.v1.animals.SheepGrpcKt.SheepCoroutineStub
import protokt.v1.grpc.ChannelCredentials

class AnimalsClient(
    port: Int
) {
    private val dogStub by lazy {
        DogCoroutineStub("localhost:$port", ChannelCredentials.createInsecure())
    }

    private val pigStub by lazy {
        PigCoroutineStub("localhost:$port", ChannelCredentials.createInsecure())
    }

    private val sheepStub by lazy {
        SheepCoroutineStub("localhost:$port", ChannelCredentials.createInsecure())
    }

    suspend fun bark() {
        val request = BarkRequest {}
        val response = dogStub.bark(request)
        println("Received: ${response.message}")
    }

    suspend fun oink() {
        val request = OinkRequest {}
        val response = pigStub.oink(request)
        println("Received: ${response.message}")
    }

    suspend fun baa() {
        val request = BaaRequest {}
        val response = sheepStub.baa(request)
        println("Received: ${response.message}")
    }
}

/**
 * Talk to the animals. Fluent in dog, pig and sheep.
 */
suspend fun main(args: Array<String>) {
    val usage = "usage: ./gradlew :examples:grpc-kotlin:AnimalsClient --args={dog|pig|sheep}"

    if (args.isEmpty()) {
        println("No animals specified.")
        println(usage)
    }

    val port = 50051
    val client = AnimalsClient(port)

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
