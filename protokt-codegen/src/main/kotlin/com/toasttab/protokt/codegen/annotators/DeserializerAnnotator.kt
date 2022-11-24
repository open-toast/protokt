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
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
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
import com.toasttab.protokt.codegen.impl.buildFunSpec
import com.toasttab.protokt.codegen.impl.endControlFlowWithoutNewline
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
                            addStatement("var %L", deserializeVar(it))
                        }
                    }
                    addStatement("var·unknownFields:·%T?·=·null", UnknownFieldSet.Builder::class)
                    beginControlFlow("while (true)")
                    beginControlFlow("when (deserializer.readTag())")
                    val constructor =
                        buildCodeBlock {
                            add("0·->·return·%N(\n", msg.name)
                            withIndent {
                                constructorLines(properties).forEach(::add)
                            }
                            add("\n)")
                        }
                    addStatement("%L", constructor)
                    deserializerInfo.forEach {
                        addStatement(
                            "%L -> %L = %L",
                            it.tag,
                            it.assignment.fieldName,
                            it.assignment.value
                        )
                    }
                    val unknownFieldBuilder =
                        buildCodeBlock {
                            add("(unknownFields ?: %T.Builder())", UnknownFieldSet::class)
                            beginControlFlow(".also")
                            add("it.add(deserializer.readUnknown())\n")
                            endControlFlowWithoutNewline()
                        }
                    addStatement("else -> unknownFields =\n%L", unknownFieldBuilder)
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
            CodeBlock.of("%L: %T = %L", p.name, deserializeType(p), deserializeValue(p))
        } else {
            CodeBlock.of("%L = %L", p.name, deserializeValue(p))
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
        properties.map { CodeBlock.of("%L,\n", deserializeWrapper(it)) } +
            CodeBlock.of("%T.from(unknownFields)", UnknownFieldSet::class)

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
                        { Assignment(field.fieldName, deserialize(field, ctx, tag is Tag.Packed)) },
                        { Assignment(it.fieldName, oneofDes(it, field)) }
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
        CodeBlock.of("%T(%L)", f.qualify(ff), deserialize(ff, ctx, false))

    companion object {
        fun annotateDeserializer(msg: Message, ctx: Context) =
            DeserializerAnnotator(msg, ctx).annotateDeserializer()
    }
}

internal fun deserialize(f: StandardField, ctx: Context, packed: Boolean): CodeBlock {
    val options = deserializeOptions(f, ctx)
    val read = CodeBlock.of("deserializer.%L", interceptReadFn(f, f.readFn()))

    val wrappedRead =
        options
            ?.let { opt ->
                opt.wrapName?.let { wrapField(it, read, opt.type, opt.oneof) }
            }
            ?: read

    return when {
        f.map -> deserializeMap(f, options, read)
        f.repeated ->
            buildCodeBlock {
                add("\n(${f.fieldName} ?: mutableListOf())")
                beginControlFlow(".apply")
                beginControlFlow("deserializer.readRepeated($packed)")
                add("add(%L)\n", wrappedRead)
                endControlFlow()
                endControlFlowWithoutNewline()
            }
        else -> wrappedRead
    }
}

private fun deserializeMap(f: StandardField, options: Options?, read: CodeBlock): CodeBlock {
    val key =
        options?.keyWrap
            ?.let { wrapField(it, CodeBlock.of("it.key"), options.type, options.oneof) }
            ?: CodeBlock.of("it.key")

    val value =
        options?.valueWrap
            ?.let { wrapField(it, CodeBlock.of("it.value"), options.valueType, options.oneof) }
            ?: CodeBlock.of("it.value")

    return buildCodeBlock {
        add("\n(${f.fieldName} ?: mutableMapOf())")
        beginControlFlow(".apply")
        beginControlFlow("deserializer.readRepeated(false)")
        add(read)
        beginControlFlow(".let")
        add("put(%L, %L)\n", key, value)
        endControlFlow()
        endControlFlow()
        endControlFlowWithoutNewline()
    }
}

private fun StandardField.readFn() =
    when (type) {
        FieldType.SFIXED32 -> CodeBlock.of("readSFixed32()")
        FieldType.SFIXED64 -> CodeBlock.of("readSFixed64()")
        FieldType.SINT32 -> CodeBlock.of("readSInt32()")
        FieldType.SINT64 -> CodeBlock.of("readSInt64()")
        FieldType.UINT32 -> CodeBlock.of("readUInt32()")
        FieldType.UINT64 -> CodeBlock.of("readUInt64()")
        // by default for DOUBLE we get readDouble, for BOOL we get readBool(), etc.
        else -> buildCodeBlock {
            add("read${type.name.lowercase().capitalize()}(")
            if (type == FieldType.ENUM || type == FieldType.MESSAGE) {
                add("%T", typePClass.toTypeName())
            }
            add(")")
        }
    }

private fun deserializeOptions(f: StandardField, ctx: Context) =
    if (f.wrapped || f.keyWrapped || f.valueWrapped) {
        Options(
            wrapName = wrapperName(f, ctx).getOrElse { null },
            keyWrap = mapKeyConverter(f, ctx),
            valueWrap = mapValueConverter(f, ctx),
            valueType = f.mapEntry?.value?.type,
            type = f.type,
            oneof = true
        )
    } else {
        null
    }

private class Options(
    val wrapName: TypeName?,
    val keyWrap: TypeName?,
    val valueWrap: TypeName?,
    val valueType: FieldType?,
    val type: FieldType,
    val oneof: Boolean
)
