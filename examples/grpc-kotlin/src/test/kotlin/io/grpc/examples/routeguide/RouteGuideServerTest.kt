/*
 * Copyright 2022 gRPC authors.
 * Copyright 2022 Toast, Inc.
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

import io.grpc.testing.GrpcServerRule
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import protokt.v1.io.grpc.examples.routeguide.Database
import protokt.v1.io.grpc.examples.routeguide.RouteGuideServer
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteGuideServerTest {

    @get:Rule
    val grpcServerRule: GrpcServerRule = GrpcServerRule().directExecutor()

    @Test
    fun listFeatures() = runBlocking {
        val service = RouteGuideServer.RouteGuideService(Database.features())
        grpcServerRule.serviceRegistry.addService(service)

        val stub = RouteGuideGrpcKt.RouteGuideCoroutineStub(grpcServerRule.channel)

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
