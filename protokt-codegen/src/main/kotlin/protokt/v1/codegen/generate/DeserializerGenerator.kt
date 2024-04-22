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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import protokt.v1.AbstractDeserializer
import protokt.v1.Decoder
import protokt.v1.UnknownFieldSet
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Wrapper.interceptRead
import protokt.v1.codegen.generate.Wrapper.wrapField
import protokt.v1.codegen.util.FieldType
import protokt.v1.codegen.util.FieldType.Enum
import protokt.v1.codegen.util.FieldType.SFixed32
import protokt.v1.codegen.util.FieldType.SFixed64
import protokt.v1.codegen.util.FieldType.SInt32
import protokt.v1.codegen.util.FieldType.SInt64
import protokt.v1.codegen.util.FieldType.UInt32
import protokt.v1.codegen.util.FieldType.UInt64
import protokt.v1.codegen.util.KotlinPlugin
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.Oneof
import protokt.v1.codegen.util.StandardField
import protokt.v1.codegen.util.Tag

fun generateDeserializer(msg: Message, ctx: Context, properties: List<PropertyInfo>) =
    DeserializerGenerator(msg, ctx, properties).generate()

private class DeserializerGenerator(
    private val msg: Message,
    private val ctx: Context,
    private val properties: List<PropertyInfo>
) {
    fun generate(): TypeSpec {
        val deserializerInfo = deserializerInfo()

        return TypeSpec.companionObjectBuilder(msg.deserializerClassName.simpleName)
            .superclass(
                AbstractDeserializer::class
                    .asTypeName()
                    .parameterizedBy(msg.className)
            )
            .addFunction(
                buildFunSpec("deserialize") {
                    addModifiers(KModifier.OVERRIDE)
                    if (ctx.info.context.appliedKotlinPlugin != KotlinPlugin.JS) {
                        addAnnotation(JvmStatic::class) // can't put this here generally until JS code is actually common code in a multiplatform module
                    }
                    addParameter("decoder", Decoder::class)
                    returns(msg.className)
                    if (properties.isNotEmpty()) {
                        properties.forEach {
                            addStatement("var %L", declareDeserializeVar(it))
                        }
                    }
                    addStatement("var·unknownFields:·%T?·=·null\n", UnknownFieldSet.Builder::class)
                    beginControlFlow("while (true)")
                    beginControlFlow("when (deconder.readTag())")
                    val constructor =
                        buildCodeBlock {
                            add("0u·->·return·%T(\n", msg.className)
                            withIndent {
                                constructorLines(properties).forEach(::add)
                            }
                            add("\n)")
                        }
                    addStatement("%L", constructor)
                    deserializerInfo.forEach {
                        addStatement(
                            "%Lu -> %N = %L",
                            it.tag,
                            it.fieldName,
                            it.value
                        )
                    }
                    val unknownFieldBuilder =
                        buildCodeBlock {
                            add("(unknownFields ?: %T.Builder())", UnknownFieldSet::class)
                            beginControlFlow(".also")
                            add("it.add(decoder.readUnknown())\n")
                            endControlFlowWithoutNewline()
                        }
                    addStatement("else -> unknownFields =\n%L", unknownFieldBuilder)
                    endControlFlow()
                    endControlFlow()
                }
            )
            .addFunction(
                buildFunSpec("invoke") {
                    if (ctx.info.context.appliedKotlinPlugin != KotlinPlugin.JS) {
                        addAnnotation(JvmStatic::class) // todo: remove when JS code is common in multiplatform
                    }
                    addModifiers(KModifier.OPERATOR)
                    returns(msg.className)
                    addParameter(
                        "dsl",
                        LambdaTypeName.get(
                            msg.builderClassName,
                            emptyList(),
                            Unit::class.asTypeName()
                        )
                    )
                    addStatement("return %T().apply(dsl).build()", msg.builderClassName)
                    build()
                }
            )
            .build()
    }

    private fun declareDeserializeVar(p: PropertyInfo): CodeBlock {
        val initialState = deserializeVarInitialState(p)
        return if (p.fieldType == FieldType.Message || p.repeated || p.oneof || p.nullable || p.wrapped) {
            CodeBlock.of("%N: %T = %L", p.name, deserializeType(p), initialState)
        } else {
            CodeBlock.of("%N = %L", p.name, initialState)
        }
    }

    private fun deserializeType(p: PropertyInfo) =
        if (p.repeated || p.isMap) {
            p.deserializeType as ParameterizedTypeName
            ClassName(p.deserializeType.rawType.packageName, "Mutable" + p.deserializeType.rawType.simpleName)
                .parameterizedBy(p.deserializeType.typeArguments)
                .copy(nullable = true)
        } else {
            p.deserializeType
        }

    private fun constructorLines(properties: List<PropertyInfo>) =
        properties.map { CodeBlock.of("%L,\n", wrapDeserializedValueForConstructor(it)) } +
            CodeBlock.of("%T.from(unknownFields)", UnknownFieldSet::class)

    private class DeserializerInfo(
        val tag: UInt,
        val fieldName: String,
        val value: CodeBlock
    )

    private fun deserializerInfo(): List<DeserializerInfo> =
        msg.flattenedSortedFields().flatMap { (field, oneOf) ->
            field.tagList.map { tag ->
                DeserializerInfo(
                    tag.value,
                    oneOf?.fieldName ?: field.fieldName,
                    oneOf?.let { oneofDes(it, field) } ?: deserialize(field, ctx, tag is Tag.Packed)
                )
            }
        }

    private val StandardField.tagList
        get() =
            tag.let {
                if (repeated) {
                    // For repeated fields, catch the other (packed or non-packed)
                    // possibility.
                    keepIfDifferent(
                        it,
                        if (packed) {
                            Tag.Unpacked(number, type.wireType)
                        } else {
                            Tag.Packed(number)
                        }
                    )
                } else {
                    listOf(it)
                }
            }.sorted()

    private fun keepIfDifferent(tag: Tag, other: Tag) =
        if (tag.value == other.value) {
            listOf(tag)
        } else {
            listOf(tag, other)
        }

    private fun Message.flattenedSortedFields() =
        fields.flatMap {
            when (it) {
                is StandardField -> listOf(FlattenedField(it))
                is Oneof -> it.fields.map { f -> FlattenedField(f, it) }
            }
        }.sortedBy { it.field.number }

    private data class FlattenedField(
        val field: StandardField,
        val oneof: Oneof? = null
    )

    private fun oneofDes(f: Oneof, ff: StandardField) =
        CodeBlock.of("%T(%L)", f.qualify(ff), deserialize(ff, ctx))
}

