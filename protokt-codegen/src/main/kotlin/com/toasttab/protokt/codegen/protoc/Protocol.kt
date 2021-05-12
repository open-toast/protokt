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
import com.github.andrewoma.dexx.kollection.ImmutableList
import com.github.andrewoma.dexx.kollection.ImmutableSet
import com.github.andrewoma.dexx.kollection.immutableListOf
import com.github.andrewoma.dexx.kollection.immutableMapOf
import com.github.andrewoma.dexx.kollection.immutableSetOf
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto
import com.toasttab.protokt.codegen.impl.STAnnotator.rootGoogleProto
import com.toasttab.protokt.codegen.impl.overrideGoogleProtobuf
import com.toasttab.protokt.codegen.impl.resolveMapEntry
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.ext.Protokt

fun toProtocol(ctx: ProtocolContext) =
    Protocol(
        FileDesc(
            name = ctx.fdp.name,
            packageName = ctx.fdp.`package`,
            options = ctx.fdp.fileOptions,
            context = ctx,
            sourceCodeInfo = ctx.fdp.sourceCodeInfo
        ),
        types = toTypeList(
            ctx,
            ctx.fdp.enumTypeList,
            ctx.fdp.messageTypeList,
            ctx.fdp.serviceList
        )
    )

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
    enums: List<EnumDescriptorProto>,
    messages: List<DescriptorProto>,
    services: List<ServiceDescriptorProto> = emptyList()
): ImmutableList<TopLevelType> =
    enums.foldIndexed(
        Pair(immutableSetOf<String>(), immutableListOf<TopLevelType>())
    ) { idx, acc, t ->
        val e = toEnum(idx, t, acc.first)
        Pair(acc.first + e.name, acc.second + e)
    }.second +

    messages.foldIndexed(
        Pair(immutableSetOf<String>(), immutableListOf<TopLevelType>())
    ) { idx, acc, t ->
        val m = toMessage(idx, ctx, t, acc.first)
        Pair(acc.first + m.name, acc.second + m)
    }.second +

    services.fold(
        Pair(immutableSetOf<String>(), immutableListOf<TopLevelType>())
    ) { acc, t ->
        val s = toService(t, ctx, acc.first)
        Pair(acc.first + s.type, acc.second + s)
    }.second

private fun toEnum(
    idx: Int,
    desc: EnumDescriptorProto,
    names: ImmutableSet<String>
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
                immutableListOf<Enum.Value>()
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
        options =
            EnumOptions(
                desc.options,
                desc.options.getExtension(Protokt.enum_)
            )
    )
}

private fun toMessage(
    idx: Int,
    ctx: ProtocolContext,
    desc: DescriptorProto,
    names: Set<String>
): Message {
    val typeName = newTypeNameFromPascal(desc.name, names)
    val fieldList = toFields(ctx, desc, names + typeName)
    return Message(
        name = typeName,
        fields =
            fieldList.sortedBy {
                when (it) {
                    is StandardField -> it
                    is Oneof -> it.fields.first()
                }.number
            },
        nestedTypes = toTypeList(ctx, desc.enumTypeList, desc.nestedTypeList),
        mapEntry = desc.options?.mapEntry == true,
        options =
            MessageOptions(
                desc.options,
                desc.options.getExtension(Protokt.class_)
            ),
        index = idx,
        fullProtobufTypeName = "${ctx.fdp.`package`}.${desc.name}"
    )
}

private fun toService(
    desc: ServiceDescriptorProto,
    ctx: ProtocolContext,
    names: Set<String>
) =
    Service(
        name = desc.name,
        type = newTypeNameFromPascal(desc.name, names),
        methods = desc.methodList.map { toMethod(it, ctx) },
        deprecated = desc.options.deprecated,
        options =
            ServiceOptions(
                desc.options,
                desc.options.getExtension(Protokt.service)
            )
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
    desc: DescriptorProto,
    typeNames: Set<String>,
    ids: Set<Int> = immutableSetOf()
): ImmutableList<Field> =
    desc.fieldList.foldIndexed(
        Tuple4(
            typeNames,
            ids,
            immutableSetOf<String>(),
            immutableListOf<Field>()
        )
    ) { idx, acc, t ->
        when (t.type) {
            FieldDescriptorProto.Type.TYPE_GROUP -> acc
            else -> {
                val i = if (t.hasOneofIndex()) Some(t.oneofIndex) else None
                i.fold({
                    val f = toStandard(idx, ctx, t, emptySet())
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
                            acc.fourth + toOneof(idx, ctx, desc, ood, t, acc.first, acc.fourth)
                        )
                    }
                })
            }
        }
    }.fourth

