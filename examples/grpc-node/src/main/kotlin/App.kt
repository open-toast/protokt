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

import io.grpc.examples.animals.AnimalsServer
import io.grpc.examples.helloworld.HelloWorldClient
import io.grpc.examples.helloworld.HelloWorldServer

external val process: dynamic

// todo: adapt runtime from https://github.com/grpc/grpc/blob/v1.54.0/examples/node/static_codegen/route_guide/route_guide_client.js
// https://github.com/grpc/grpc/blob/v1.54.0/examples/node/static_codegen/route_guide/route_guide_grpc_pb.js#L146

suspend fun main() {
    // https://stackoverflow.com/a/65088942
    val argv = process.argv.slice(2) as Array<String>
    val service = argv[0]
    val mode = argv[1]

    when (service) {
        "helloworld" -> {
            when (mode) {
                "server" -> {
                    HelloWorldServer().start()
                }
                "client" -> {
                    HelloWorldClient().greet()
                }
                else -> error("unsupported mode: $mode")
            }
        }
        "routeguide" -> {
            when (mode) {
                "server" -> {
                    io.grpc.examples.routeguide.serverMain()
                }
                "client" -> {
                    io.grpc.examples.routeguide.clientMain()
                }
                else -> error("unsupported mode: $mode")
            }
        }
        "animals" -> {
            when (mode) {
                "server" -> {
                    AnimalsServer().start()
                }
                "client" -> {}
                else -> error("unsupported mode: $mode")
            }
        }
        else -> error("unsupported service: $service")
    }
}
