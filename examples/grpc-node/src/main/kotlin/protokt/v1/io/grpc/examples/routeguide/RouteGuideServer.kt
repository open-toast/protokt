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

package protokt.v1.io.grpc.examples.routeguide

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import protokt.v1.grpc.Server
import protokt.v1.grpc.ServerCredentials
import protokt.v1.grpc.addService
import protokt.v1.grpc.start
import protokt.v1.io.grpc.examples.routeguide.RouteGuideGrpcKt.RouteGuideCoroutineImplBase
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * Kotlin adaptation of RouteGuideServer from the Java gRPC example.
 */
class RouteGuideServer(
    private val port: Int
) {
    private val server = Server()

    suspend fun start() {
        server
            .addService(RouteGuideGrpc.getServiceDescriptor(), RouteGuideService(Database.features()))
            .start("0.0.0.0:$port", ServerCredentials.createInsecure())
        println("Server started, listening on $port")
    }

    fun stop() {
        server.forceShutdown()
    }

    internal class RouteGuideService(
        private val features: Collection<Feature>
    ) : RouteGuideCoroutineImplBase() {
        private val routeNotes = mutableMapOf<Point, MutableList<RouteNote>>()

        override suspend fun getFeature(request: Point): Feature =
            // No feature was found, return an unnamed feature.
            features.find { it.location == request } ?: Feature { location = request }

        override fun listFeatures(request: Rectangle): Flow<Feature> =
            features.asFlow().filter { it.exists() && it.location!! in request }

        @OptIn(ExperimentalTime::class)
        override suspend fun recordRoute(requests: Flow<Point>): RouteSummary {
            var pointCount = 0
            var featureCount = 0
            var distance = 0
            var previous: Point? = null
            val timeMark = TimeSource.Monotonic.markNow()
            requests.collect { request ->
                pointCount++
                if (getFeature(request).exists()) {
                    featureCount++
                }
                val prev = previous
                if (prev != null) {
                    distance += prev distanceTo request
                }
                previous = request
            }
            return RouteSummary {
                this.pointCount = pointCount
                this.featureCount = featureCount
                this.distance = distance
                this.elapsedTime = Durations.fromMicros(timeMark.elapsedNow().inWholeMicroseconds)
            }
        }

        override fun routeChat(requests: Flow<RouteNote>): Flow<RouteNote> =
            flow {
                requests.collect { note ->
                    val notes: MutableList<RouteNote> = routeNotes.getOrPut(note.location!!) {
                        mutableListOf()
                    }
                    for (prevNote in notes.toTypedArray()) { // thread-safe snapshot
                        emit(prevNote)
                    }
                    notes += note
                }
            }
    }
}

suspend fun serverMain() {
    val port = 8980
    val server = RouteGuideServer(port)
    server.start()
}