private fun toOneof(
    idx: Int,
    ctx: ProtocolContext,
    desc: DescriptorProto,
    oneof: OneofDescriptorProto,
    field: FieldDescriptorProto,
    typeNames: Set<String>,
    fields: ImmutableList<Field>
): Field {
    val newName = newFieldName(oneof.name, typeNames)

    if (field.proto3Optional) {
        return toStandard(idx, ctx, field, typeNames)
    }

    val standardTuple = desc.fieldList.filter {
        it.hasOneofIndex() && it.oneofIndex == field.oneofIndex
    }.foldIndexed(
        Triple(
            immutableMapOf<String, String>(),
            immutableSetOf<String>(),
            immutableListOf<StandardField>()
        ), { oneofIdx, acc, t ->
            val ftn = newTypeNameFromCamel(t.name, acc.second)
            Triple(
                acc.first + (newFieldName(t.name, acc.second) to ftn),
                acc.second + ftn,
                acc.third + toStandard(idx + oneofIdx, ctx, t, emptySet(), true)
            )
    })
    return Oneof(
        name = newTypeNameFromCamel(oneof.name, typeNames),
        fieldTypeNames = standardTuple.first,
        fieldName = newName,
        fields = standardTuple.third,
        options =
            OneofOptions(
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
    fdp: FieldDescriptorProto,
    usedFieldNames: Set<String>,
    alwaysRequired: Boolean = false
): StandardField =
    toFieldType(fdp.type).let { type ->
        StandardField(
            number = fdp.number,
            name = newFieldName(fdp.name, usedFieldNames),
            type = type,
            repeated = fdp.label == LABEL_REPEATED,
            optional =
                !alwaysRequired &&
                    (fdp.label == LABEL_OPTIONAL && ctx.proto2) ||
                    fdp.proto3Optional,
            packed =
                type.packable &&
                    // marginal support for proto2
                    ((ctx.proto2 && fdp.options.packed) ||
                        // packed if: proto3 and `packed` isn't set, or proto3
                        // and `packed` is true. If proto3, only explicitly
                        // setting `packed` to false disables packing, since
                        // the default value for an unset boolean is false.
                        (ctx.proto3 &&
                            (!fdp.options.hasPacked() ||
                                (fdp.options.hasPacked() && fdp.options.packed)))),
            mapEntry =
                if (fdp.label == LABEL_REPEATED &&
                    fdp.type == FieldDescriptorProto.Type.TYPE_MESSAGE
                ) {
                    findMapEntry(ctx.fdp, fdp.typeName).filter { it.options.mapEntry }
                        .fold(
                            { null },
                            {
                                resolveMapEntry(
                                    toMessage(-1, ctx, it, usedFieldNames)
                                )
                            }
                        )
                } else {
                    null
                },
            fieldName = newFieldName(fdp.name, usedFieldNames),
            options =
                FieldOptions(
                    fdp.options,
                    fdp.options.getExtension(Protokt.property)
                ),
            protoTypeName = fdp.typeName,
            typePClass = typePClass(fdp.typeName, ctx, type),
            index = idx
        )
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

private fun typePClass(
    protoTypeName: String,
    ctx: ProtocolContext,
    fieldType: FieldType
): PClass {
    val fullyProtoQualified = protoTypeName.startsWith(".")

    return if (fullyProtoQualified) {
        requalifyProtoType(protoTypeName, ctx)
    } else {
        newTypeNameFromPascal(protoTypeName).let {
            if (it.isEmpty()) {
                PClass.fromClass(fieldType.protoktFieldType)
            } else {
                PClass.fromName(it)
            }
        }
    }
}

private fun requalifyProtoType(typeName: String, ctx: ProtocolContext): PClass {
    val withOverriddenGoogleProtoPackage =
        PClass.fromName(
            overrideGoogleProtobuf(typeName.removePrefix("."), rootGoogleProto)
        )

    val withOverriddenReservedName =
        PClass(
            newTypeNameFromPascal(withOverriddenGoogleProtoPackage.simpleName),
            withOverriddenGoogleProtoPackage.ppackage,
            withOverriddenGoogleProtoPackage.enclosing
        )

    return if (ctx.respectJavaPackage) {
        PClass.fromName(
            withOverriddenReservedName.nestedName
        ).qualify(ctx.ppackage(typeName))
    } else {
        withOverriddenReservedName
    }
}
