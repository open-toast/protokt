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

package protokt.v1.animals

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import protokt.v1.animals.DogGrpcKt.DogCoroutineStub
import protokt.v1.animals.PigGrpcKt.PigCoroutineStub
import protokt.v1.animals.SheepGrpcKt.SheepCoroutineStub
import protokt.v1.grpc.ChannelCredentials
import protokt.v1.grpc.newChannel
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AnimalsServerTest {
    private val server = AnimalsServer(0)

    @AfterTest
    fun after() {
        server.server.forceShutdown()
    }

    @Test
    fun animals() =
        runTest {
            server.start()

            val address = "localhost:${server.port}"
            val channel = newChannel(address, ChannelCredentials.createInsecure())
            try {
                val dogBark = DogCoroutineStub(channel).bark(BarkRequest { })
                assertEquals("Bark!", dogBark.message)

                val pigOink = PigCoroutineStub(channel).oink(OinkRequest { })
                assertEquals("Oink!", pigOink.message)

                val sheepBaa = SheepCoroutineStub(channel).baa(BaaRequest { })
                assertEquals("Baa!", sheepBaa.message)
            } finally {
                channel.close()
            }
        }
}
