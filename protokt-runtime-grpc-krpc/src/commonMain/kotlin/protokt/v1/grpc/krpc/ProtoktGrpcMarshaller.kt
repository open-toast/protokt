/*
 * Copyright (c) 2026 Toast, Inc.
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

package protokt.v1.grpc.krpc

import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.rpc.grpc.marshaller.GrpcMarshaller
import kotlinx.rpc.grpc.marshaller.GrpcMarshallerConfig
import kotlinx.rpc.grpc.marshaller.GrpcMarshallerResolver
import protokt.v1.Beta
import protokt.v1.Deserializer
import protokt.v1.Message
import protokt.v1.deserialize
import protokt.v1.serialize
import kotlin.reflect.KType

@Beta
class ProtoktGrpcMarshaller<T : Message>(
    private val deserializer: Deserializer<T>
) : GrpcMarshaller<T> {
    override fun encode(value: T, config: GrpcMarshallerConfig?): Source {
        val buffer = Buffer()
        value.serialize(buffer)
        return buffer
    }

    override fun decode(source: Source, config: GrpcMarshallerConfig?): T =
        deserializer.deserialize(source)
}

@Beta
class ProtoktMarshallerResolver(
    private val marshallers: Map<KType, ProtoktGrpcMarshaller<*>>
) : GrpcMarshallerResolver {
    override fun resolveOrNull(kType: KType) =
        marshallers[kType]
}
