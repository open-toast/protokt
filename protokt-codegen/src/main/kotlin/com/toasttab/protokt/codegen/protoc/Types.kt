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

package com.toasttab.protokt.codegen.protoc

import arrow.core.None
import arrow.core.Option
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.ext.Protokt
import com.toasttab.protokt.shared.KOTLIN_EXTRA_CLASSPATH
import com.toasttab.protokt.shared.RESPECT_JAVA_PACKAGE
import com.toasttab.protokt.util.getProtoktVersion

sealed class TopLevelType

class Message(
    val name: String,
    val fields: List<Field>,
    val nestedTypes: List<TopLevelType>,
    val mapEntry: Boolean,
    val options: MessageOptions,
    val index: Int,
    val fullProtobufTypeName: String
) : TopLevelType()

data class MessageOptions(
    val default: DescriptorProtos.MessageOptions,
    val protokt: Protokt.ProtoktMessageOptions
)

class Enum(
    val name: String,
    val options: EnumOptions,
    val values: List<Value>,
    val index: Int
) : TopLevelType() {
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

class Service(
    val name: String,
    val type: String,
    val methods: List<Method>,
    val deprecated: Boolean,
    val options: ServiceOptions
) : TopLevelType()

class ServiceOptions(
    val default: DescriptorProtos.ServiceOptions,
    val protokt: Protokt.ProtoktServiceOptions
)

class Method(
    val name: String,
    val inputType: PClass,
    val outputType: PClass,
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
    val type: FieldType,
    val typePClass: PClass,
    val repeated: Boolean,
    val optional: Boolean,
    val packed: Boolean,
    val map: Boolean,
    val protoTypeName: String,
    val options: FieldOptions,
    val index: Int
) : Field()

class FieldOptions(
    val default: DescriptorProtos.FieldOptions,
    val protokt: Protokt.ProtoktFieldOptions
)

val StandardField.wireFormat
    get() =
        when (type) {
            FieldType.BOOL,
            FieldType.ENUM,
            FieldType.INT32,
            FieldType.INT64,
            FieldType.SINT32,
            FieldType.SINT64,
            FieldType.UINT32,
            FieldType.UINT64 -> 0
            FieldType.DOUBLE,
            FieldType.FIXED64,
            FieldType.SFIXED64 -> 1
            FieldType.BYTES,
            FieldType.MESSAGE,
            FieldType.STRING -> 2
            FieldType.FLOAT,
            FieldType.FIXED32,
            FieldType.SFIXED32 -> 5
        }

class Oneof(
    val name: String,
    val fieldName: String,
    val fields: List<StandardField>,
    val fieldTypeNames: Map<String, String>,
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

fun ProtocolContext.fileName() =
    fdp.name

fun ProtocolContext.classpath() =
    params.getOrDefault(KOTLIN_EXTRA_CLASSPATH, "").split(";")

fun respectJavaPackage(params: Map<String, String>) =
    params.getValue(RESPECT_JAVA_PACKAGE).toBoolean()

fun ProtocolContext.respectJavaPackage() =
    respectJavaPackage(params)

fun ProtocolContext.version() =
    getProtoktVersion(ProtocolContext::class)

fun ProtocolContext.ppackage(typeName: String) =
    allPackagesByTypeName.getValue(typeName)

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
    val context: ProtocolContext,
    val sourceCodeInfo: DescriptorProtos.SourceCodeInfo
)

class FileOptions(
    val default: DescriptorProtos.FileOptions,
    val protokt: Protokt.ProtoktFileOptions
)

class Protocol(
    val desc: FileDesc,
    val types: List<TopLevelType>
)

class AnnotatedType(
    val rawType: TopLevelType,
    val code: Option<String> = None
)

class TypeDesc(
    val desc: FileDesc,
    val type: AnnotatedType
)

sealed class Tag(val value: Int) : Comparable<Tag> {
    class Packed(
        number: Int
    ) : Tag((number shl 3) or 2)

    class Unpacked(
        number: Int,
        wireFormat: Int
    ) : Tag((number shl 3) or wireFormat)

    override fun compareTo(other: Tag) =
        value.compareTo(other.value)
}
