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

package com.toasttab.protokt.codegen.annotators

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.annotators.DeserializerAnnotator.DeserializerInfo.Assignment
import com.toasttab.protokt.codegen.annotators.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.annotators.PropertyAnnotator.PropertyInfo
import com.toasttab.protokt.codegen.impl.Wrapper.interceptReadFn
import com.toasttab.protokt.codegen.impl.Wrapper.keyWrapped
import com.toasttab.protokt.codegen.impl.Wrapper.mapKeyConverter
import com.toasttab.protokt.codegen.impl.Wrapper.mapValueConverter
import com.toasttab.protokt.codegen.impl.Wrapper.valueWrapped
import com.toasttab.protokt.codegen.impl.Wrapper.wrapField
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.impl.Wrapper.wrapperName
import com.toasttab.protokt.codegen.impl.addStatement
import com.toasttab.protokt.codegen.impl.buildFunSpec
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.protoc.Tag
import com.toasttab.protokt.codegen.util.capitalize
import com.toasttab.protokt.rt.AbstractKtDeserializer
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.UnknownFieldSet

internal class DeserializerAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateDeserializer(): TypeSpec {
        val deserializerInfo = deserializerInfo()
        val properties = annotateProperties(msg, ctx)

        return TypeSpec.companionObjectBuilder("Deserializer")
            .superclass(
                AbstractKtDeserializer::class
                    .asTypeName()
                    .parameterizedBy(msg.typeName)
            )
            .addFunction(
                buildFunSpec("deserialize") {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter("deserializer", KtMessageDeserializer::class)
                    returns(msg.typeName)
                    if (properties.isNotEmpty()) {
                        properties.forEach {
                            addStatement(
                                buildCodeBlock {
                                    add("var ")
                                    add(deserializeVar(it))
                                }
                            )
                        }
                    }
                    addStatement("var·unknownFields:·%T?·=·null", UnknownFieldSet.Builder::class)
                    beginControlFlow("while (true)")
                    beginControlFlow("when (deserializer.readTag())")
                    addStatement("0·->·return·%N(%L)", msg.name, constructorLines(properties))
                    deserializerInfo.forEach {
                        addStatement(
                            CodeBlock.of(
                                "%L -> %L = ",
                                it.tag,
                                it.assignment.fieldName
                            ),
                            it.assignment.value
                        )
                    }
                    addStatement("else -> unknownFields = (unknownFields ?: %T.Builder()).also·{it.add(deserializer.readUnknown()) }", UnknownFieldSet::class)
                    endControlFlow()
                    endControlFlow()
                }
            )
            .apply {
                msg.nestedTypes
                    .filterIsInstance<Message>()
                    .filterNot { it.mapEntry }
                    .forEach { addConstructorFunction(it, ::addFunction) }
            }
            .build()
    }

    private fun deserializeVar(p: PropertyInfo): CodeBlock =
        if (p.fieldType == "MESSAGE" || p.repeated || p.oneof || p.nullable || p.wrapped) {
            buildCodeBlock {
                add(CodeBlock.of("%L : %T = ", p.name, deserializeType(p)))
                add(deserializeValue(p))
            }
        } else {
            buildCodeBlock {
                add(CodeBlock.of("%L = ", p.name))
                add(deserializeValue(p))
            }
        }

    private fun deserializeType(p: PropertyInfo) =
        if (p.repeated || p.map) {
            p.deserializeType as ParameterizedTypeName
            ClassName(p.deserializeType.rawType.packageName, "Mutable" + p.deserializeType.rawType.simpleName)
                .parameterizedBy(p.deserializeType.typeArguments)
                .copy(nullable = true)
        } else {
            p.deserializeType
        }

    private fun constructorLines(properties: List<PropertyInfo>) =
        buildCodeBlock {
            properties.forEach { add("%L,\n", deserializeWrapper(it)) }
            add("%T.from(unknownFields)", UnknownFieldSet::class)
        }

    private class DeserializerInfo(
        val tag: Int,
        val assignment: Assignment
    ) {
        class Assignment(
            val fieldName: String,
            val value: CodeBlock
        )
    }

    private fun deserializerInfo(): List<DeserializerInfo> =
        msg.flattenedSortedFields().flatMap { (field, oneOf) ->
            field.tagList.map { tag ->
                DeserializerInfo(
                    tag.value,
                    oneOf.fold(
                        {
                            deserialize(
                                field,
                                ctx,
                                tag is Tag.Packed
                            ).let { value ->
                                Assignment(field.fieldName, value)
                            }
                        },
                        {
                            Assignment(it.fieldName, oneofDes(it, field))
                        }
                    )
                )
            }
        }

    private fun Message.flattenedSortedFields() =
        fields.flatMap {
            when (it) {
                is StandardField ->
                    listOf(FlattenedField(it, None))
                is Oneof ->
                    it.fields.map { f -> FlattenedField(f, Some(it)) }
            }
        }.sortedBy { it.field.number }

    private data class FlattenedField(
        val field: StandardField,
        val oneof: Option<Oneof>
    )

    private fun oneofDes(f: Oneof, ff: StandardField) =
        buildCodeBlock {
            add("%T(", f.qualify(ff))
            add(deserialize(ff, ctx, false))
            add(")")
        }

    companion object {
        fun annotateDeserializer(msg: Message, ctx: Context) =
            DeserializerAnnotator(msg, ctx).annotateDeserializer()
    }
}

