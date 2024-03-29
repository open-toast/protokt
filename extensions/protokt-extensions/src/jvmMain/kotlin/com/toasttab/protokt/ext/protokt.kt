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

// Generated by protokt version 0.12.1. Do not modify.
// Source: protokt/protokt.proto
package com.toasttab.protokt.ext

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.DescriptorProtos
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@KtGeneratedFileDescriptor
@Deprecated("use v1")
object ProtoktProto {
    val descriptor: FileDescriptor by lazy {
        val descriptorData = arrayOf(
            "\nprotokt/protokt.protoprotokt google" +
                    "/protobuf/descriptor.proto\"Q\nProtoktFil" +
                    "eOptions\nkotlin_package (\t#\nfile" +
                    "_descriptor_object_name (\t\"H\nProtokt" +
                    "MessageOptions\n\nimplements (\t\nde" +
                    "precation_message (\t\"ﾍ\nProtoktField" +
                    "Options\n\bnon_null (\b\nwrap (\t" +
                    "\nbytes_slice (\b\ndeprecation_mess" +
                    "age (\t\n\bkey_wrap (\t\n\nvalue_wra" +
                    "p (\t\"X\nProtoktOneofOptions\n\bnon_nu" +
                    "ll (\b\n\nimplements (\t\ndeprecat" +
                    "ion_message (\t\"1\nProtoktEnumOptions" +
                    "\ndeprecation_message (\t\"6\nProtoktE" +
                    "numValueOptions\ndeprecation_message" +
                    " (\t\"\nProtoktServiceOptions\"O\nProtokt" +
                    "MethodOptions\nrequest_marshaller (" +
                    "\t\nresponse_marshaller (\t:N\nfile" +
                    ".google.protobuf.FileOptionsﾰ\b (2.pr" +
                    "otokt.ProtoktFileOptionsRfile:V\nclass" +
                    ".google.protobuf.MessageOptionsﾰ\b (2" +
                    ".protokt.ProtoktMessageOptionsRclass:X" +
                    "\n\bproperty.google.protobuf.FieldOption" +
                    "sﾰ\b (2.protokt.ProtoktFieldOptionsR\b" +
                    "property:R\noneof.google.protobuf.Oneo" +
                    "fOptionsﾰ\b (2.protokt.ProtoktOneofOp" +
                    "tionsRoneof:N\nenum.google.protobuf.E" +
                    "numOptionsﾰ\b (2.protokt.ProtoktEnumO" +
                    "ptionsRenum:c\n\nenum_value!.google.prot" +
                    "obuf.EnumValueOptionsﾰ\b (2 .protokt.P" +
                    "rotoktEnumValueOptionsR\tenumValue:Z\nser" +
                    "vice.google.protobuf.ServiceOptionsﾰ\b" +
                    " (2.protokt.ProtoktServiceOptionsRse" +
                    "rvice:V\nmethod.google.protobuf.Method" +
                    "Optionsﾰ\b (2.protokt.ProtoktMethodOp" +
                    "tionsRmethodB(\ncom.toasttab.protokt.ex" +
                    "tBProtoktProtobproto3"
        )

        FileDescriptor.buildFrom(
            descriptorData,
            listOf(
                DescriptorProtos.descriptor
            )
        )
    }
}

val ProtoktFileOptions.Deserializer.descriptor: Descriptor
    get() = ProtoktProto.descriptor.messageTypes[0]

val ProtoktMessageOptions.Deserializer.descriptor: Descriptor
    get() = ProtoktProto.descriptor.messageTypes[1]

val ProtoktFieldOptions.Deserializer.descriptor: Descriptor
    get() = ProtoktProto.descriptor.messageTypes[2]

val ProtoktOneofOptions.Deserializer.descriptor: Descriptor
    get() = ProtoktProto.descriptor.messageTypes[3]

val ProtoktEnumOptions.Deserializer.descriptor: Descriptor
    get() = ProtoktProto.descriptor.messageTypes[4]

val ProtoktEnumValueOptions.Deserializer.descriptor: Descriptor
    get() = ProtoktProto.descriptor.messageTypes[5]

val ProtoktServiceOptions.Deserializer.descriptor: Descriptor
    get() = ProtoktProto.descriptor.messageTypes[6]

val ProtoktMethodOptions.Deserializer.descriptor: Descriptor
    get() = ProtoktProto.descriptor.messageTypes[7]
