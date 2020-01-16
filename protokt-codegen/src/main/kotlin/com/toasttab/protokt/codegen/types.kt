/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.codegen

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto
import com.google.protobuf.DescriptorProtos.UninterpretedOption
import com.toasttab.protokt.ext.Protokt
import com.toasttab.protokt.rt.PType

sealed class Type {
    abstract val name: String
    abstract val type: String
}

data class MessageType(
    override val name: String,
    override val type: String,
    val fields: List<Field>,
    val nestedTypes: List<Type>,
    val mapEntry: Boolean,
    val options: MessageOptions,
    val index: Int,
    val fullProtobufTypeName: String
) : Type()

data class MessageOptions(
    val default: DescriptorProtos.MessageOptions,
    val protokt: Protokt.ProtoktMessageOptions
)

data class EnumType(
    override val name: String,
    override val type: String,
    val options: EnumOptions,
    val values: List<Value>,
    val index: Int
) : Type() {
    data class Value(
        val number: Int,
        val name: String,
        val valueName: String,
        val options: EnumValueOptions,
        val index: Int
    )
}

data class EnumOptions(
    val default: DescriptorProtos.EnumOptions,
    val protokt: Protokt.ProtoktEnumOptions
)

data class EnumValueOptions(
    val default: DescriptorProtos.EnumValueOptions,
    val protokt: Protokt.ProtoktEnumValueOptions
)

@Suppress("UNUSED")
data class ServiceType(
    override val name: String,
    override val type: String,
    val methods: List<MethodType>,
    val deprecated: Boolean,
    val unknownOpts: List<UninterpretedOption>
) : Type() {
    constructor(desc: ServiceDescriptorProto) : this(
        desc.name,
        "",
        desc.methodList?.map { MethodType(it) } ?: emptyList(),
        desc.options.deprecated,
        desc.options?.uninterpretedOptionList ?: emptyList())
}

data class MethodType(
    override val name: String,
    override val type: String,
    val inputType: Option<String>,
    val outputType: Option<String>,
    val inputStreaming: Boolean,
    val outputStreaming: Boolean,
    val deprecated: Boolean,
    val unknownOpts: List<UninterpretedOption>
) : Type() {
    constructor(desc: MethodDescriptorProto) : this(
        desc.name,
        "",
        Some(desc.inputType),
        Some(desc.outputType),
        desc.clientStreaming,
        desc.serverStreaming,
        desc.options.deprecated,
        desc.options?.uninterpretedOptionList ?: emptyList())
}

sealed class Field

data class StandardField(
    val number: Int,
    val name: String,
    val fieldName: String,
    val type: PType,
    val repeated: Boolean,
    val optional: Boolean,
    val packed: Boolean,
    val map: Boolean,
    val typeName: String,
    val nativeTypeName: Option<String>,
    val options: FieldOptions,
    val index: Int
) : Field()

data class FieldOptions(
    val default: DescriptorProtos.FieldOptions,
    val protokt: Protokt.ProtoktFieldOptions
)

val StandardField.wireFormat
    get() = when (type) {
        PType.BOOL,
        PType.ENUM,
        PType.INT32,
        PType.INT64,
        PType.SINT32,
        PType.SINT64,
        PType.UINT32,
        PType.UINT64 -> 0
        PType.DOUBLE,
        PType.FIXED64,
        PType.SFIXED64 -> 1
        PType.BYTES,
        PType.MESSAGE,
        PType.STRING -> 2
        PType.FLOAT,
        PType.FIXED32,
        PType.SFIXED32 -> 5
    }

data class OneOf(
    val name: String,
    val fieldName: String,
    val fields: List<StandardField>,
    val fieldTypeNames: Map<String, String>,
    val nativeTypeName: String,
    val options: OneofOptions,
    val index: Int
) : Field()

data class OneofOptions(
    val default: DescriptorProtos.OneofOptions,
    val protokt: Protokt.ProtoktOneofOptions
)

data class ProtocolContext(
    val fdp: FileDescriptorProto,
    val params: Map<String, String>
)

fun ProtocolContext.findLocal(
    name: String,
    parent: Option<DescriptorProto> = None
): Option<DescriptorProto> {
    val (typeList, typeName) = parent.fold(
        {
            fdp.messageTypeList.filterNotNull() to
                name.removePrefix(".${fdp.`package`}.")
        },
        {
            it.nestedTypeList.filterNotNull() to name
        })

    typeName.indexOf('.').let { idx ->
        if (idx == -1) return Option.fromNullable(
            typeList.find { it.name == typeName })
        return findLocal(
            typeName.substring(idx + 1),
            Option.fromNullable(
                typeList.find { it.name == typeName.substring(0, idx) }))
    }
}

data class FileDesc(
    val name: String,
    val packageName: Option<String>,
    val rtPackage: Option<String>,
    val version: Int,
    val options: Protokt.ProtoktFileOptions,
    val params: PluginParams,
    val sourceCodeInfo: DescriptorProtos.SourceCodeInfo
)

data class PluginParams(
    private val params: Map<String, String>
) {
    val classpath =
        params.getOrDefault("kotlin_extra_classpath", "")
            .split(";")
}

data class Protocol(
    val desc: FileDesc,
    val types: List<Type>
)

// Interpreter Types
data class AnnotatedType(
    val rawType: Type,
    val template: Option<Template> = None
)
data class TypeDesc(
    val desc: FileDesc,
    val type: AnnotatedType
)

// Template interface
interface Template {
    fun render(): String
}
