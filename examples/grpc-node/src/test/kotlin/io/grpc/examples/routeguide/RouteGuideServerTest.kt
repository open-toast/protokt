/*
 * Copyright 2022 gRPC authors.
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

package io.grpc.examples.routeguide

import protokt.v1.grpc.ChannelCredentials
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RouteGuideServerTest {
    private val server = RouteGuideServer(8980)

    @AfterTest
    fun after() {
        server.stop()
    }

    @Test
    fun listFeatures() = runTest {
        server.start()

        val stub = RouteGuideCoroutineStub("localhost:8980", ChannelCredentials.createInsecure())

        val rectangle = Rectangle {
            lo = Point {
                latitude = 407838351
                longitude = -746143763
            }
            hi = Point {
                latitude = 407838351
                longitude = -746143763
            }
        }

        val features = stub.listFeatures(rectangle).toList()
        assertEquals("Patriots Path, Mendham, NJ 07945, USA", features.first().name)
    }
}
