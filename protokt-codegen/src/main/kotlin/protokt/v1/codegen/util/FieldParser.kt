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
import com.squareup.kotlinpoet.ClassName
import protokt.v1.codegen.generate.Wrapper.wrapped
import protokt.v1.codegen.util.ErrorContext.withFieldName
import protokt.v1.google.protobuf.DescriptorProto
import protokt.v1.google.protobuf.FeatureSet
import protokt.v1.google.protobuf.FeatureSet.FieldPresence
import protokt.v1.google.protobuf.FieldDescriptorProto
import protokt.v1.google.protobuf.FieldDescriptorProto.Label.OPTIONAL
import protokt.v1.google.protobuf.FieldDescriptorProto.Label.REPEATED
import protokt.v1.google.protobuf.FileDescriptorProto
import protokt.v1.google.protobuf.OneofDescriptorProto
import protokt.v1.oneof
import protokt.v1.property
import protokt.v1.reflect.FieldType
import protokt.v1.reflect.typeName

internal class FieldParser(
    private val ctx: GeneratorContext,
    private val desc: DescriptorProto,
    private val enclosingMessages: List<String>,
    private val keyWrap: String?,
    private val valueWrap: String?
) {
    fun toFields(): List<Field> {
        val generatedOneofIndices = mutableSetOf<Int>()
        val fields = mutableListOf<Field>()

        desc.field.forEachIndexed { idx, t ->
            withFieldName(t.name.orEmpty()) {
                if (t.type != FieldDescriptorProto.Type.GROUP) {
                    t.oneofIndex?.let { oneofIndex ->
                        if (oneofIndex !in generatedOneofIndices) {
                            generatedOneofIndices.add(oneofIndex)
                            fields.add(toOneof(idx, desc, desc.oneofDecl[oneofIndex], t, fields))
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
        if (field.proto3Optional == true) {
            return toStandard(idx, field)
        }

        val oneofFieldDescriptors =
            desc.field.filter { it.oneofIndex != null && it.oneofIndex == field.oneofIndex }

        val oneofStdFields =
            oneofFieldDescriptors.mapIndexed { fdpIdx, fdp ->
                toStandard(idx + fdpIdx, fdp, true)
            }

        val fieldTypeNames =
            oneofStdFields.associate {
                it.fieldName to LOWER_CAMEL.to(UPPER_CAMEL, it.fieldName)
            }

        val name = LOWER_UNDERSCORE.to(UPPER_CAMEL, oneof.name.orEmpty())

        return Oneof(
            name = name,
            className = ClassName(ctx.kotlinPackage, enclosingMessages + desc.name.orEmpty() + name),
            fieldTypeNames = fieldTypeNames,
            fieldName = LOWER_UNDERSCORE.to(LOWER_CAMEL, oneof.name.orEmpty()),
            fields = oneofStdFields,
            options = OneofOptions(
                oneof.options ?: protokt.v1.google.protobuf.OneofOptions {},
                oneof.options?.oneof ?: protokt.v1.OneofOptions {}
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
        val fieldType = FieldType.from((fdp.type ?: FieldDescriptorProto.Type.STRING).value)
        val protoktOptions = fdp.options?.property ?: protokt.v1.FieldOptions {}
        val repeated = fdp.label == REPEATED
        val mapEntry = mapEntry(fdp, protoktOptions)
        if (mapEntry == null) {
            require(protoktOptions.keyWrap.isEmpty()) {
                "key wrap is not applicable to non-map-entry"
            }
            require(protoktOptions.valueWrap.isEmpty()) {
                "value wrap is not applicable to non-map-entry"
            }
        } else {
            require(protoktOptions.wrap.isEmpty()) {
                "wrap is not applicable to map entry"
            }
        }
        val optional = optional(fdp)
        val packed = packed(fieldType, fdp)
        val number = fdp.number ?: 0
        val tag =
            if (repeated && packed) {
                Tag.Packed(number)
            } else {
                Tag.Unpacked(number, fieldType.wireType)
            }

        val result = StandardField(
            number = number,
            tag = tag,
            type = fieldType,
            repeated = repeated,
            optional = !withinOneof && optional,
            packed = packed,
            mapEntry = mapEntry,
            fieldName = camelCaseFieldName(fdp.name.orEmpty()),
            options = FieldOptions(
                fdp.options ?: protokt.v1.google.protobuf.FieldOptions {},
                protoktOptions,
                when {
                    keyWrap != null && idx == 0 -> keyWrap
                    valueWrap != null && idx == 1 -> valueWrap
                    else -> protoktOptions.wrap.takeIf { it.isNotEmpty() }
                }
            ),
            protoTypeName = fdp.typeName.orEmpty(),
            className = ClassName.bestGuess(typeName(fdp.typeName.orEmpty(), fieldType)),
            index = idx
        )

        if (protoktOptions.generateNonNullAccessor) {
            validateNonNullOption(fdp, result, withinOneof, optional)
        }

        return result
    }

    private fun mapEntry(fdp: FieldDescriptorProto, options: protokt.v1.FieldOptions) =
        if (fdp.label == REPEATED && fdp.type == FieldDescriptorProto.Type.MESSAGE) {
            findMapEntry(ctx.fdp, fdp.typeName.orEmpty())
                ?.takeIf { it.options?.mapEntry == true }
                ?.let { entry ->
                    MessageParser(
                        ctx,
                        -1,
                        entry,
                        enclosingMessages + desc.name.orEmpty(),
                        options.keyWrap.takeIf { it.isNotEmpty() },
                        options.valueWrap.takeIf { it.isNotEmpty() }
                    ).toMessage()
                }
        } else {
            null
        }

    private fun findMapEntry(
        fdp: FileDescriptorProto,
        name: String,
        parent: DescriptorProto? = null
    ): DescriptorProto? {
        val (typeList, typeName) =
            if (parent == null) {
                Pair(
                    fdp.messageType,
                    name.removePrefix(".${fdp.`package`.orEmpty()}.")
                )
            } else {
                parent.nestedType to name
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
        fdp.label != REPEATED &&
            when {
                ctx.proto2 -> fdp.label == OPTIONAL
                ctx.proto3 -> fdp.proto3Optional == true
                ctx.edition2023 -> optional(ctx.fileOptions.default.features, fdp.options?.features)
                else -> error("unexpected edition/syntax")
            }

    private fun optional(fileFeatures: FeatureSet?, fieldFeatures: FeatureSet?) =
        if (fileFeatures?.fieldPresence == FieldPresence.EXPLICIT || fileFeatures?.fieldPresence == null) {
            fieldFeatures?.fieldPresence !in setOf(FieldPresence.IMPLICIT, FieldPresence.LEGACY_REQUIRED)
        } else {
            fieldFeatures?.fieldPresence == FieldPresence.EXPLICIT
        }

    private fun packed(type: FieldType, fdp: FieldDescriptorProto) =
        type.packable &&
            // marginal support for proto2
            (
                (ctx.proto2 && fdp.options?.packed == true) ||
                    // packed if: proto3 and `packed` isn't set, or proto3
                    // and `packed` is true. If proto3, only explicitly
                    // setting `packed` to false disables packing, since
                    // the default value for an unset boolean is false.
                    (ctx.proto3 && (fdp.options?.packed == null || fdp.options?.packed == true))
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
                FieldType.Enum, FieldType.Message -> fdp.typeName.orEmpty()
                else -> field.type.typeName()
            }

        require(!optional) {
            "(protokt.property).generate_non_null_accessor is not applicable to optional fields " +
                "and is inapplicable to optional $typeName"
        }
        require(!withinOneof) {
            "(protokt.property).generate_non_null_accessor is only applicable to top level types " +
                "and is inapplicable to oneof field $typeName"
        }

        require((field.type == FieldType.Message && !field.repeated) || field.wrapped) {
            "(protokt.property).generate_non_null_accessor is only applicable to message types or wrapped types " +
                "and is inapplicable to non-message " +
                when {
                    field.mapEntry != null ->
                        "map<${name(field.mapKey)}, ${name(field.mapValue)}>"

                    field.repeated ->
                        "repeated $typeName"

                    else ->
                        field.type.typeName()
                }
        }
    }

    // Adapted from https://github.com/protocolbuffers/protobuf/blob/2fe8aaa15868c8d419ea6532cecf7f0045b3779b/src/google/protobuf/compiler/java/helpers.cc#L202
    // The grpc-kotlin compiler doesn't get this right (as of 1.4.1) with ProtoFieldName and crashes on conformance
    // field names that begin with underscores. They never actually seem to use that utility, however.
    private fun camelCaseFieldName(name: String): String {
        val fieldName = underscoresToCamelCase(name)
        return if (fieldName[0].isDigit()) {
            "_$fieldName"
        } else {
            fieldName
        }
    }

    private fun underscoresToCamelCase(input: String): String {
        val result = StringBuilder()
        var capNext = false

        for (char in input) {
            when {
                char.isLowerCase() -> {
                    if (capNext) {
                        result.append(char.uppercaseChar())
                    } else {
                        result.append(char)
                    }
                    capNext = false
                }

                char.isUpperCase() -> {
                    if (result.isEmpty() && !capNext) {
                        result.append(char.lowercaseChar())
                    } else {
                        result.append(char)
                    }
                    capNext = false
                }

                char.isDigit() -> {
                    result.append(char)
                    capNext = true
                }

                else -> {
                    capNext = true
                }
            }
        }

        // Add a trailing "_" if the name should be altered.
        if (input.last() == '#') {
            result.append('_')
        }

        return result.toString()
    }
}
