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
import com.toasttab.protokt.v1.ProtoktProtos
import protokt.v1.codegen.generate.Wrapper.wrapperRequiresNonNullOptionForNonNullity
import protokt.v1.codegen.util.ErrorContext.withFieldName
import protokt.v1.reflect.FieldType
import protokt.v1.reflect.typeName

internal class FieldParser(
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
        val fieldType = FieldType.from(fdp.type)
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

        val result = StandardField(
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
            className = ClassName.bestGuess(typeName(fdp.typeName, fieldType)),
            index = idx
        )

        if (protoktOptions.nonNull) {
            validateNonNullOption(fdp, result, withinOneof, optional)
        }

        return result
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

    private fun validateNonNullOption(
        fdp: FieldDescriptorProto,
        field: StandardField,
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
            when (field.type) {
                FieldType.Enum, FieldType.Message -> fdp.typeName
                else -> field.type.typeName()
            }

        require(!optional) {
            "(protokt.property).non_null is not applicable to optional fields " +
                "and is inapplicable to optional $typeName"
        }
        require(!withinOneof) {
            "(protokt.property).non_null is only applicable to top level types " +
                "and is inapplicable to oneof field $typeName"
        }

        require((field.type == FieldType.Message && !field.repeated) || field.wrapperRequiresNonNullOptionForNonNullity(ctx)) {
            "(protokt.property).non_null is only applicable to message types " +
                "and is inapplicable to non-message " +
                when {
                    field.mapEntry != null ->
                        "map<${name(field.mapEntry.key)}, ${name(field.mapEntry.value)}>"

                    field.repeated ->
                        "repeated $typeName"

                    else ->
                        field.type.typeName()
                }
        }
    }
}
