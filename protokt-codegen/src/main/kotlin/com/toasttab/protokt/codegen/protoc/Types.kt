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
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.ext.Protokt
import com.toasttab.protokt.rt.computeTag

sealed class TopLevelType {
    abstract val name: String
}

class Message(
    override val name: String,
    val fields: List<Field>,
    val nestedTypes: List<TopLevelType>,
    val mapEntry: Boolean,
    val options: MessageOptions,
    val index: Int,
    val parentName: String?
) : TopLevelType()

data class MessageOptions(
    val default: DescriptorProtos.MessageOptions,
    val protokt: Protokt.ProtoktMessageOptions
)

class Enum(
    override val name: String,
    val options: EnumOptions,
    val values: List<Value>,
    val parentName: String?,
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
    val mapEntry: MapEntry?,
    val protoTypeName: String,
    val options: FieldOptions,
    val index: Int
) : Field() {
    val map
        get() = mapEntry != null
}

class MapEntry(
    val key: StandardField,
    val value: StandardField
)

class FieldOptions(
    val default: DescriptorProtos.FieldOptions,
    val protokt: Protokt.ProtoktFieldOptions
)

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

class FileDesc(
    val name: String,
    val packageName: String,
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
    ) : Tag(computeTag(number, 2))

    class Unpacked(
        number: Int,
        wireFormat: Int
    ) : Tag(computeTag(number, wireFormat))

    override fun compareTo(other: Tag) =
        value.compareTo(other.value)
}
