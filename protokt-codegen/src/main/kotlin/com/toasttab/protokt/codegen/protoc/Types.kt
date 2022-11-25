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

import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.model.computeTag
import com.toasttab.protokt.ext.Protokt

sealed class TopLevelType {
    abstract val name: String
}

class Message(
    override val name: String,
    val typeName: ClassName,
    val deserializerTypeName: TypeName,
    val dslTypeName: TypeName,
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
    override val name: String,
    val typeName: TypeName,
    val deserializerTypeName: TypeName,
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
    override val name: String,
    val type: String,
    val methods: List<Method>,
    val deprecated: Boolean,
    val options: ServiceOptions,
    val index: Int
) : TopLevelType()

class ServiceOptions(
    val default: DescriptorProtos.ServiceOptions,
    val protokt: Protokt.ProtoktServiceOptions
)

class Method(
    val name: String,
    val inputType: ClassName,
    val outputType: ClassName,
    val clientStreaming: Boolean,
    val serverStreaming: Boolean,
    val deprecated: Boolean,
    val options: MethodOptions
)

class MethodOptions(
    val default: DescriptorProtos.MethodOptions,
    val protokt: Protokt.ProtoktMethodOptions
)

sealed class Field {
    abstract val fieldName: String
}

class StandardField(
    val number: Int,
    override val fieldName: String,
    val type: FieldType,
    val className: ClassName,
    val repeated: Boolean,
    val optional: Boolean,
    val packed: Boolean,
    val mapEntry: MapEntry?,
    val protoTypeName: String,
    val options: FieldOptions,
    val index: Int
) : Field() {
    val map
        get() = mapEntry != null
}

class Oneof(
    val name: String,
    val className: ClassName,
    override val fieldName: String,
    val fields: List<StandardField>,
    val fieldTypeNames: Map<String, String>,
    val options: OneofOptions,
    val index: Int
) : Field()

class MapEntry(
    val key: StandardField,
    val value: StandardField
)

class FieldOptions(
    val default: DescriptorProtos.FieldOptions,
    val protokt: Protokt.ProtoktFieldOptions
)

class OneofOptions(
    val default: DescriptorProtos.OneofOptions,
    val protokt: Protokt.ProtoktOneofOptions
)

class ProtoFileInfo(
    val name: String,
    val protoPackage: String,
    val kotlinPackage: String,
    val options: FileOptions,
    val context: GeneratorContext,
    val sourceCodeInfo: DescriptorProtos.SourceCodeInfo
)

class FileOptions(
    val default: DescriptorProtos.FileOptions,
    val protokt: Protokt.ProtoktFileOptions
)

class ProtoFileContents(
    val info: ProtoFileInfo,
    val types: List<TopLevelType>
)

class GeneratedType(
    val rawType: TopLevelType,
    val typeSpec: TypeSpec
)

sealed class Tag(val value: Int) : Comparable<Tag> {
    class Packed(
        number: Int
    ) : Tag(computeTag(number, 2))

    class Unpacked(
        number: Int,
        wireFormat: Int
    ) : Tag(computeTag(number, wireFormat))

    override fun compareTo(other: Tag) =
        value.compareTo(other.value)
}
