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
import arrow.core.Some
import arrow.core.Tuple4
import arrow.core.firstOrNone
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.annotators.Annotator.rootGoogleProto
import com.toasttab.protokt.codegen.annotators.resolveMapEntry
import com.toasttab.protokt.codegen.impl.overrideGoogleProtobuf
import com.toasttab.protokt.codegen.impl.resolvePackage
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.ext.Protokt
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.plus

/**
 * Converts a message in the format used by protoc to the unannotated AST used by protokt.
 */
fun toProtocol(ctx: ProtocolContext): Protocol {
    val kotlinPackage = resolvePackage(ctx.fdp.fileOptions, ctx.fdp.`package`, ctx.respectJavaPackage)
    return Protocol(
        FileDesc(
            name = ctx.fdp.name,
            protoPackage = ctx.fdp.`package`,
            kotlinPackage = kotlinPackage,
            options = ctx.fdp.fileOptions,
            context = ctx,
            sourceCodeInfo = ctx.fdp.sourceCodeInfo
        ),
        types = toTypeList(
            ctx,
            kotlinPackage,
            ctx.fdp.enumTypeList,
            ctx.fdp.messageTypeList,
            ctx.fdp.serviceList
        )
    )
}

val FileDescriptorProto.fileOptions
    get() =
        FileOptions(
            options,
            options.getExtension(Protokt.file)
        )

private fun toFieldType(type: FieldDescriptorProto.Type) =
    when (type) {
        FieldDescriptorProto.Type.TYPE_BOOL -> FieldType.BOOL
        FieldDescriptorProto.Type.TYPE_BYTES -> FieldType.BYTES
        FieldDescriptorProto.Type.TYPE_DOUBLE -> FieldType.DOUBLE
        FieldDescriptorProto.Type.TYPE_ENUM -> FieldType.ENUM
        FieldDescriptorProto.Type.TYPE_FIXED32 -> FieldType.FIXED32
        FieldDescriptorProto.Type.TYPE_FIXED64 -> FieldType.FIXED64
        FieldDescriptorProto.Type.TYPE_FLOAT -> FieldType.FLOAT
        FieldDescriptorProto.Type.TYPE_INT32 -> FieldType.INT32
        FieldDescriptorProto.Type.TYPE_INT64 -> FieldType.INT64
        FieldDescriptorProto.Type.TYPE_MESSAGE -> FieldType.MESSAGE
        FieldDescriptorProto.Type.TYPE_SFIXED32 -> FieldType.SFIXED32
        FieldDescriptorProto.Type.TYPE_SFIXED64 -> FieldType.SFIXED64
        FieldDescriptorProto.Type.TYPE_SINT32 -> FieldType.SINT32
        FieldDescriptorProto.Type.TYPE_SINT64 -> FieldType.SINT64
        FieldDescriptorProto.Type.TYPE_STRING -> FieldType.STRING
        FieldDescriptorProto.Type.TYPE_UINT32 -> FieldType.UINT32
        FieldDescriptorProto.Type.TYPE_UINT64 -> FieldType.UINT64
        else -> error("Unknown type: $type")
    }

private fun toTypeList(
    ctx: ProtocolContext,
    pkg: String,
    enums: List<EnumDescriptorProto>,
    messages: List<DescriptorProto>,
    services: List<ServiceDescriptorProto> = emptyList(),
    enclosingMessages: List<String> = emptyList()
): PersistentList<TopLevelType> =
    enums.foldIndexed(
        Pair(persistentSetOf<String>(), persistentListOf<TopLevelType>())
    ) { idx, acc, t ->
        val e = toEnum(idx, t, acc.first, enclosingMessages, pkg)
        Pair(acc.first + e.name, acc.second + e)
    }.second +

        messages.foldIndexed(
            Pair(persistentSetOf<String>(), persistentListOf<TopLevelType>())
        ) { idx, acc, t ->
            val m = toMessage(idx, ctx, pkg, t, acc.first, enclosingMessages)
            Pair(acc.first + m.name, acc.second + m)
        }.second +

        services.foldIndexed(
            Pair(persistentSetOf<String>(), persistentListOf<TopLevelType>())
        ) { idx, acc, t ->
            val s = toService(idx, t, ctx, acc.first)
            Pair(acc.first + s.type, acc.second + s)
        }.second

private fun toEnum(
    idx: Int,
    desc: EnumDescriptorProto,
    names: PersistentSet<String>,
    enclosingMessages: List<String>,
    pkg: String
): Enum {
    val typeName = newTypeNameFromCamel(desc.name, names)

    val enumTypeNamePrefixToStrip =
        (camelToUpperSnake(desc.name) + '_')
            .takeIf { prefix ->
                desc.valueList.all { it.name.startsWith(prefix) }
            }

    return Enum(
        name = typeName,
        values = desc.valueList.foldIndexed(
            Pair(
                names + typeName,
                persistentListOf<Enum.Value>()
            )
        ) { enumIdx, acc, t ->
            val n = newEnumValueName(enumTypeNamePrefixToStrip, t.name, acc.first)
            val v =
                Enum.Value(
                    t.number,
                    t.name,
                    n,
                    EnumValueOptions(
                        t.options,
                        t.options.getExtension(Protokt.enumValue)
                    ),
                    enumIdx
                )
            Pair(acc.first + n, acc.second + v)
        }.second,
        index = idx,
        options = EnumOptions(
            desc.options,
            desc.options.getExtension(Protokt.enum_)
        ),
        typeName = ClassName(pkg, enclosingMessages + typeName),
        deserializerTypeName = ClassName(pkg, enclosingMessages + typeName + "Deserializer")
    )
}

