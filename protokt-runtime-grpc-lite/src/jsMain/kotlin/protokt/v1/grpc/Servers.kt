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

package protokt.v1.grpc

import protokt.v1.Beta
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.json

@Beta
fun Server.addService(
    service: ServiceDescriptor,
    implementation: BindableService
) =
    apply {
        addService(
            service.toServiceDefinition(),
            implementation.toUntypedServiceImplementation()
        )
    }

private fun BindableService.toUntypedServiceImplementation() =
    json(
        *bindService().methods.map { (_, serverMethodDefinition) ->
            serverMethodDefinition.methodDescriptor.lowerBareMethodName to
                serverMethodDefinition.handler
        }.toTypedArray()
    )

@Beta
suspend fun Server.start(
    address: String,
    credentials: ServerCredentials
) =
    apply {
        suspendCoroutine { continuation ->
            bindAsync(address, credentials) { _, _ ->
                start()
                continuation.resume(Unit)
            }
        }
    }
