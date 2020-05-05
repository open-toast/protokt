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
import arrow.core.Some
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.Tuple4
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
import com.google.protobuf.GeneratedMessage.GeneratedExtension
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage
import com.toasttab.protokt.codegen.impl.STAnnotator.rootGoogleProto
import com.toasttab.protokt.codegen.impl.overrideGoogleProtobuf
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.ext.Protokt
import com.toasttab.protokt.rt.PType

private fun <I, E : ExtendableMessage<E>> E.extensionOrDefault(
    ext: GeneratedExtension<E, I>
) =
    if (hasExtension(ext)) {
        getExtension(ext)
    } else {
        ext.defaultValue
    }

fun toProtocol(ctx: ProtocolContext) =
    Protocol(
        FileDesc(
            name = ctx.fdp.name,
            packageName = ctx.fdp.`package`,
            version = ctx.fdp.syntax?.removePrefix("proto")?.toIntOrNull() ?: 2,
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
            options.extensionOrDefault(Protokt.file)
        )

fun FileDescriptorProto.newFileName(name: String) =
    newFileName(name, this.name)

private fun toPType(type: FieldDescriptorProto.Type) = when (type) {
    FieldDescriptorProto.Type.TYPE_BOOL -> PType.BOOL
    FieldDescriptorProto.Type.TYPE_BYTES -> PType.BYTES
    FieldDescriptorProto.Type.TYPE_DOUBLE -> PType.DOUBLE
    FieldDescriptorProto.Type.TYPE_ENUM -> PType.ENUM
    FieldDescriptorProto.Type.TYPE_FIXED32 -> PType.FIXED32
    FieldDescriptorProto.Type.TYPE_FIXED64 -> PType.FIXED64
    FieldDescriptorProto.Type.TYPE_FLOAT -> PType.FLOAT
    FieldDescriptorProto.Type.TYPE_INT32 -> PType.INT32
    FieldDescriptorProto.Type.TYPE_INT64 -> PType.INT64
    FieldDescriptorProto.Type.TYPE_MESSAGE -> PType.MESSAGE
    FieldDescriptorProto.Type.TYPE_SFIXED32 -> PType.SFIXED32
    FieldDescriptorProto.Type.TYPE_SFIXED64 -> PType.SFIXED64
    FieldDescriptorProto.Type.TYPE_SINT32 -> PType.SINT32
    FieldDescriptorProto.Type.TYPE_SINT64 -> PType.SINT64
    FieldDescriptorProto.Type.TYPE_STRING -> PType.STRING
    FieldDescriptorProto.Type.TYPE_UINT32 -> PType.UINT32
    FieldDescriptorProto.Type.TYPE_UINT64 -> PType.UINT64
    else -> error("Unknown type: $type")
}

private fun toTypeList(
    ctx: ProtocolContext,
    enums: List<EnumDescriptorProto>,
    messages: List<DescriptorProto>,
    services: List<ServiceDescriptorProto> = emptyList()
): ImmutableList<Type> =
    enums.foldIndexed(
        Tuple2(immutableSetOf<String>(), immutableListOf<Type>())
    ) { idx, acc, t ->
        val e = toEnum(idx, t, acc.a)
        Tuple2(acc.a + e.name, acc.b + e)
    }.b +

    messages.foldIndexed(
        Tuple2(immutableSetOf<String>(), immutableListOf<Type>())
    ) { idx, acc, t ->
        val m = toMessage(idx, ctx, t, acc.a)
        Tuple2(acc.a + m.name, acc.b + m)
    }.b +

    services.fold(
        Tuple2(immutableSetOf<String>(), immutableListOf<Type>())
    ) { acc, t ->
        val s = toService(t, ctx, acc.a)
        Tuple2(acc.a + s.type, acc.b + s)
    }.b

private fun toEnum(
    idx: Int,
    desc: EnumDescriptorProto,
    names: ImmutableSet<String>
): EnumType {
    val typeName = newTypeNameFromCamel(desc.name, names)
    return EnumType(
        name = typeName,
        values = desc.valueList.toList().foldIndexed(
            Tuple2(names + typeName, immutableListOf<EnumType.Value>())
        ) { enumIdx, acc, t ->
            val n = newEnumValueName(t.name, acc.a)
            val v =
                EnumType.Value(
                    t.number,
                    t.name,
                    n,
                    EnumValueOptions(
                        t.options,
                        t.options.extensionOrDefault(Protokt.enumValue)
                    ),
                    enumIdx
                )
            Tuple2(acc.a + n, acc.b + v)
        }.b,
        index = idx,
        options =
            EnumOptions(
                desc.options,
                desc.options.extensionOrDefault(Protokt.enum_)
            )
    )
}

private fun toMessage(
    idx: Int,
    ctx: ProtocolContext,
    desc: DescriptorProto,
    names: Set<String>
): MessageType {
    val typeName = newTypeNameFromPascal(desc.name, names)
    val fieldList = toFields(ctx, desc, names + typeName)
    return MessageType(
        name = typeName,
        fields = fieldList,
        nestedTypes = toTypeList(ctx, desc.enumTypeList, desc.nestedTypeList),
        mapEntry = desc.options?.mapEntry == true,
        options =
            MessageOptions(
                desc.options,
                desc.options.extensionOrDefault(Protokt.class_)
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
    ServiceType(
        name = desc.name,
        type = newTypeNameFromPascal(desc.name, names),
        methods = desc.methodList.map { toMethod(it, ctx) },
        deprecated = desc.options.deprecated,
        options =
            ServiceOptions(
                desc.options,
                desc.options.extensionOrDefault(Protokt.service)
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
            desc.options.extensionOrDefault(Protokt.method)
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
                    Tuple4(acc.a + f.fieldName, acc.b, acc.c, acc.d + f)
                }, {
                    if (it in acc.b || desc.oneofDeclList.isEmpty()) {
                        acc
                    } else {
                        val ood = desc.getOneofDecl(it)
                        Tuple4(
                            acc.a,
                            acc.b + it,
                            acc.c + ood.name,
                            acc.d + toOneof(idx, ctx, desc, ood, t, acc.a, acc.d)
                        )
                    }
                })
            }
        }
    }.d

private fun toOneof(
    idx: Int,
    ctx: ProtocolContext,
    desc: DescriptorProto,
    oneof: OneofDescriptorProto,
    field: FieldDescriptorProto,
    typeNames: Set<String>,
    fields: ImmutableList<Field>
): Oneof {
    val newName = newFieldName(oneof.name, typeNames)
    val standardTuple = desc.fieldList.filter {
        it.hasOneofIndex() && it.oneofIndex == field.oneofIndex
    }.foldIndexed(
        Tuple3(
            immutableMapOf<String, String>(),
            immutableSetOf<String>(),
            immutableListOf<StandardField>()
        ), { oneofIdx, acc, t ->
            val ftn = newTypeNameFromCamel(t.name, acc.b)
            Tuple3(
                acc.a + (newFieldName(t.name, acc.b) to ftn),
                acc.b + ftn,
                acc.c + toStandard(idx + oneofIdx, ctx, t, emptySet(), true)
            )
    })
    return Oneof(
        name = newTypeNameFromCamel(oneof.name, typeNames),
        fieldTypeNames = standardTuple.a,
        fieldName = newName,
        fields = standardTuple.c,
        options =
            OneofOptions(
                oneof.options,
                oneof.options.extensionOrDefault(Protokt.oneof)
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
    toPType(checkNotNull(fdp.type) { "Missing field type" }).let { type ->
        StandardField(
            number = fdp.number,
            name = newFieldName(fdp.name, usedFieldNames),
            type = type,
            repeated = fdp.label == LABEL_REPEATED,
            optional = !alwaysRequired && fdp.label == LABEL_OPTIONAL,
            packed =
                type.packed &&
                    (ctx.fdp.syntax == "proto3" || fdp.options?.packed == true),
            map =
                fdp.label == LABEL_REPEATED &&
                fdp.type == FieldDescriptorProto.Type.TYPE_MESSAGE &&
                    ctx.findLocal(fdp.typeName).fold(
                        { false },
                        { it.options?.mapEntry == true }
                    ),
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

private fun typePClass(
    protoTypeName: String,
    ctx: ProtocolContext,
    type: PType
): PClass {
    val fullyProtoQualified = protoTypeName.startsWith(".")

    return if (fullyProtoQualified) {
        requalifyProtoType(protoTypeName, ctx)
    } else {
        newTypeNameFromPascal(protoTypeName).let {
            if (it.isEmpty()) {
                PClass.fromClass(type.protoktFieldType)
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

    return if (ctx.respectJavaPackage()) {
        PClass.fromName(
            withOverriddenReservedName.nestedName
        ).qualify(ctx.ppackage(typeName))
    } else {
        withOverriddenReservedName
    }
}