private fun toMessage(
    idx: Int,
    ctx: ProtocolContext,
    pkg: String,
    desc: DescriptorProto,
    names: Set<String>,
    enclosingMessages: List<String>
): Message {
    val typeName = newTypeNameFromPascal(desc.name, names)
    val fieldList = toFields(ctx, pkg, desc, enclosingMessages + typeName, names + typeName)
    return Message(
        name = typeName,
        fields = fieldList.sortedBy {
            when (it) {
                is StandardField -> it
                is Oneof -> it.fields.first()
            }.number
        },
        nestedTypes = toTypeList(ctx, pkg, desc.enumTypeList, desc.nestedTypeList, enclosingMessages = enclosingMessages + typeName),
        mapEntry = desc.options?.mapEntry == true,
        options = MessageOptions(
            desc.options,
            desc.options.getExtension(Protokt.class_)
        ),
        index = idx,
        fullProtobufTypeName = "${ctx.fdp.`package`}.${desc.name}",
        typeName = ClassName(pkg.toString(), enclosingMessages + typeName),
        deserializerTypeName = ClassName(pkg.toString(), enclosingMessages + typeName + "Deserializer"),
        dslTypeName = ClassName(pkg.toString(), enclosingMessages + typeName + "${typeName}Dsl")
    )
}

private fun toService(
    idx: Int,
    desc: ServiceDescriptorProto,
    ctx: ProtocolContext,
    names: Set<String>
) =
    Service(
        name = desc.name,
        type = newTypeNameFromPascal(desc.name, names),
        methods = desc.methodList.map { toMethod(it, ctx) },
        deprecated = desc.options.deprecated,
        options = ServiceOptions(
            desc.options,
            desc.options.getExtension(Protokt.service)
        ),
        index = idx
    )

private fun toMethod(
    desc: DescriptorProtos.MethodDescriptorProto,
    ctx: ProtocolContext
) =
    Method(
        desc.name,
        requalifyProtoType(desc.inputType, ctx),
        requalifyProtoType(desc.outputType, ctx),
        desc.clientStreaming,
        desc.serverStreaming,
        desc.options.deprecated,
        MethodOptions(
            desc.options,
            desc.options.getExtension(Protokt.method)
        )
    )

private fun toFields(
    ctx: ProtocolContext,
    pkg: String,
    desc: DescriptorProto,
    enclosingMessages: List<String>,
    typeNames: Set<String>,
    ids: Set<Int> = persistentSetOf()
): PersistentList<Field> =
    desc.fieldList.foldIndexed(
        Tuple4(
            typeNames,
            ids,
            persistentSetOf<String>(),
            persistentListOf<Field>()
        )
    ) { idx, acc, t ->
        when (t.type) {
            FieldDescriptorProto.Type.TYPE_GROUP -> acc
            else -> {
                val i = if (t.hasOneofIndex()) Some(t.oneofIndex) else None
                i.fold({
                    val f = toStandard(idx, ctx, pkg, t, emptySet())
                    Tuple4(acc.first + f.fieldName, acc.second, acc.third, acc.fourth + f)
                }, {
                    if (it in acc.second || desc.oneofDeclList.isEmpty()) {
                        acc
                    } else {
                        val ood = desc.getOneofDecl(it)
                        Tuple4(
                            acc.first,
                            acc.second + it,
                            acc.third + ood.name,
                            acc.fourth + toOneof(idx, ctx, pkg, enclosingMessages, desc, ood, t, acc.first, acc.fourth)
                        )
                    }
                })
            }
        }
    }.fourth

private fun toOneof(
    idx: Int,
    ctx: ProtocolContext,
    pkg: String,
    enclosingMessages: List<String>,
    desc: DescriptorProto,
    oneof: OneofDescriptorProto,
    field: FieldDescriptorProto,
    typeNames: Set<String>,
    fields: PersistentList<Field>
): Field {
    val newName = newFieldName(oneof.name, typeNames)

    if (field.proto3Optional) {
        return toStandard(idx, ctx, pkg, field, typeNames)
    }

    val standardTuple = desc.fieldList.filter {
        it.hasOneofIndex() && it.oneofIndex == field.oneofIndex
    }.foldIndexed(
        Triple(
            persistentMapOf<String, String>(),
            persistentSetOf<String>(),
            persistentListOf<StandardField>()
        ),
        { oneofIdx, acc, t ->
            val ftn = newTypeNameFromCamel(t.name, acc.second)
            Triple(
                acc.first + (newFieldName(t.name, acc.second) to ftn),
                acc.second + ftn,
                acc.third + toStandard(idx + oneofIdx, ctx, pkg, t, emptySet(), true)
            )
        }
    )
    val name = newTypeNameFromCamel(oneof.name, typeNames)
    return Oneof(
        name = name,
        className = ClassName(pkg.toString(), enclosingMessages + name),
        fieldTypeNames = standardTuple.first,
        fieldName = newName,
        fields = standardTuple.third,
        options = OneofOptions(
            oneof.options,
            oneof.options.getExtension(Protokt.oneof)
        ),
        // index relative to all oneofs in this message
        index = idx - fields.filterIsInstance<StandardField>().count()
    )
}

