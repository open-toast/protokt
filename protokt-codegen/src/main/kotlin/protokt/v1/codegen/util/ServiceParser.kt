/*
 * Copyright (c) 2022 Toast, Inc.
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

package protokt.v1.codegen.util

import com.squareup.kotlinpoet.ClassName
import io.grpc.kotlin.generator.protoc.ProtoMethodName
import protokt.v1.google.protobuf.MethodDescriptorProto
import protokt.v1.google.protobuf.ServiceDescriptorProto
import protokt.v1.method
import protokt.v1.reflect.requalifyProtoType
import protokt.v1.service

class ServiceParser(
    private val idx: Int,
    private val desc: ServiceDescriptorProto
) {
    fun toService() =
        Service(
            name = desc.name.orEmpty(),
            methods = desc.method.map(::toMethod),
            deprecated = desc.options?.deprecated == true,
            options = ServiceOptions(
                desc.options ?: protokt.v1.google.protobuf.ServiceOptions {},
                desc.options?.service ?: protokt.v1.ServiceOptions {}
            ),
            index = idx
        )

    private fun toMethod(desc: MethodDescriptorProto) =
        Method(
            name = ProtoMethodName(desc.name.orEmpty()),
            inputType = ClassName.bestGuess(requalifyProtoType(desc.inputType.orEmpty())),
            outputType = ClassName.bestGuess(requalifyProtoType(desc.outputType.orEmpty())),
            clientStreaming = desc.clientStreaming == true,
            serverStreaming = desc.serverStreaming == true,
            deprecated = desc.options?.deprecated == true,
            options = MethodOptions(
                desc.options ?: protokt.v1.google.protobuf.MethodOptions {},
                desc.options?.method ?: protokt.v1.MethodOptions {}
            )
        )
}
