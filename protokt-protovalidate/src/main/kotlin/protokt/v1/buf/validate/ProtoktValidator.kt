/*
 * Copyright (c) 2024 Toast, Inc.
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

package protokt.v1.buf.validate

import build.buf.protovalidate.Config
import build.buf.protovalidate.ValidationResult
import build.buf.protovalidate.internal.celext.ValidateLibrary
import build.buf.protovalidate.internal.evaluator.Evaluator
import build.buf.protovalidate.internal.evaluator.EvaluatorBuilder
import build.buf.protovalidate.internal.evaluator.MessageValue
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.Descriptor
import org.projectnessie.cel.Env
import org.projectnessie.cel.Library
import protokt.v1.Beta
import protokt.v1.GeneratedMessage
import protokt.v1.Message
import protokt.v1.google.protobuf.FileDescriptor
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.toDynamicMessage
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.findAnnotation

@Beta
class ProtoktValidator @JvmOverloads constructor(
    config: Config = Config.newBuilder().build()
) {
    private val evaluatorBuilder =
        EvaluatorBuilder(
            Env.newEnv(Library.Lib(ValidateLibrary())),
            config.isDisableLazy
        )

    private val failFast = config.isFailFast

    private val evaluatorsByFullTypeName = ConcurrentHashMap<String, Evaluator>()
    private val runtimeContext = RuntimeContext(emptyList())

    fun load(descriptor: FileDescriptor) {
        descriptor
            .toProtobufJavaDescriptor()
            .messageTypes
            .forEach(::load)
    }

    private fun FileDescriptor.toProtobufJavaDescriptor(): Descriptors.FileDescriptor =
        Descriptors.FileDescriptor.buildFrom(
            DescriptorProtos.FileDescriptorProto.parseFrom(proto.serialize()),
            dependencies.map { it.toProtobufJavaDescriptor() }.toTypedArray(),
            true
        )

    fun load(descriptor: Descriptor) {
        runtimeContext.add(descriptor)
        evaluatorsByFullTypeName[descriptor.fullName] = evaluatorBuilder.load(descriptor)
        descriptor.nestedTypes.forEach(::load)
    }

    fun validate(message: Message): ValidationResult =
        evaluatorsByFullTypeName
            .getValue(message::class.findAnnotation<GeneratedMessage>()!!.fullTypeName)
            .evaluate(MessageValue(message.toDynamicMessage(runtimeContext)), failFast)
}
