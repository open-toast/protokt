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

import com.toasttab.protokt.testing.node.Status.UNIMPLEMENTED
import io.grpc.examples.routeguide.Feature
import io.grpc.examples.routeguide.Point
import io.grpc.examples.routeguide.Rectangle
import io.grpc.examples.routeguide.RouteNote
import io.grpc.examples.routeguide.RouteSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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

        val routeGuideService = object : RouteGuideServiceCoroutineImplBase() {
            override suspend fun getFeature(request: Point): Feature {
                println("received request: $request")
                return Feature {
                    name = "foo"
                    location = request
                }
            }

            override fun listFeatures(request: Rectangle): Flow<Feature> {
                println("received $request")
                return flowOf(
                    Feature {
                        name = "foo"
                        location = Point { latitude = 2; longitude = 4 }
                    },
                    Feature {
                        name = "bar"
                        location = Point { latitude = 3; longitude = 5 }
                    }
                )
            }

            override suspend fun recordRoute(requests: Flow<Point>): RouteSummary {
                requests.collect {
                    println("received RecordRoute request: $it")
                }
                return RouteSummary {
                    pointCount = 4
                    featureCount = 5
                    distance = 6
                    // elapsedTime = todo: figure this out
                }
            }

            override fun routeChat(requests: Flow<RouteNote>): Flow<RouteNote> =
                flow {
                    requests.collect {
                        println("got chat request $it")
                        emit(it)
                    }
                }
        }

        val routeServer = Server()
        routeServer.addService(
            RouteGuideService,
            // todo: find a way to make this friendly to build
            json(
                "getFeature" to routeGuideService::_getFeature,
                "listFeatures" to routeGuideService::_listFeatures,
                "recordRoute" to routeGuideService::_recordRoute,
                "routeChat" to routeGuideService::_routeChat
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
    fun _getFeature(
        call: ServerUnaryCall<Point, Feature>,
        callback: (error: Any?, value: Feature?, trailer: Metadata?, flags: Int?) -> Unit
    )

    fun _listFeatures(call: ServerWritableStream<Rectangle, Feature>)

    fun _recordRoute(
        call: ServerReadableStream<Point, RouteSummary>,
        callback: (error: Any?, value: RouteSummary?, trailer: Metadata?, flags: Int?) -> Unit
    )

    fun _routeChat(call: ServerDuplexStream<RouteNote, RouteNote>)
}

open class RouteGuideServiceCoroutineImplBase(
    open val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : RouteGuideService {
    open suspend fun getFeature(request: Point): Feature =
        throw StatusException(UNIMPLEMENTED, "Method io.grpc.examples.routeguide.RouteGuide.GetFeature is unimplemented")

    open fun listFeatures(request: Rectangle): Flow<Feature> =
        throw StatusException(UNIMPLEMENTED, "Method io.grpc.examples.routeguide.RouteGuide.ListFeatures is unimplemented")

    open suspend fun recordRoute(requests: Flow<Point>): RouteSummary =
        throw StatusException(UNIMPLEMENTED, "Method io.grpc.examples.routeguide.RouteGuide.RecordRoute is unimplemented")

    open fun routeChat(requests: Flow<RouteNote>): Flow<RouteNote> =
        throw StatusException(UNIMPLEMENTED, "Method io.grpc.examples.routeguide.RouteGuide.RouteChat is unimplemented")

    // todo: pull into runtime
    final override fun _getFeature(
        call: ServerUnaryCall<Point, Feature>,
        callback: (error: Any?, value: Feature?, trailer: Metadata?, flags: Int?) -> Unit
    ) {
        CoroutineScope(coroutineContext).launch {
            try {
                callback(null, getFeature(call.request), null, null)
            } catch (t: Throwable) {
                callback(t, null, null, null)
            }
        }
    }

    final override fun _listFeatures(call: ServerWritableStream<Rectangle, Feature>) {
        CoroutineScope(coroutineContext).launch {
            listFeatures(call.request).collect { call.write(it, null) }
            call.end()
        }
    }

    final override fun _recordRoute(
        call: ServerReadableStream<Point, RouteSummary>,
        callback: (error: Any?, value: RouteSummary?, trailer: Metadata?, flags: Int?) -> Unit
    ) {
        val scope = CoroutineScope(coroutineContext)
        val requests = callbackFlow<Point> {
            call.on("data") {
                scope.launch { send(it) }
            }
            call.on("end") {
                close()
            }
            awaitClose()
        }
        scope.launch {
            try {
                callback(null, recordRoute(requests), null, null)
            } catch (t: Throwable) {
                callback(t, null, null, null)
            }
        }
    }

    final override fun _routeChat(call: ServerDuplexStream<RouteNote, RouteNote>) {
        val scope = CoroutineScope(coroutineContext)
        val requests = callbackFlow<RouteNote> {
            call.on("data") {
                scope.launch { send(it) }
            }
            call.on("end") {
                close()
            }
            awaitClose()
        }
        scope.launch {
            try {
                routeChat(requests).collect { call.write(it, null) }
                call.end()
            } catch (t: Throwable) {
                // todo: propagate error
                call.end()
            }
        }
    }
}
