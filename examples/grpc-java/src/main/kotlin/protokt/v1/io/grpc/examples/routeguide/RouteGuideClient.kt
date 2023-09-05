/*
 * Copyright 2020 gRPC authors.
 * Copyright 2021 Toast, Inc.
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

import io.grpc.CallOptions
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.stub.ClientCalls
import io.grpc.stub.StreamObserver
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import kotlin.random.Random
import kotlin.random.nextLong

class RouteGuideClient(
    private val channel: ManagedChannel
) : Closeable {
    private val logger = Logger.getLogger(RouteGuideClient::class.java.simpleName)
    private val random = Random(314159)

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    fun getFeature(latitude: Int, longitude: Int) {
        println("*** GetFeature: lat=$latitude lon=$longitude")

        val request = point(latitude, longitude)
        val feature =
            ClientCalls.blockingUnaryCall(
                channel,
                RouteGuideGrpc.getGetFeatureMethod(),
                CallOptions.DEFAULT,
                request
            )

        if (feature.exists()) {
            println("Found feature called \"${feature.name}\" at ${feature.location?.toStr()}")
        } else {
            println("Found no feature at ${request.toStr()}")
        }
    }

    fun listFeatures(lowLat: Int, lowLon: Int, hiLat: Int, hiLon: Int) {
        println("*** ListFeatures: lowLat=$lowLat lowLon=$lowLon hiLat=$hiLat liLon=$hiLon")

        val request =
            Rectangle {
                lo = point(lowLat, lowLon)
                hi = point(hiLat, hiLon)
            }
        var i = 1
        ClientCalls.blockingServerStreamingCall(
            channel,
            RouteGuideGrpc.getListFeaturesMethod(),
            CallOptions.DEFAULT,
            request
        ).forEach { feature ->
            println("Result #${i++}: $feature")
        }
    }

    fun recordRoute(points: Sequence<Point>) {
        println("*** RecordRoute")
        val latch = CountDownLatch(1)
        val requestObserver =
            ClientCalls.asyncClientStreamingCall(
                channel.newCall(RouteGuideGrpc.getRecordRouteMethod(), CallOptions.DEFAULT),
                object : StreamObserver<RouteSummary> {
                    override fun onNext(summary: RouteSummary) {
                        println("Finished trip with ${summary.pointCount} points.")
                        println("Passed ${summary.featureCount} features.")
                        println("Travelled ${summary.distance} meters.")
                        val duration = summary.elapsedTime?.seconds
                        println("It took $duration seconds.")
                    }

                    override fun onError(t: Throwable) {
                        logger.info { "RecordRoute failed: ${Status.fromThrowable(t)}" }
                        latch.countDown()
                    }

                    override fun onCompleted() {
                        latch.countDown()
                    }
                }
            )

        points.forEach(requestObserver::onNext)
        requestObserver.onCompleted()

        if (!latch.await(1, TimeUnit.MINUTES)) {
            logger.info { "Could not finish RecordRoute in one minute" }
        }
    }

    fun generateRoutePoints(features: List<Feature>, numPoints: Int) =
        sequence {
            (1..numPoints).map {
                val feature = features.random(random)
                println("Visiting point ${feature.location?.toStr()}")
                yield(feature.location!!)
                Thread.sleep(random.nextLong(500L..1500L))
            }
        }

    fun routeChat() {
        println("*** RouteChat")
        val latch = CountDownLatch(1)

        val requestObserver =
            ClientCalls.asyncBidiStreamingCall(
                channel.newCall(RouteGuideGrpc.getRouteChatMethod(), CallOptions.DEFAULT),
                object : StreamObserver<RouteNote> {
                    override fun onNext(note: RouteNote) {
                        println("Got message \"${note.message}\" at ${note.location?.toStr()}")
                    }

                    override fun onError(t: Throwable?) {
                        latch.countDown()
                    }

                    override fun onCompleted() {
                        latch.countDown()
                        println("Finished RouteChat")
                    }
                }
            )

        val requests = generateOutgoingNotes()
        requests.forEach(requestObserver::onNext)
        requestObserver.onCompleted()

        if (!latch.await(1, TimeUnit.MINUTES)) {
            logger.info { "Could not finish RouteChat in one minute" }
        }
    }

    private fun generateOutgoingNotes(): Sequence<RouteNote> =
        sequence {
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
                yield(note)
                Thread.sleep(500)
            }
        }
}

fun main() {
    val features = Database.features()

    val channel = ManagedChannelBuilder.forAddress("localhost", 8980).usePlaintext().build()

    RouteGuideClient(channel).use {
        it.getFeature(409146138, -746188906)
        it.getFeature(0, 0)
        it.listFeatures(400000000, -750000000, 420000000, -730000000)
        it.recordRoute(it.generateRoutePoints(features, 10))
        it.routeChat()
    }
}

private fun point(lat: Int, lon: Int) =
    Point {
        latitude = lat
        longitude = lon
    }
