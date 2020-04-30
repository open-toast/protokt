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
import arrow.core.extensions.list.foldable.nonEmpty
import arrow.core.toOption
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
import com.toasttab.protokt.codegen.impl.emptyToNone
import com.toasttab.protokt.ext.Protokt
import com.toasttab.protokt.rt.PType
import com.toasttab.protokt.shared.KOTLIN_EXTRA_CLASSPATH
import com.toasttab.protokt.shared.RESPECT_JAVA_PACKAGE

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
            context =
                PluginContext(
                    ctx.params.getOrDefault(KOTLIN_EXTRA_CLASSPATH, "").split(";"),
                    respectJavaPackage(ctx.params),
                    ctx.fdp.name,
                    ctx.allPackagesByTypeName
                ),
            sourceCodeInfo = ctx.fdp.sourceCodeInfo
        ),
        types = toTypeList(
            ctx,
            ctx.fdp.enumTypeList,
            ctx.fdp.messageTypeList,
            ctx.fdp.serviceList
        )
    )

fun respectJavaPackage(params: Map<String, String>) =
    params.getValue(RESPECT_JAVA_PACKAGE).toBoolean()

val FileDescriptorProto.fileOptions
    get() =
        FileOptions(
            options,
            options.extensionOrDefault(Protokt.file)
        )

fun FileDescriptorProto.pkg(
    paramName: String
) =
    packageName(this, paramName)

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
        Tuple2(acc.a + e.type, acc.b + e)
    }.b +

    messages.foldIndexed(
        Tuple2(immutableSetOf<String>(), immutableListOf<Type>())
    ) { idx, acc, t ->
        val m = toMessage(idx, ctx, t, acc.a)
        Tuple2(acc.a + m.type, acc.b + m)
    }.b +

    services.fold(
        Tuple2(immutableSetOf<String>(), immutableListOf<Type>())
    ) { acc, t ->
        val s = toService(t, acc.a)
        Tuple2(acc.a + s.type, acc.b + s)
    }.b

private fun toEnum(
    idx: Int,
    desc: EnumDescriptorProto,
    names: ImmutableSet<String>
): EnumType {
    val typeName = newTypeName(desc.name, names)
    return EnumType(
        name = desc.name,
        type = typeName,
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
    val typeName = newTypeName(desc.name, names)
    val fieldList = toFields(ctx, desc, names + typeName)
    return MessageType(
        name = desc.name,
        fields = fieldList,
        nestedTypes = toTypeList(ctx, desc.enumTypeList, desc.nestedTypeList),
        mapEntry = desc.options?.mapEntry == true,
        type = typeName,
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
    names: Set<String>
) =
    ServiceType(
        name = desc.name,
        type = newTypeName(desc.name, names),
        methods = desc.methodList.map { toMethod(it) },
        deprecated = desc.options.deprecated,
        options =
            ServiceOptions(
                desc.options,
                desc.options.extensionOrDefault(Protokt.service)
            )
    )

private fun toMethod(desc: DescriptorProtos.MethodDescriptorProto) =
    Method(
        desc.name,
        desc.inputType,
        desc.outputType,
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
                            acc.d + toOneOf(idx, ctx, desc, ood, t, acc.a, acc.d)
                        )
                    }
                })
            }
        }
    }.d

private fun toOneOf(
    idx: Int,
    ctx: ProtocolContext,
    desc: DescriptorProto,
    oneOf: OneofDescriptorProto,
    field: FieldDescriptorProto,
    typeNames: Set<String>,
    fields: ImmutableList<Field>
): Oneof {
    val newName = newFieldName(oneOf.name, typeNames)
    val standardTuple = desc.fieldList.filter {
        it.hasOneofIndex() && it.oneofIndex == field.oneofIndex
    }.foldIndexed(
        Tuple3(
            immutableMapOf<String, String>(),
            immutableSetOf<String>(),
            immutableListOf<StandardField>()
        ), { oneofIdx, acc, t ->
            val ftn = newTypeName(t.name, acc.b)
            Tuple3(
                acc.a + (t.name to ftn),
                acc.b + ftn,
                acc.c + toStandard(idx + oneofIdx, ctx, t, emptySet(), true)
            )
    })
    return Oneof(
        name = oneOf.name,
        fieldTypeNames = standardTuple.a,
        fieldName = newName,
        nativeTypeName = newTypeName(oneOf.name, typeNames),
        fields = standardTuple.c,
        options =
            OneofOptions(
                oneOf.options,
                oneOf.options.extensionOrDefault(Protokt.oneof)
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
): StandardField = toPType(
    fdp.type ?: error("Missing field type")).let { type ->
    StandardField(
        number = fdp.number,
        name = fdp.name!!,
        type = type,
        typeName = fdp.typeName,
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
        nativeTypeName =
            if (fdp.typeName.startsWith('.')) {
                None
            } else {
                Some(newTypeName(fdp.typeName))
            },
        options =
            FieldOptions(
                fdp.options,
                fdp.options.extensionOrDefault(Protokt.property)
            ),
        index = idx
    )
}

private fun packageName(
    fdp: FileDescriptorProto,
    paramName: String
) =
    if (fdp.options?.uninterpretedOptionList?.nonEmpty() == true) {
        fdp.options?.uninterpretedOptionList?.find { f ->
            f.nameList.singleOrNull()?.namePart == paramName
        }?.stringValue?.toStringUtf8().toOption()
    } else {
        fdp.`package`.emptyToNone()
    }
