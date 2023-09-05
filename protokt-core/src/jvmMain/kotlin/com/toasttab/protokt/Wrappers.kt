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

package com.toasttab.protokt

@Deprecated("use v1")
object WrappersProto {
    val descriptor: FileDescriptor by lazy {
        val descriptorData = arrayOf(
            "\ngoogle/protobuf/wrappers.protogoogle" +
                ".protobuf\"\nDoubleValue\r\nvalue (\"" +
                "\n\nFloatValue\r\nvalue (\"\n\nInt64Val" +
                "ue\r\nvalue (\"\nUInt64Value\r\nvalu" +
                "e (\"\n\nInt32Value\r\nvalue (\"\n" +
                "UInt32Value\r\nvalue (\r\"\n\tBoolValue" +
                "\r\nvalue (\b\"\nStringValue\r\nvalue" +
                " (\t\"\n\nBytesValue\r\nvalue (Bﾃ\nco" +
                "m.google.protobufB\rWrappersProtoPZ1goog" +
                "le.golang.org/protobuf/types/known/wrapp" +
                "erspb￸ﾢGPBﾪGoogle.Protobuf.WellKno" +
                "wnTypesbproto3"
        )

        FileDescriptor.buildFrom(
            descriptorData,
            listOf()
        )
    }
}

@Deprecated("use v1")
val DoubleValue.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[0]

@Deprecated("use v1")
val FloatValue.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[1]

@Deprecated("use v1")
val Int64Value.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[2]

@Deprecated("use v1")
val UInt64Value.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[3]

@Deprecated("use v1")
val Int32Value.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[4]

@Deprecated("use v1")
val UInt32Value.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[5]

@Deprecated("use v1")
val BoolValue.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[6]

@Deprecated("use v1")
val StringValue.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[7]

@Deprecated("use v1")
val BytesValue.Deserializer.descriptor: Descriptor
    get() = WrappersProto.descriptor.messageTypes[8]
