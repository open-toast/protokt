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

@Beta
class ServerServiceDefinition internal constructor(
    val serviceDescriptor: ServiceDescriptor?,
    internal val methods: Map<String, ServerMethodDefinition<*, *>>
) {
    class Builder internal constructor(
        private val serviceName: String,
        private val serviceDescriptor: ServiceDescriptor?
    ) {
        private val methods = mutableMapOf<String, ServerMethodDefinition<*, *>>()

        fun addMethod(def: ServerMethodDefinition<*, *>) =
            apply {
                require(serviceName == def.methodDescriptor.serviceName) {
                    "Method name should be prefixed with service name and separated with '/'. " +
                        "Expected service name: '$serviceName'. Actual fully qualified method name: " +
                        "'${def.methodDescriptor.fullMethodName}'."
                }
                check(def.methodDescriptor.fullMethodName !in methods) {
                    "Method by same name already registered: ${def.methodDescriptor.fullMethodName}"
                }
                methods[def.methodDescriptor.fullMethodName] = def
            }

        fun build(): ServerServiceDefinition {
            val serviceDescriptor =
                this.serviceDescriptor ?: ServiceDescriptor(serviceName, methods.values.map { it.methodDescriptor })

            val tmpMethods = methods.toMutableMap()

            serviceDescriptor.methods.forEach { descriptorMethod ->
                val removed = tmpMethods.remove(descriptorMethod.fullMethodName)
                checkNotNull(removed) {
                    "No method bound for descriptor entry ${descriptorMethod.fullMethodName}"
                }
                check(removed.methodDescriptor == descriptorMethod) {
                    "Bound method for ${descriptorMethod.fullMethodName} not same instance " +
                        "as method in service descriptor"
                }
            }
            check(tmpMethods.isEmpty()) {
                "No entry in descriptor matching bound method ${tmpMethods.values.first()}"
            }
            return ServerServiceDefinition(serviceDescriptor, methods)
        }
    }

    companion object {
        fun builder(serviceName: String) =
            Builder(serviceName, null)

        fun builder(serviceDescriptor: ServiceDescriptor) =
            Builder(serviceDescriptor.name, serviceDescriptor)
    }
}
