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

import com.toasttab.protokt.grpc.v1.BindableService
import com.toasttab.protokt.grpc.v1.KtMarshaller
import com.toasttab.protokt.grpc.v1.MethodDescriptor
import com.toasttab.protokt.grpc.v1.Server
import com.toasttab.protokt.grpc.v1.ServerCalls
import com.toasttab.protokt.grpc.v1.ServerCredentials
import com.toasttab.protokt.grpc.v1.ServerServiceDefinition
import com.toasttab.protokt.grpc.v1.ServiceDescriptor
import com.toasttab.protokt.grpc.v1.Status
import com.toasttab.protokt.grpc.v1.StatusException
import com.toasttab.protokt.grpc.v1.addService
import io.grpc.examples.routeguide.Feature
import io.grpc.examples.routeguide.Point
import io.grpc.examples.routeguide.Rectangle
import io.grpc.examples.routeguide.RouteNote
import io.grpc.examples.routeguide.RouteSummary
import kotlinx.coroutines.flow.Flow
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
        routeServer.addService(routeGuideService, routeGuideServiceImpl)
        routeServer.bindAsync(
            "0.0.0.0:8980",
            ServerCredentials.createInsecure()
        ) { _, _ -> routeServer.start() }
    }
}

// todo: generate this
val getFeatureMethod =
    MethodDescriptor(
        "io.grpc.examples.routeguide.RouteGuide/GetFeature",
        MethodDescriptor.MethodType.UNARY,
        KtMarshaller(Point),
        KtMarshaller(Feature)
    )

val listFeaturesMethod =
    MethodDescriptor(
        "io.grpc.examples.routeguide.RouteGuide/ListFeatures",
        MethodDescriptor.MethodType.SERVER_STREAMING,
        KtMarshaller(Rectangle),
        KtMarshaller(Feature)
    )

val recordRouteMethod =
    MethodDescriptor(
        "io.grpc.examples.routeguide.RouteGuide/RecordRoute",
        MethodDescriptor.MethodType.CLIENT_STREAMING,
        KtMarshaller(Point),
        KtMarshaller(RouteSummary)
    )

val routeChatMethod =
    MethodDescriptor(
        "io.grpc.examples.routeguide.RouteGuide/RouteChat",
        MethodDescriptor.MethodType.BIDI_STREAMING,
        KtMarshaller(RouteNote),
        KtMarshaller(RouteNote)
    )

val routeGuideService =
    ServiceDescriptor(
        "io.grpc.examples.routeguide.RouteGuide",
        listOf(
            getFeatureMethod,
            listFeaturesMethod,
            recordRouteMethod,
            routeChatMethod
        )
    )

open class RouteGuideServiceCoroutineImplBase(
    open val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : BindableService {
    final override fun bindService() =
        ServerServiceDefinition.builder(routeGuideService)
            .addMethod(
                getFeatureMethod,
                ServerCalls.unaryServerMethodDefinition(
                    coroutineContext,
                    getFeatureMethod,
                    ::getFeature
                )
            )
            .addMethod(
                listFeaturesMethod,
                ServerCalls.serverStreamingServerMethodDefinition(
                    coroutineContext,
                    listFeaturesMethod,
                    ::listFeatures,
                )
            )
            .addMethod(
                recordRouteMethod,
                ServerCalls.clientStreamingServerMethodDefinition(
                    coroutineContext,
                    recordRouteMethod,
                    ::recordRoute
                )
            )
            .addMethod(
                routeChatMethod,
                ServerCalls.bidiStreamingServerMethodDefinition(
                    coroutineContext,
                    routeChatMethod,
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
