/*
 * Copyright (c) 2019 Toast, Inc.
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

import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto
import protokt.v1.codegen.util.ErrorContext.withEnumName
import protokt.v1.codegen.util.ErrorContext.withMessageName
import protokt.v1.codegen.util.ErrorContext.withServiceName

internal fun parseFileContents(ctx: GeneratorContext) =
    ProtoFileContents(
        ProtoFileInfo(ctx),
        FileContentParser(ctx).parseContents()
    )

internal class FileContentParser(
    private val ctx: GeneratorContext,
    private val enums: List<EnumDescriptorProto>,
    private val messages: List<DescriptorProto>,
    private val services: List<ServiceDescriptorProto>,
    private val enclosingMessages: List<String>
) {
    constructor(ctx: GeneratorContext) : this(
        ctx,
        ctx.fdp.enumTypeList,
        ctx.fdp.messageTypeList,
        ctx.fdp.serviceList,
        emptyList()
    )

    fun parseContents(): List<TopLevelType> =
        enums.mapIndexed { idx, desc ->
            withEnumName(desc.name) {
                EnumParser(ctx, idx, desc, enclosingMessages).toEnum()
            }
        } +
            messages.mapIndexed { idx, desc ->
                withMessageName((enclosingMessages + desc.name).joinToString(".")) {
                    MessageParser(ctx, idx, desc, enclosingMessages).toMessage()
                }
            } +
            services.mapIndexed { idx, desc ->
                withServiceName(desc.name) {
                    ServiceParser(idx, desc).toService()
                }
            }
}