internal fun deserialize(f: StandardField, ctx: Context, packed: Boolean): CodeBlock {
    val options = deserializeOptions(f, ctx)
    val read = CodeBlock.of("deserializer.%L", interceptReadFn(f, f.readFn()))
    val wrappedRead = options?.let { wrapField(it.wrapName, read, it.type, it.oneof) } ?: read
    return when {
        f.map -> CodeBlock.of(deserializeMap(f, options, read))
        f.repeated ->
            buildCodeBlock {
                add("(${f.fieldName} ?: mutableListOf())")
                beginControlFlow(".apply")
                beginControlFlow("deserializer.readRepeated($packed)")
                add("add(")
                add(wrappedRead)
                add(")")
                endControlFlow()
                endControlFlow()
            }
        else -> wrappedRead
    }
}

// todo
internal fun deserializeMap(f: StandardField, options: Options?, read: CodeBlock): String {
    val key = options?.keyWrap?.let { wrapField(it, CodeBlock.of("it.key"), options.type, options.oneof) } ?: CodeBlock.of("it.key")
    val value = options?.valueWrap?.let { wrapField(it, CodeBlock.of("it.value"), options.valueType, options.oneof) } ?: CodeBlock.of("it.value")
    return """
        |(${f.fieldName} ?: mutableMapOf()).apply·{
        |       deserializer.readRepeated(false)·{
        |           $read
        |           .let { put(
        |               $key,
        |               $value
        |           ) }
        |       }
        |   }
    """.trimMargin()
}

private fun StandardField.readFn() =
    when (type) {
        FieldType.SFIXED32 -> "readSFixed32()"
        FieldType.SFIXED64 -> "readSFixed64()"
        FieldType.SINT32 -> "readSInt32()"
        FieldType.SINT64 -> "readSInt64()"
        FieldType.UINT32 -> "readUInt32()"
        FieldType.UINT64 -> "readUInt64()"
        // by default for DOUBLE we get readDouble, for BOOL we get readBool(), etc.
        else -> "read${type.name.lowercase().capitalize()}(${readFnBuilder(type)})"
    }

private fun StandardField.readFnBuilder(type: FieldType) =
    when (type) {
        FieldType.ENUM, FieldType.MESSAGE -> typePClass.qualifiedName
        else -> ""
    }

private fun deserializeOptions(f: StandardField, ctx: Context) =
    if (f.wrapped || f.keyWrapped || f.valueWrapped) {
        Options(
            wrapName = wrapperName(f, ctx).map { it.toString() }.getOrElse { "" },
            keyWrap = mapKeyConverter(f, ctx)?.toString(),
            valueWrap = mapValueConverter(f, ctx)?.toString(),
            valueType = f.mapEntry?.value?.type,
            type = f.type,
            oneof = true
        )
    } else {
        null
    }

class Options(
    val wrapName: String,
    val keyWrap: String?,
    val valueWrap: String?,
    val valueType: FieldType?,
    val type: FieldType,
    val oneof: Boolean
)