fun deserialize(f: StandardField, ctx: Context, packed: Boolean = false): CodeBlock {
    val read = CodeBlock.of("decoder.%L", interceptRead(f, f.readFn()))
    val wrappedRead = wrapField(f, ctx, read) ?: read

    return when {
        f.isMap -> deserializeMap(f, read)
        f.repeated ->
            buildCodeBlock {
                add("\n(%N ?: mutableListOf())", f.fieldName)
                beginControlFlow(".apply")
                beginControlFlow("decoder.readRepeated($packed)")
                add("add(%L)\n", wrappedRead)
                endControlFlow()
                endControlFlowWithoutNewline()
            }
        else -> wrappedRead
    }
}

private fun deserializeMap(f: StandardField, read: CodeBlock): CodeBlock {
    return buildCodeBlock {
        add("\n(%N ?: mutableMapOf())", f.fieldName)
        beginControlFlow(".apply")
        beginControlFlow("decoder.readRepeated(false)")
        add(read)
        beginControlFlow(".let")
        add("put(%L, %L)\n", CodeBlock.of("it.key"), CodeBlock.of("it.value"))
        endControlFlow()
        endControlFlow()
        endControlFlowWithoutNewline()
    }
}

private fun StandardField.readFn() =
    when (type) {
        SFixed32 -> CodeBlock.of("readSFixed32()")
        SFixed64 -> CodeBlock.of("readSFixed64()")
        SInt32 -> CodeBlock.of("readSInt32()")
        SInt64 -> CodeBlock.of("readSInt64()")
        UInt32 -> CodeBlock.of("readUInt32()")
        UInt64 -> CodeBlock.of("readUInt64()")
        // by default for DOUBLE we get readDouble, for BOOL we get readBool(), etc.
        else -> buildCodeBlock {
            add("read${type::class.simpleName}(")
            if (type == Enum || type == FieldType.Message) {
                add("%T", className)
            }
            add(")")
        }
    }