private fun toStandard(
    idx: Int,
    ctx: ProtocolContext,
    pkg: String,
    fdp: FieldDescriptorProto,
    usedFieldNames: Set<String>,
    withinOneof: Boolean = false
): StandardField =
    toFieldType(fdp.type).let { type ->
        val protoktOptions = fdp.options.getExtension(Protokt.property)
        val repeated = fdp.label == LABEL_REPEATED
        val mapEntry = mapEntry(usedFieldNames, fdp, ctx, pkg)
        val optional = optional(fdp, ctx)

        if (protoktOptions.nonNull) {
            validateNonNullOption(fdp, type, repeated, mapEntry, withinOneof, optional)
        }

        StandardField(
            number = fdp.number,
            type = type,
            repeated = repeated,
            optional = !withinOneof && optional,
            packed = packed(type, fdp, ctx),
            mapEntry = mapEntry,
            fieldName = newFieldName(fdp.name, usedFieldNames),
            options = FieldOptions(fdp.options, protoktOptions),
            protoTypeName = fdp.typeName,
            className = typeName(fdp.typeName, ctx, type),
            index = idx
        )
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
        name.lowercase()

    fun name(field: StandardField) =
        if (field.type == FieldType.ENUM) {
            field.protoTypeName
        } else {
            field.type.typeName()
        }

    val typeName =
        when (type) {
            FieldType.ENUM, FieldType.MESSAGE -> fdp.typeName
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
    require(type == FieldType.MESSAGE && !repeated) {
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

private fun optional(fdp: FieldDescriptorProto, ctx: ProtocolContext) =
    (fdp.label == LABEL_OPTIONAL && ctx.proto2) ||
        fdp.proto3Optional

private fun packed(type: FieldType, fdp: FieldDescriptorProto, ctx: ProtocolContext) =
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

private fun mapEntry(
    usedFieldNames: Set<String>,
    fdp: FieldDescriptorProto,
    ctx: ProtocolContext,
    pkg: String
) =
    if (fdp.label == LABEL_REPEATED &&
        fdp.type == FieldDescriptorProto.Type.TYPE_MESSAGE
    ) {
        findMapEntry(ctx.fdp, fdp.typeName).filter { it.options.mapEntry }
            .fold(
                { null },
                {
                    resolveMapEntry(
                        toMessage(-1, ctx, pkg, it, usedFieldNames, emptyList())
                    )
                }
            )
    } else {
        null
    }

private fun findMapEntry(
    fdp: FileDescriptorProto,
    name: String,
    parent: Option<DescriptorProto> = None
): Option<DescriptorProto> {
    val (typeList, typeName) =
        parent.fold(
            {
                fdp.messageTypeList.filterNotNull() to
                    name.removePrefix(".${fdp.`package`}.")
            },
            { it.nestedTypeList.filterNotNull() to name }
        )

    typeName.indexOf('.').let { idx ->
        return if (idx == -1) {
            typeList.firstOrNone { it.name == typeName }
        } else {
            findMapEntry(
                fdp,
                typeName.substring(idx + 1),
                typeList.firstOrNone { it.name == typeName.substring(0, idx) }
            )
        }
    }
}

private fun typeName(
    protoTypeName: String,
    ctx: ProtocolContext,
    fieldType: FieldType
): ClassName {
    val fullyProtoQualified = protoTypeName.startsWith(".")

    return if (fullyProtoQualified) {
        requalifyProtoType(protoTypeName, ctx)
    } else {
        newTypeNameFromPascal(protoTypeName).let {
            if (it.isEmpty()) {
                fieldType.protoktFieldType.asTypeName()
            } else {
                ClassName.bestGuess(it)
            }
        }
    }
}

private fun requalifyProtoType(typeName: String, ctx: ProtocolContext): ClassName {
    val withOverriddenGoogleProtoPackage =
        ClassName.bestGuess(
            overrideGoogleProtobuf(typeName.removePrefix("."), rootGoogleProto)
        )

    val withOverriddenReservedName =
        ClassName(
            withOverriddenGoogleProtoPackage.packageName,
            withOverriddenGoogleProtoPackage.simpleNames.dropLast(1) +
                newTypeNameFromPascal(withOverriddenGoogleProtoPackage.simpleName)
        )

    return if (ctx.respectJavaPackage) {
        ClassName(
            ctx.allPackagesByTypeName.getValue(typeName).toString(),
            withOverriddenReservedName.simpleNames
        )
    } else {
        withOverriddenReservedName
    }
}
