/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt.codegen.util

import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto
import com.toasttab.protokt.Protokt

class ServiceParser(
    private val ctx: GeneratorContext,
    private val idx: Int,
    private val desc: ServiceDescriptorProto
) {
    fun toService() =
        Service(
            name = desc.name,
            methods = desc.methodList.map(::toMethod),
            deprecated = desc.options.deprecated,
            options = ServiceOptions(
                desc.options,
                desc.options.getExtension(Protokt.service)
            ),
            index = idx
        )

    private fun toMethod(desc: MethodDescriptorProto) =
        Method(
            name = desc.name,
            inputType = requalifyProtoType(ctx, desc.inputType),
            outputType = requalifyProtoType(ctx, desc.outputType),
            clientStreaming = desc.clientStreaming,
            serverStreaming = desc.serverStreaming,
            deprecated = desc.options.deprecated,
            options = MethodOptions(
                desc.options,
                desc.options.getExtension(Protokt.method)
            )
        )
}
