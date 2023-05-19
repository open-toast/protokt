/*
 * Copyright (c) 2023 Toast, Inc.
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

package com.toasttab.protokt.testing.node

import io.grpc.examples.routeguide.Feature
import io.grpc.examples.routeguide.Point
import io.grpc.examples.routeguide.Rectangle
import io.grpc.examples.routeguide.RouteNote
import io.grpc.examples.routeguide.RouteSummary
import kotlin.js.json

external class Buffer {
    companion object {
        fun from(array: ByteArray): Buffer
    }
}

object RouteGuideServer {
    fun main() {
        // todo: generate this
        val RouteGuideService = json(
            "getFeature" to json(
                "path" to "/io.grpc.examples.routeguide.RouteGuide/GetFeature",
                "requestStream" to false,
                "responseStream" to false,
                "requestSerialize" to { it: Point -> Buffer.from(it.serialize()) },
                "requestDeserialize" to { it: ByteArray -> Point.deserialize(it) },
                "responseSerialize" to { it: Feature -> Buffer.from(it.serialize()) },
                "responseDeserialize" to { it: ByteArray -> Feature.deserialize(it) }
            ),
            "listFeatures" to json(
                "path" to "/io.grpc.examples.routeguide.RouteGuide/ListFeatures",
                "requestStream" to false,
                "responseStream" to true,
                "requestSerialize" to { it: Rectangle -> Buffer.from(it.serialize()) },
                "requestDeserialize" to { it: ByteArray -> Rectangle.deserialize(it) },
                "responseSerialize" to { it: Feature -> Buffer.from(it.serialize()) },
                "responseDeserialize" to { it: ByteArray -> Feature.deserialize(it) }
            ),
            "recordRoute" to json(
                "path" to "/io.grpc.examples.routeguide.RouteGuide/RecordRoute",
                "requestStream" to true,
                "responseStream" to false,
                "requestSerialize" to { it: Point -> Buffer.from(it.serialize()) },
                "requestDeserialize" to { it: ByteArray -> Point.deserialize(it) },
                "responseSerialize" to { it: RouteSummary -> Buffer.from(it.serialize()) },
                "responseDeserialize" to { it: ByteArray -> RouteSummary.deserialize(it) }
            ),
            "routeChat" to json(
                "path" to "/io.grpc.examples.routeguide.RouteGuide/RouteChat",
                "requestStream" to true,
                "responseStream" to true,
                "requestSerialize" to { it: RouteNote -> Buffer.from(it.serialize()) },
                "requestDeserialize" to { it: ByteArray -> RouteNote.deserialize(it) },
                "responseSerialize" to { it: RouteNote -> Buffer.from(it.serialize()) },
                "responseDeserialize" to { it: ByteArray -> RouteNote.deserialize(it) }
            )
        )

        val routeGuideService = object : RouteGuideService {
            override fun getFeature(
                call: ServerUnaryCall<Point, Feature>,
                callback: (error: Any?, value: Feature, trailer: Metadata?, flags: Int?) -> Unit
            ) {
                println("received request: " + call.request)
                callback(
                    null,
                    Feature {
                        name = "foo"
                        location = call.request
                    },
                    null,
                    null
                )
            }

            override fun listFeatures(call: ServerWritableStream<Rectangle, Feature>) {
                println("received ${call.request}")
                call.write(
                    Feature {
                        name = "foo"
                        location = Point { latitude = 2; longitude = 4 }
                    },
                    null
                )
                call.write(
                    Feature {
                        name = "bar"
                        location = Point { latitude = 3; longitude = 5 }
                    },
                    null
                )
                call.end()
            }

            override fun recordRoute(
                call: ServerReadableStream<Point, RouteSummary>,
                callback: (error: Any?, value: RouteSummary, trailer: Metadata?, flags: Int?) -> Unit
            ) {
                call.on("data") {
                    println(it as Point)
                }
                call.on("end") {
                    callback(
                        null,
                        RouteSummary {
                            pointCount = 4
                            featureCount = 5
                            distance = 6
                            // elapsedTime = todo: figure this out
                        },
                        null,
                        null
                    )
                }
            }

            override fun routeChat(call: ServerDuplexStream<RouteNote, RouteNote>) {
                call.on("data") {
                    println("got $it")
                    call.write(it, null)
                }

                call.on("end") {
                    call.end()
                }
            }
        }

        val routeServer = Server()
        routeServer.addService(
            RouteGuideService,
            // todo: find a way to make this friendly to build
            json(
                "getFeature" to routeGuideService::getFeature,
                "listFeatures" to routeGuideService::listFeatures,
                "recordRoute" to routeGuideService::recordRoute,
                "routeChat" to routeGuideService::routeChat
            )
        )
        routeServer.bindAsync(
            "0.0.0.0:8980",
            ServerCredentials.createInsecure()
        ) { _, _ -> routeServer.start() }
    }
}

// todo: generate this
interface RouteGuideService {
    fun getFeature(
        call: ServerUnaryCall<Point, Feature>,
        callback: (error: Any?, value: Feature, trailer: Metadata?, flags: Int?) -> Unit
    )

    fun listFeatures(call: ServerWritableStream<Rectangle, Feature>)

    fun recordRoute(
        call: ServerReadableStream<Point, RouteSummary>,
        callback: (error: Any?, value: RouteSummary, trailer: Metadata?, flags: Int?) -> Unit
    )

    fun routeChat(call: ServerDuplexStream<RouteNote, RouteNote>)
}
