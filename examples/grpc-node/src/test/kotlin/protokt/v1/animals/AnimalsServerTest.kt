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
import protokt.v1.grpc.ChannelCredentials
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AnimalsServerTest {
    private val server = AnimalsServer()

    @AfterTest
    fun after() {
        server.server.forceShutdown()
    }

    @Test
    fun animals() = runTest {
        server.start()

        val dogStub = DogCoroutineStub("localhost:50051", ChannelCredentials.createInsecure())
        val dogBark = dogStub.bark(BarkRequest { })
        assertEquals("Bark!", dogBark.message)

        val pigStub = PigCoroutineStub("localhost:50051", ChannelCredentials.createInsecure())
        val pigOink = pigStub.oink(OinkRequest { })
        assertEquals("Oink!", pigOink.message)

        val sheepStub = SheepCoroutineStub("localhost:50051", ChannelCredentials.createInsecure())
        val sheepBaa = sheepStub.baa(BaaRequest { })
        assertEquals("Baa!", sheepBaa.message)
    }
}
