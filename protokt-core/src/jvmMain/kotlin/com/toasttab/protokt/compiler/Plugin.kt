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

package com.toasttab.protokt.compiler

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.DescriptorProtos
import com.toasttab.protokt.EnumDescriptor
import com.toasttab.protokt.FileDescriptor

@Deprecated("use v1")
object PluginProtos {
    val descriptor: FileDescriptor by lazy {
        val descriptorData = arrayOf(
            "\n%google/protobuf/compiler/plugin.proto" +
                "google.protobuf.compiler google/protob" +
                "uf/descriptor.proto\"F\nVersion\r\nmajor" +
                " (\r\nminor (\r\npatch (\ns" +
                "uffix (\t\"ﾺ\nCodeGeneratorRequest\n" +
                "file_to_generate (\t\n\tparameter (" +
                "\t8\n\nproto_file (2\$.google.protobuf." +
                "FileDescriptorProto;\ncompiler_version" +
                " (2!.google.protobuf.compiler.Version" +
                "\"￁\nCodeGeneratorResponse\r\nerror (" +
                "\t\nsupported_features (B\nfile " +
                "(24.google.protobuf.compiler.CodeGener" +
                "atorResponse.File\nFile\nname (\t" +
                "\ninsertion_point (\t\ncontent (" +
                "\t?\ngenerated_code_info (2\".google." +
                "protobuf.GeneratedCodeInfo\"8\nFeature\n" +
                "FEATURE_NONE \nFEATURE_PROTO3_OPTION" +
                "ALBW\ncom.google.protobuf.compilerBPl" +
                "uginProtosZ)google.golang.org/protobuf/t" +
                "ypes/pluginpb"
        )

        FileDescriptor.buildFrom(
            descriptorData,
            listOf(
                DescriptorProtos.descriptor
            )
        )
    }
}

@Deprecated("use v1")
val CodeGeneratorResponse.Feature.Deserializer.descriptor: EnumDescriptor
    get() = CodeGeneratorResponse.descriptor.enumTypes[0]

@Deprecated("use v1")
val Version.Deserializer.descriptor: Descriptor
    get() = PluginProtos.descriptor.messageTypes[0]

@Deprecated("use v1")
val CodeGeneratorRequest.Deserializer.descriptor: Descriptor
    get() = PluginProtos.descriptor.messageTypes[1]

@Deprecated("use v1")
val CodeGeneratorResponse.Deserializer.descriptor: Descriptor
    get() = PluginProtos.descriptor.messageTypes[2]

@Deprecated("use v1")
val CodeGeneratorResponse.File.Deserializer.descriptor: Descriptor
    get() = CodeGeneratorResponse.descriptor.nestedTypes[0]
