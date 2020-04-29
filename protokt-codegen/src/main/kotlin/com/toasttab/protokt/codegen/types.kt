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
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.ext.Protokt
import com.toasttab.protokt.rt.PType

sealed class Type {
    abstract val name: String
    abstract val type: String
}

class MessageType(
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

class EnumType(
    override val name: String,
    override val type: String,
    val options: EnumOptions,
    val values: List<Value>,
    val index: Int
) : Type() {
    class Value(
        val number: Int,
        val name: String,
        val valueName: String,
        val options: EnumValueOptions,
        val index: Int
    )
}

class EnumOptions(
    val default: DescriptorProtos.EnumOptions,
    val protokt: Protokt.ProtoktEnumOptions
)

class EnumValueOptions(
    val default: DescriptorProtos.EnumValueOptions,
    val protokt: Protokt.ProtoktEnumValueOptions
)

class ServiceType(
    override val name: String,
    override val type: String,
    val methods: List<Method>,
    val deprecated: Boolean,
    val options: ServiceOptions
) : Type()

class ServiceOptions(
    val default: DescriptorProtos.ServiceOptions,
    val protokt: Protokt.ProtoktServiceOptions
)

class Method(
    val name: String,
    val inputType: String,
    val outputType: String,
    val clientStreaming: Boolean,
    val serverStreaming: Boolean,
    val deprecated: Boolean,
    val options: MethodOptions
)

class MethodOptions(
    val default: DescriptorProtos.MethodOptions,
    val protokt: Protokt.ProtoktMethodOptions
)

sealed class Field

class StandardField(
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

class FieldOptions(
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

class Oneof(
    val name: String,
    val fieldName: String,
    val fields: List<StandardField>,
    val fieldTypeNames: Map<String, String>,
    val nativeTypeName: String,
    val options: OneofOptions,
    val index: Int
) : Field()

class OneofOptions(
    val default: DescriptorProtos.OneofOptions,
    val protokt: Protokt.ProtoktOneofOptions
)

class ProtocolContext(
    val fdp: FileDescriptorProto,
    val params: Map<String, String>,
    val allPackagesByTypeName: Map<String, PPackage>
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

class FileDesc(
    val name: String,
    val packageName: String,
    val version: Int,
    val options: FileOptions,
    val context: PluginContext,
    val sourceCodeInfo: DescriptorProtos.SourceCodeInfo
)

class PluginContext(
    val classpath: List<String>,
    val respectJavaPackage: Boolean,
    val fileName: String,
    private val allPackagesByTypeName: Map<String, PPackage>
) {
    fun ppackage(typeName: String) =
        allPackagesByTypeName.getValue(typeName)
}

class FileOptions(
    val default: DescriptorProtos.FileOptions,
    val protokt: Protokt.ProtoktFileOptions
)

class Protocol(
    val desc: FileDesc,
    val types: List<Type>
)

// Interpreter Types
data class AnnotatedType(
    val rawType: Type,
    val code: Option<String> = None
)

data class TypeDesc(
    val desc: FileDesc,
    val type: AnnotatedType
)
