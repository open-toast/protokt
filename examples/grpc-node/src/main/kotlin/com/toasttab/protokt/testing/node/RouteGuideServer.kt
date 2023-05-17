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

object RouteGuideServer {
    fun main() {
        val serialize_routeguide_Point: (Point) -> ByteArray = Point::serialize
        val deserialize_routeguide_Point: (ByteArray) -> Point = Point.Deserializer::deserialize
        val serialize_routeguide_Feature: (Feature) -> ByteArray = Feature::serialize
        val deserialize_routeguide_Feature: (ByteArray) -> Feature = Feature.Deserializer::deserialize
        val serialize_routeguide_Rectangle: (Rectangle) -> ByteArray = Rectangle::serialize
        val deserialize_routeguide_Rectangle: (ByteArray) -> Rectangle = Rectangle.Deserializer::deserialize
        val serialize_routeguide_RouteSummary: (RouteSummary) -> ByteArray = RouteSummary::serialize
        val deserialize_routeguide_RouteSummary: (ByteArray) -> RouteSummary = RouteSummary.Deserializer::deserialize
        val serialize_routeguide_RouteNote: (RouteNote) -> ByteArray = RouteNote::serialize
        val deserialize_routeguide_RouteNote: (ByteArray) -> RouteNote = RouteNote.Deserializer::deserialize

        val decl = """
            var RouteGuideService = {
            // A simple RPC.
            //
            // Obtains the feature at a given position.
            //
            // A feature with an empty name is returned if there's no feature at the given
            // position.
            getFeature: {
                path: '/routeguide.RouteGuide/GetFeature',
                requestStream: false,
                responseStream: false,
                //requestType: route_guide_pb.Point,
                //responseType: route_guide_pb.Feature,
                requestSerialize: serialize_routeguide_Point,
                requestDeserialize: deserialize_routeguide_Point,
                responseSerialize: serialize_routeguide_Feature,
                responseDeserialize: deserialize_routeguide_Feature,
              },
              // A server-to-client streaming RPC.
            //
            // Obtains the Features available within the given Rectangle.  Results are
            // streamed rather than returned at once (e.g. in a response message with a
            // repeated field), as the rectangle may cover a large area and contain a
            // huge number of features.
            listFeatures: {
                path: '/routeguide.RouteGuide/ListFeatures',
                requestStream: false,
                responseStream: true,
                //requestType: route_guide_pb.Rectangle,
                //responseType: route_guide_pb.Feature,
                requestSerialize: serialize_routeguide_Rectangle,
                requestDeserialize: deserialize_routeguide_Rectangle,
                responseSerialize: serialize_routeguide_Feature,
                responseDeserialize: deserialize_routeguide_Feature,
              },
              // A client-to-server streaming RPC.
            //
            // Accepts a stream of Points on a route being traversed, returning a
            // RouteSummary when traversal is completed.
            recordRoute: {
                path: '/routeguide.RouteGuide/RecordRoute',
                requestStream: true,
                responseStream: false,
                //requestType: route_guide_pb.Point,
                //responseType: route_guide_pb.RouteSummary,
                requestSerialize: serialize_routeguide_Point,
                requestDeserialize: deserialize_routeguide_Point,
                responseSerialize: serialize_routeguide_RouteSummary,
                responseDeserialize: deserialize_routeguide_RouteSummary,
              },
              // A Bidirectional streaming RPC.
            //
            // Accepts a stream of RouteNotes sent while a route is being traversed,
            // while receiving other RouteNotes (e.g. from other users).
            routeChat: {
                path: '/routeguide.RouteGuide/RouteChat',
                requestStream: true,
                responseStream: true,
                //requestType: route_guide_pb.RouteNote,
                //responseType: route_guide_pb.RouteNote,
                requestSerialize: serialize_routeguide_RouteNote,
                requestDeserialize: deserialize_routeguide_RouteNote,
                responseSerialize: serialize_routeguide_RouteNote,
                responseDeserialize: deserialize_routeguide_RouteNote,
              },
            };
        """
        js(decl)

        val getFeature = { call: dynamic ->
            println(call.request)
            call.write(
                Feature {
                    name = "foo"
                    location = call.request
                }
            )
            call.end()
        }

        val listFeatures = {
        }

        val recordRoute = {
        }

        val routeChat = {
        }

        js("var grpc = require('@grpc/grpc-js')")
        var routeServer = js("new grpc.Server()")
        val startServer = { js("routeServer.start()") }

        val server = """
          routeServer.addService(RouteGuideService, {
            getFeature: getFeature,
            listFeatures: listFeatures,
            recordRoute: recordRoute,
            routeChat: routeChat
          });
            
          routeServer.bindAsync('0.0.0.0:8980', grpc.ServerCredentials.createInsecure(), startServer)
        """
        js(server)
    }
}
