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

import com.toasttab.protokt.v1.grpc.BindableService
import com.toasttab.protokt.v1.grpc.Server
import com.toasttab.protokt.v1.grpc.ServerCalls
import com.toasttab.protokt.v1.grpc.ServerCredentials
import com.toasttab.protokt.v1.grpc.ServerServiceDefinition
import com.toasttab.protokt.v1.grpc.Status
import com.toasttab.protokt.v1.grpc.StatusException
import com.toasttab.protokt.v1.grpc.addService
import io.grpc.examples.routeguide.Feature
import io.grpc.examples.routeguide.Point
import io.grpc.examples.routeguide.Rectangle
import io.grpc.examples.routeguide.RouteGuideGrpc
import io.grpc.examples.routeguide.RouteNote
import io.grpc.examples.routeguide.RouteSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object RouteGuideServer {
    fun main() {
        val routeGuideServiceImpl = object : RouteGuideServiceCoroutineImplBase() {
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
        routeServer.addService(RouteGuideGrpc.getServiceDescriptor(), routeGuideServiceImpl)
        routeServer.bindAsync(
            "0.0.0.0:8980",
            ServerCredentials.createInsecure()
        ) { _, _ -> routeServer.start() }
    }
}

open class RouteGuideServiceCoroutineImplBase(
    open val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : BindableService {
    final override fun bindService() =
        ServerServiceDefinition.builder(RouteGuideGrpc.getServiceDescriptor())
            .addMethod(
                RouteGuideGrpc.getGetFeatureMethod(),
                ServerCalls.unaryServerMethodDefinition(
                    coroutineContext,
                    RouteGuideGrpc.getGetFeatureMethod(),
                    ::getFeature
                )
            )
            .addMethod(
                RouteGuideGrpc.getListFeaturesMethod(),
                ServerCalls.serverStreamingServerMethodDefinition(
                    coroutineContext,
                    RouteGuideGrpc.getListFeaturesMethod(),
                    ::listFeatures,
                )
            )
            .addMethod(
                RouteGuideGrpc.getRecordRouteMethod(),
                ServerCalls.clientStreamingServerMethodDefinition(
                    coroutineContext,
                    RouteGuideGrpc.getRecordRouteMethod(),
                    ::recordRoute
                )
            )
            .addMethod(
                RouteGuideGrpc.getRouteChatMethod(),
                ServerCalls.bidiStreamingServerMethodDefinition(
                    coroutineContext,
                    RouteGuideGrpc.getRouteChatMethod(),
                    ::routeChat
                )
            )
            .build()

    open suspend fun getFeature(request: Point): Feature =
        throw StatusException(Status.UNIMPLEMENTED.withDescription("Method io.grpc.examples.routeguide.RouteGuide.GetFeature is unimplemented"))

    open fun listFeatures(request: Rectangle): Flow<Feature> =
        throw StatusException(Status.UNIMPLEMENTED.withDescription("Method io.grpc.examples.routeguide.RouteGuide.ListFeatures is unimplemented"))

    open suspend fun recordRoute(requests: Flow<Point>): RouteSummary =
        throw StatusException(Status.UNIMPLEMENTED.withDescription("Method io.grpc.examples.routeguide.RouteGuide.RecordRoute is unimplemented"))

    open fun routeChat(requests: Flow<RouteNote>): Flow<RouteNote> =
        throw StatusException(Status.UNIMPLEMENTED.withDescription("Method io.grpc.examples.routeguide.RouteGuide.RouteChat is unimplemented"))
}
