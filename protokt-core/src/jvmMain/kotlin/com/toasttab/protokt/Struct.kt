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
object StructProto {
    val descriptor: FileDescriptor by lazy {
        val descriptorData = arrayOf(
            "\ngoogle/protobuf/struct.protogoogle.p" +
                "rotobuf\"ﾄ\nStruct3\nfields (2#.goo" +
                "gle.protobuf.Struct.FieldsEntryE\nField" +
                "sEntry\nkey (\t%\nvalue (2.goo" +
                "gle.protobuf.Value:8\"￪\nValue0\n\nnull" +
                "_value (2.google.protobuf.NullValue" +
                "H \nnumber_value (H \nstring_val" +
                "ue (\tH \n\nbool_value (\bH /\nstru" +
                "ct_value (2.google.protobuf.StructH" +
                " 0\n\nlist_value (2.google.protobuf." +
                "ListValueH B\nkind\"3\n\tListValue&\nvalu" +
                "es (2.google.protobuf.Value*\n\tNull" +
                "Value\n\nNULL_VALUE B\ncom.google.prot" +
                "obufBStructProtoPZ/google.golang.org/p" +
                "rotobuf/types/known/structpb￸ﾢGPBﾪ" +
                "Google.Protobuf.WellKnownTypesbproto3"
        )

        FileDescriptor.buildFrom(
            descriptorData,
            listOf()
        )
    }
}

@Deprecated("use v1")
val NullValue.Deserializer.descriptor: EnumDescriptor
    get() = StructProto.descriptor.enumTypes[0]

@Deprecated("use v1")
val Struct.Deserializer.descriptor: Descriptor
    get() = StructProto.descriptor.messageTypes[0]

@Deprecated("use v1")
val Value.Deserializer.descriptor: Descriptor
    get() = StructProto.descriptor.messageTypes[1]

@Deprecated("use v1")
val ListValue.Deserializer.descriptor: Descriptor
    get() = StructProto.descriptor.messageTypes[2]
