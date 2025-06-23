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
import protokt.v1.Collections.copyList
import protokt.v1.OnlyForUseByGeneratedProtoCode

@Beta
class ServiceDescriptor internal constructor(
    val name: String,
    methods: List<MethodDescriptor<*, *>>,
    val schemaDescriptor: Any? = null
) {
    @OptIn(OnlyForUseByGeneratedProtoCode::class)
    val methods = copyList(methods)

    override fun toString() =
        "ServiceDescriptor(" +
            "name=$name, " +
            "methods=$methods, " +
            "schemaDescriptor=$schemaDescriptor)"

    class Builder internal constructor(
        private var name: String
    ) {
        private val methods = mutableListOf<MethodDescriptor<*, *>>()
        private var schemaDescriptor: Any? = null

        fun setName(name: String) =
            apply { this.name = name }

        fun addMethod(method: MethodDescriptor<*, *>) =
            apply { methods.add(method) }

        fun setSchemaDescriptor(schemaDescriptor: Any?) =
            apply { this.schemaDescriptor = schemaDescriptor }

        fun build() =
            ServiceDescriptor(
                name,
                methods,
                schemaDescriptor
            )
    }

    companion object {
        fun newBuilder(name: String) =
            Builder(name)
    }
}
