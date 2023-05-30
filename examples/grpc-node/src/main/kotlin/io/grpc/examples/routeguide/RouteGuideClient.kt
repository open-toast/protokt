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

package io.grpc.examples.routeguide

import com.toasttab.protokt.v1.grpc.ChannelCredentials
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random
import kotlin.random.nextLong

// todo: multiplatform
private fun Int.normalizeCoordinate(): Double = this / 1.0e7

fun Point.toStr(): String {
    val lat = latitude.normalizeCoordinate()
    val long = longitude.normalizeCoordinate()
    return "$lat, $long"
}

fun Feature.exists(): Boolean = name.isNotEmpty()

class RouteGuideClient {
    private val random = Random(314159)
    private val stub = RouteGuideCoroutineStub("localhost:8980", ChannelCredentials.createInsecure())

    suspend fun getFeature(latitude: Int, longitude: Int) {
        println("*** GetFeature: lat=$latitude lon=$longitude")

        val request = point(latitude, longitude)
        val feature = stub.getFeature(request)

        if (feature.exists()) {
            println("Found feature called \"${feature.name}\" at ${feature.location?.toStr()}")
        } else {
            println("Found no feature at ${request.toStr()}")
        }
    }

    suspend fun listFeatures(lowLat: Int, lowLon: Int, hiLat: Int, hiLon: Int) {
        println("*** ListFeatures: lowLat=$lowLat lowLon=$lowLon hiLat=$hiLat liLon=$hiLon")

        val request = Rectangle {
            lo = point(lowLat, lowLon)
            hi = point(hiLat, hiLon)
        }
        var i = 1
        stub.listFeatures(request).collect { feature ->
            println("Result #${i++}: $feature")
        }
    }

    suspend fun recordRoute(points: Flow<Point>) {
        println("*** RecordRoute")
        val summary = stub.recordRoute(points)
        println("Finished trip with ${summary.pointCount} points.")
        println("Passed ${summary.featureCount} features.")
        println("Travelled ${summary.distance} meters.")
        val duration = summary.elapsedTime?.seconds
        println("It took $duration seconds.")
    }

    fun generateRoutePoints(features: List<Feature>, numPoints: Int): Flow<Point> = flow {
        for (i in 1..numPoints) {
            val feature = features.random(random)
            println("Visiting point ${feature.location?.toStr()}")
            emit(feature.location!!)
            delay(timeMillis = random.nextLong(500L..1500L))
        }
    }

    suspend fun routeChat() {
        println("*** RouteChat")
        val requests = generateOutgoingNotes()
        stub.routeChat(requests).collect { note ->
            println("Got message \"${note.message}\" at ${note.location?.toStr()}")
        }
        println("Finished RouteChat")
    }

    private fun generateOutgoingNotes(): Flow<RouteNote> = flow {
        val notes = listOf(
            RouteNote {
                message = "First message"
                location = point(0, 0)
            },
            RouteNote {
                message = "Second message"
                location = point(0, 0)
            },
            RouteNote {
                message = "Third message"
                location = point(10000000, 0)
            },
            RouteNote {
                message = "Fourth message"
                location = point(10000000, 10000000)
            },
            RouteNote {
                message = "Last message"
                location = point(0, 0)
            }
        )
        for (note in notes) {
            println("Sending message \"${note.message}\" at ${note.location?.toStr()}")
            emit(note)
            delay(500)
        }
    }
}

suspend fun main() {
    // todo: val features = emptyList<Feature>() // Database.features()

    RouteGuideClient().let {
        it.getFeature(409146138, -746188906)
        it.getFeature(0, 0)
        it.listFeatures(400000000, -750000000, 420000000, -730000000)
        // it.recordRoute(it.generateRoutePoints(features, 10))
        it.routeChat()
    }
}

private fun point(lat: Int, lon: Int): Point = Point {
    latitude = lat
    longitude = lon
}
