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
object TypeProto {
    val descriptor: FileDescriptor by lazy {
        val descriptorData = arrayOf(
            "\ngoogle/protobuf/type.protogoogle.pro" +
                "tobufgoogle/protobuf/any.proto\$google" +
                "/protobuf/source_context.proto\"ￗ\nType" +
                "\nname (\t&\nfields (2.google.p" +
                "rotobuf.Field\noneofs (\t(\noptions" +
                " (2.google.protobuf.Option6\nsourc" +
                "e_context (2.google.protobuf.Source" +
                "Context\'\nsyntax (2.google.protobu" +
                "f.Syntax\"ￕ\nField)\nkind (2.googl" +
                "e.protobuf.Field.Kind7\ncardinality " +
                "(2\".google.protobuf.Field.Cardinality" +
                "\nnumber (\nname (\t\n\btype_url" +
                " (\t\noneof_index (\npacked\b " +
                "(\b(\noptions\t (2.google.protobuf.O" +
                "ption\n\tjson_name\n (\t\n\rdefault_valu" +
                "e (\t\"￈\nKind\nTYPE_UNKNOWN \nTY" +
                "PE_DOUBLE\n\nTYPE_FLOAT\n\nTYPE_INT6" +
                "4\nTYPE_UINT64\n\nTYPE_INT32\n" +
                "TYPE_FIXED64\nTYPE_FIXED32\r\n\tTYPE" +
                "_BOOL\b\nTYPE_STRING\t\n\nTYPE_GROUP\n" +
                "\nTYPE_MESSAGE\n\nTYPE_BYTES\nTY" +
                "PE_UINT32\r\r\n\tTYPE_ENUM\n\rTYPE_SFIXE" +
                "D32\n\rTYPE_SFIXED64\nTYPE_SINT32" +
                "\nTYPE_SINT64\"t\nCardinality\nCAR" +
                "DINALITY_UNKNOWN \nCARDINALITY_OPTION" +
                "AL\nCARDINALITY_REQUIRED\nCARDIN" +
                "ALITY_REPEATED\"ￎ\nEnum\nname (\t" +
                "-\n\tenumvalue (2.google.protobuf.Enu" +
                "mValue(\noptions (2.google.protobu" +
                "f.Option6\nsource_context (2.googl" +
                "e.protobuf.SourceContext\'\nsyntax (" +
                "2.google.protobuf.Syntax\"S\n\tEnumValue" +
                "\nname (\t\nnumber ((\noptions" +
                " (2.google.protobuf.Option\";\nOption" +
                "\nname (\t#\nvalue (2.google.p" +
                "rotobuf.Any*.\nSyntax\n\rSYNTAX_PROTO2 " +
                "\n\rSYNTAX_PROTO3B{\ncom.google.protob" +
                "ufB\tTypeProtoPZ-google.golang.org/proto" +
                "buf/types/known/typepb￸ﾢGPBﾪGoogle" +
                ".Protobuf.WellKnownTypesbproto3"
        )

        FileDescriptor.buildFrom(
            descriptorData,
            listOf(
                AnyProto.descriptor,
                SourceContextProto.descriptor
            )
        )
    }
}

@Deprecated("use v1")
val Syntax.Deserializer.descriptor: EnumDescriptor
    get() = TypeProto.descriptor.enumTypes[0]

@Deprecated("use v1")
val Field.Kind.Deserializer.descriptor: EnumDescriptor
    get() = Field.descriptor.enumTypes[0]

@Deprecated("use v1")
val Field.Cardinality.Deserializer.descriptor: EnumDescriptor
    get() = Field.descriptor.enumTypes[1]

@Deprecated("use v1")
val Type.Deserializer.descriptor: Descriptor
    get() = TypeProto.descriptor.messageTypes[0]

@Deprecated("use v1")
val Field.Deserializer.descriptor: Descriptor
    get() = TypeProto.descriptor.messageTypes[1]

@Deprecated("use v1")
val Enum_.Deserializer.descriptor: Descriptor
    get() = TypeProto.descriptor.messageTypes[2]

@Deprecated("use v1")
val EnumValue.Deserializer.descriptor: Descriptor
    get() = TypeProto.descriptor.messageTypes[3]

@Deprecated("use v1")
val Option.Deserializer.descriptor: Descriptor
    get() = TypeProto.descriptor.messageTypes[4]
