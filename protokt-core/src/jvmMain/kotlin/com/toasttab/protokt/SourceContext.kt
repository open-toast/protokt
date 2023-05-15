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

@file:Suppress("DEPRECATION")
@file:JvmName("Source_contextKt")

package com.toasttab.protokt

@Deprecated("use v1")
object SourceContextProto {
    val descriptor: FileDescriptor by lazy {
        val descriptorData = arrayOf(
            "\n\$google/protobuf/source_context.proto" +
                "google.protobuf\"\"\n\rSourceContext\n\tfile" +
                "_name (\tBﾊ\ncom.google.protobufBSou" +
                "rceContextProtoPZ6google.golang.org/pro" +
                "tobuf/types/known/sourcecontextpbﾢGPBﾪ" +
                "Google.Protobuf.WellKnownTypesbproto3"
        )

        FileDescriptor.buildFrom(
            descriptorData,
            listOf()
        )
    }
}

@Deprecated("use v1")
val SourceContext.Deserializer.descriptor: Descriptor
    get() = SourceContextProto.descriptor.messageTypes[0]
