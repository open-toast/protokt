package io.grpc.examples.routeguide

import com.toasttab.protokt.v1.grpc.Server
import com.toasttab.protokt.v1.grpc.ServerCredentials
import com.toasttab.protokt.v1.grpc.addService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class RouteGuideServer {
    val port = 50051

    fun start() {
        val routeGuideServiceImpl = object : RouteGuideCoroutineImplBase() {
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
            "0.0.0.0:$port",
            ServerCredentials.createInsecure()
        ) { _, _ -> routeServer.start() }
    }
}
