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

package protokt.v1.io.grpc.examples.routeguide

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import protokt.v1.grpc.ChannelCredentials
import protokt.v1.io.grpc.examples.routeguide.RouteGuideGrpcKt.RouteGuideCoroutineStub
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RouteGuideServerTest {
    private val server = RouteGuideServer(0)

    @AfterTest
    fun after() {
        server.stop()
    }

    @Test
    fun listFeatures() =
        runTest {
            server.start()

            val stub = RouteGuideCoroutineStub("localhost:${server.port}", ChannelCredentials.createInsecure())

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

    @Test
    fun recordRoute() =
        runTest {
            server.start()

            val stub = RouteGuideCoroutineStub("localhost:${server.port}", ChannelCredentials.createInsecure())

            val points = flowOf(
                Point {
                    latitude = 407838351
                    longitude = -746143763
                },
                Point {
                    latitude = 408122808
                    longitude = -743999179
                },
                Point {
                    latitude = 413628156
                    longitude = -749015468
                }
            )

            val summary = stub.recordRoute(points)
            assertEquals(3, summary.pointCount)
            assertTrue(summary.distance >= 0)
        }

    @Test
    fun routeChat() =
        runTest {
            server.start()

            val stub = RouteGuideCoroutineStub("localhost:${server.port}", ChannelCredentials.createInsecure())

            val notes = flowOf(
                RouteNote {
                    message = "First message"
                    location = Point {
                        latitude = 0
                        longitude = 0
                    }
                },
                RouteNote {
                    message = "Second message"
                    location = Point {
                        latitude = 0
                        longitude = 0
                    }
                },
                RouteNote {
                    message = "Third message"
                    location = Point {
                        latitude = 0
                        longitude = 0
                    }
                }
            )

            val responses = stub.routeChat(notes).toList()
            // First note at (0,0) has no previous notes, so no responses.
            // Second note at (0,0) echoes back "First message".
            // Third note at (0,0) echoes back "First message" and "Second message".
            assertEquals(3, responses.size)
            assertEquals("First message", responses[0].message)
            assertEquals("First message", responses[1].message)
            assertEquals("Second message", responses[2].message)
        }
}
