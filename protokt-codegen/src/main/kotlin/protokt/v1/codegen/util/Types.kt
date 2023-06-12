/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1.codegen.util

import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.toasttab.protokt.v1.ProtoktProto.ProtoktEnumOptions
import com.toasttab.protokt.v1.ProtoktProto.ProtoktEnumValueOptions
import com.toasttab.protokt.v1.ProtoktProto.ProtoktFieldOptions
import com.toasttab.protokt.v1.ProtoktProto.ProtoktFileOptions
import com.toasttab.protokt.v1.ProtoktProto.ProtoktMessageOptions
import com.toasttab.protokt.v1.ProtoktProto.ProtoktMethodOptions
import com.toasttab.protokt.v1.ProtoktProto.ProtoktOneofOptions
import com.toasttab.protokt.v1.ProtoktProto.ProtoktServiceOptions

sealed class TopLevelType

class Message(
    val className: ClassName,
    val deserializerClassName: ClassName,
    val dslClassName: ClassName,
    val fields: List<Field>,
    val nestedTypes: List<TopLevelType>,
    val mapEntry: Boolean,
    val options: MessageOptions,
    val index: Int,
    val fullProtobufTypeName: String
) : TopLevelType()

data class MessageOptions(
    val default: DescriptorProtos.MessageOptions,
    val protokt: ProtoktMessageOptions
)

class Enum(
    val className: ClassName,
    val deserializerClassName: ClassName,
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
    val protokt: ProtoktEnumOptions
)

class EnumValueOptions(
    val default: DescriptorProtos.EnumValueOptions,
    val protokt: ProtoktEnumValueOptions
)

class Service(
    val name: String,
    val methods: List<Method>,
    val deprecated: Boolean,
    val options: ServiceOptions,
    val index: Int
) : TopLevelType()

class ServiceOptions(
    val default: DescriptorProtos.ServiceOptions,
    val protokt: ProtoktServiceOptions
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
    val protokt: ProtoktMethodOptions
)

sealed class Field {
    abstract val fieldName: String
}

class StandardField(
    val number: Int,
    val tag: Tag,
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
    val protokt: ProtoktFieldOptions
)

class OneofOptions(
    val default: DescriptorProtos.OneofOptions,
    val protokt: ProtoktOneofOptions
)

class ProtoFileInfo(
    val context: GeneratorContext
) {
    val name = context.fdp.name
    val kotlinPackage = context.kotlinPackage
    val protoPackage = context.fdp.`package`
    val options = context.fileOptions
    val sourceCodeInfo = context.fdp.sourceCodeInfo
}

class FileOptions(
    val default: DescriptorProtos.FileOptions,
    val protokt: ProtoktFileOptions
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

    override fun toString() =
        value.toString()
}

private fun computeTag(fieldNumber: Int, wireType: Int) =
    (fieldNumber shl 3) or wireType