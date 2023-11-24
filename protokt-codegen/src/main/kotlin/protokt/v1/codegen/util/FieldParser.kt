/*
 * Copyright (c) 2022 Toast, Inc.
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

import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.common.base.CaseFormat.UPPER_CAMEL
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.v1.ProtoktProtos
import protokt.v1.codegen.util.ErrorContext.withFieldName

class FieldParser(
    private val ctx: GeneratorContext,
    private val desc: DescriptorProto,
    private val enclosingMessages: List<String>
) {
    fun toFields(): List<Field> {
        val generatedOneofIndices = mutableSetOf<Int>()
        val fields = mutableListOf<Field>()

        desc.fieldList.forEachIndexed { idx, t ->
            withFieldName(t.name) {
                if (t.type != Type.TYPE_GROUP) {
                    t.oneofIndex.takeIf { t.hasOneofIndex() }?.let { oneofIndex ->
                        if (oneofIndex !in generatedOneofIndices) {
                            generatedOneofIndices.add(oneofIndex)
                            fields.add(toOneof(idx, desc, desc.getOneofDecl(oneofIndex), t, fields))
                        }
                    } ?: fields.add(toStandard(idx, t))
                }
            }
        }

        return fields
    }

    private fun toOneof(
        idx: Int,
        desc: DescriptorProto,
        oneof: OneofDescriptorProto,
        field: FieldDescriptorProto,
        fields: List<Field>
    ): Field {
        if (field.proto3Optional) {
            return toStandard(idx, field)
        }

        val oneofFieldDescriptors =
            desc.fieldList.filter { it.hasOneofIndex() && it.oneofIndex == field.oneofIndex }

        val oneofStdFields =
            oneofFieldDescriptors.mapIndexed { fdpIdx, fdp ->
                toStandard(idx + fdpIdx, fdp, true)
            }

        val fieldTypeNames =
            oneofStdFields.associate {
                it.fieldName to LOWER_CAMEL.to(UPPER_CAMEL, it.fieldName)
            }

        val name = LOWER_UNDERSCORE.to(UPPER_CAMEL, oneof.name)

        return Oneof(
            name = name,
            className = ClassName(ctx.kotlinPackage, enclosingMessages + desc.name + name),
            fieldTypeNames = fieldTypeNames,
            fieldName = LOWER_UNDERSCORE.to(LOWER_CAMEL, oneof.name),
            fields = oneofStdFields,
            options = OneofOptions(
                oneof.options,
                oneof.options.getExtension(ProtoktProtos.oneof)
            ),
            // index relative to all oneofs in this message
            index = idx - fields.filterIsInstance<StandardField>().count()
        )
    }

    private fun toStandard(
        idx: Int,
        fdp: FieldDescriptorProto,
        withinOneof: Boolean = false
    ): StandardField {
        val fieldType = toFieldType(fdp.type)
        val protoktOptions = fdp.options.getExtension(ProtoktProtos.property)
        val repeated = fdp.label == LABEL_REPEATED
        val mapEntry = mapEntry(fdp)
        val optional = optional(fdp)
        val packed = packed(fieldType, fdp)
        val tag =
            if (repeated && packed) {
                Tag.Packed(fdp.number)
            } else {
                Tag.Unpacked(fdp.number, fieldType.wireType)
            }

        if (protoktOptions.nonNull) {
            validateNonNullOption(fdp, fieldType, repeated, mapEntry, withinOneof, optional)
        }

        return StandardField(
            number = fdp.number,
            tag = tag,
            type = fieldType,
            repeated = repeated,
            optional = !withinOneof && optional,
            packed = packed,
            mapEntry = mapEntry,
            fieldName = LOWER_UNDERSCORE.to(LOWER_CAMEL, fdp.name),
            options = FieldOptions(fdp.options, protoktOptions),
            protoTypeName = fdp.typeName,
            className = typeName(fdp.typeName, fieldType),
            index = idx
        )
    }

    private fun mapEntry(fdp: FieldDescriptorProto) =
        if (fdp.label == LABEL_REPEATED && fdp.type == Type.TYPE_MESSAGE) {
            findMapEntry(ctx.fdp, fdp.typeName)
                ?.takeIf { it.options.mapEntry }
                ?.let { resolveMapEntry(MessageParser(ctx, -1, it, enclosingMessages).toMessage()) }
        } else {
            null
        }

    private fun resolveMapEntry(m: Message) =
        MapEntry(
            (m.fields[0] as StandardField),
            (m.fields[1] as StandardField)
        )

    private fun findMapEntry(
        fdp: FileDescriptorProto,
        name: String,
        parent: DescriptorProto? = null
    ): DescriptorProto? {
        val (typeList, typeName) =
            if (parent == null) {
                Pair(
                    fdp.messageTypeList.filterNotNull(),
                    name.removePrefix(".${fdp.`package`}.")
                )
            } else {
                parent.nestedTypeList.filterNotNull() to name
            }

        typeName.indexOf('.').let { idx ->
            return if (idx == -1) {
                typeList.firstOrNull { it.name == typeName }
            } else {
                findMapEntry(
                    fdp,
                    typeName.substring(idx + 1),
                    typeList.firstOrNull { it.name == typeName.substring(0, idx) }
                )
            }
        }
    }

    private fun typeName(protoTypeName: String, fieldType: FieldType): ClassName {
        val fullyProtoQualified = protoTypeName.startsWith(".")

        return if (fullyProtoQualified) {
            requalifyProtoType(protoTypeName)
        } else {
            protoTypeName.let {
                if (it.isEmpty()) {
                    fieldType.protoktFieldType.asTypeName()
                } else {
                    ClassName.bestGuess(it)
                }
            }
        }
    }

    private fun optional(fdp: FieldDescriptorProto) =
        (fdp.label == LABEL_OPTIONAL && ctx.proto2) || fdp.proto3Optional

    private fun packed(type: FieldType, fdp: FieldDescriptorProto) =
        type.packable &&
            // marginal support for proto2
            (
                (ctx.proto2 && fdp.options.packed) ||
                    // packed if: proto3 and `packed` isn't set, or proto3
                    // and `packed` is true. If proto3, only explicitly
                    // setting `packed` to false disables packing, since
                    // the default value for an unset boolean is false.
                    (ctx.proto3 && (!fdp.options.hasPacked() || (fdp.options.hasPacked() && fdp.options.packed)))
                )
}

private fun toFieldType(type: Type) =
    when (type) {
        Type.TYPE_BOOL -> FieldType.Bool
        Type.TYPE_BYTES -> FieldType.Bytes
        Type.TYPE_DOUBLE -> FieldType.Double
        Type.TYPE_ENUM -> FieldType.Enum
        Type.TYPE_FIXED32 -> FieldType.Fixed32
        Type.TYPE_FIXED64 -> FieldType.Fixed64
        Type.TYPE_FLOAT -> FieldType.Float
        Type.TYPE_INT32 -> FieldType.Int32
        Type.TYPE_INT64 -> FieldType.Int64
        Type.TYPE_MESSAGE -> FieldType.Message
        Type.TYPE_SFIXED32 -> FieldType.SFixed32
        Type.TYPE_SFIXED64 -> FieldType.SFixed64
        Type.TYPE_SINT32 -> FieldType.SInt32
        Type.TYPE_SINT64 -> FieldType.SInt64
        Type.TYPE_STRING -> FieldType.String
        Type.TYPE_UINT32 -> FieldType.UInt32
        Type.TYPE_UINT64 -> FieldType.UInt64
        else -> error("Unknown type: $type")
    }

private fun validateNonNullOption(
    fdp: FieldDescriptorProto,
    type: FieldType,
    repeated: Boolean,
    mapEntry: MapEntry?,
    withinOneof: Boolean,
    optional: Boolean
) {
    fun FieldType.typeName() =
        this::class.simpleName!!.lowercase()

    fun name(field: StandardField) =
        if (field.type == FieldType.Enum) {
            field.protoTypeName
        } else {
            field.type.typeName()
        }

    val typeName =
        when (type) {
            FieldType.Enum, FieldType.Message -> fdp.typeName
            else -> type.typeName()
        }

    require(!optional) {
        "(protokt.property).non_null is not applicable to optional fields " +
            "and is inapplicable to optional $typeName"
    }
    require(!withinOneof) {
        "(protokt.property).non_null is only applicable to top level types " +
            "and is inapplicable to oneof field $typeName"
    }
    require(type == FieldType.Message && !repeated) {
        "(protokt.property).non_null is only applicable to message types " +
            "and is inapplicable to non-message " +
            when {
                mapEntry != null ->
                    "map<${name(mapEntry.key)}, ${name(mapEntry.value)}>"

                repeated ->
                    "repeated $typeName"

                else ->
                    type.typeName()
            }
    }
}
