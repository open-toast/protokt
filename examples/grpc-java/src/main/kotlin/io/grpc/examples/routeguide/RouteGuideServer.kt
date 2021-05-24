/*
 * Copyright (c) 2021 Toast Inc.
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

import com.google.common.base.Stopwatch
import com.google.common.base.Ticker
import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerServiceDefinition
import io.grpc.Status
import io.grpc.stub.ServerCalls.asyncBidiStreamingCall
import io.grpc.stub.ServerCalls.asyncClientStreamingCall
import io.grpc.stub.ServerCalls.asyncServerStreamingCall
import io.grpc.stub.ServerCalls.asyncUnaryCall
import io.grpc.stub.StreamObserver
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

private val logger = Logger.getLogger(RouteGuideServer::class.java.simpleName)

/**
 * Kotlin adaptation of RouteGuideServer from the Java gRPC example.
 */
class RouteGuideServer(
    val port: Int,
    val features: Collection<Feature> = Database.features(),
    val server: Server = ServerBuilder.forPort(port).addService(RouteGuideService(features)).build()
) {
    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
                Thread {
                    println("*** shutting down gRPC server since JVM is shutting down")
                    this@RouteGuideServer.stop()
                    println("*** server shut down")
                }
        )
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    class RouteGuideService(
        val features: Collection<Feature>,
        val ticker: Ticker = Ticker.systemTicker()
    ) : BindableService {
        override fun bindService() =
            ServerServiceDefinition.builder(RouteGuideGrpc.serviceDescriptor)
                .addMethod(RouteGuideGrpc.getFeatureMethod, asyncUnaryCall(::getFeature))
                .addMethod(RouteGuideGrpc.listFeaturesMethod, asyncServerStreamingCall(::listFeatures))
                .addMethod(RouteGuideGrpc.recordRouteMethod, asyncClientStreamingCall(::recordRoute))
                .addMethod(RouteGuideGrpc.routeChatMethod, asyncBidiStreamingCall(::routeChat))
                .build()

        private val routeNotes = ConcurrentHashMap<Point, MutableList<RouteNote>>()

        fun getFeature(request: Point, responseObserver: StreamObserver<Feature>) {
            responseObserver.onNext(getFeature(request))
            responseObserver.onCompleted()
        }

        private fun getFeature(request: Point) =
            // No feature was found, return an unnamed feature.
            features.find { it.location == request } ?: Feature { location = request }

        fun listFeatures(request: Rectangle, responseObserver: StreamObserver<Feature>) {
            features.filter { it.exists() && it.location!! in request }.forEach(responseObserver::onNext)
            responseObserver.onCompleted()
        }

        fun recordRoute(responseObserver: StreamObserver<RouteSummary>): StreamObserver<Point> {
            var pointCount = 0
            var featureCount = 0
            var distance = 0
            var previous: Point? = null
            val stopwatch = Stopwatch.createStarted(ticker)
            return object : StreamObserver<Point> {
                override fun onNext(request: Point) {
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

                override fun onError(t: Throwable) {
                    logger.info { "RecordRoute failed: ${Status.fromThrowable(t)}" }
                }

                override fun onCompleted() {
                    responseObserver.onNext(
                        RouteSummary {
                            this.pointCount = pointCount
                            this.featureCount = featureCount
                            this.distance = distance
                            this.elapsedTime = Durations.fromMicros(stopwatch.elapsed(TimeUnit.MICROSECONDS))
                        }
                    )
                    responseObserver.onCompleted()
                }
            }
        }

        fun routeChat(responseObserver: StreamObserver<RouteNote>) =
            object : StreamObserver<RouteNote> {
                override fun onNext(note: RouteNote) {
                    val notes: MutableList<RouteNote> = routeNotes.computeIfAbsent(note.location!!) {
                        Collections.synchronizedList(mutableListOf<RouteNote>())
                    }
                    for (prevNote in notes.toTypedArray()) { // thread-safe snapshot
                        responseObserver.onNext(prevNote)
                    }
                    notes += note
                }

                override fun onError(t: Throwable) {
                    logger.info { "RouteChat failed: ${Status.fromThrowable(t)}" }
                }

                override fun onCompleted() {
                    responseObserver.onCompleted()
                }
            }
    }
}

fun main() {
    val port = 8980
    val server = RouteGuideServer(port)
    server.start()
    server.blockUntilShutdown()
}
