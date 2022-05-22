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
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.annotators.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.impl.Wrapper.interceptReadFn
import com.toasttab.protokt.codegen.impl.Wrapper.keyWrapped
import com.toasttab.protokt.codegen.impl.Wrapper.mapKeyConverter
import com.toasttab.protokt.codegen.impl.Wrapper.mapValueConverter
import com.toasttab.protokt.codegen.impl.Wrapper.valueWrapped
import com.toasttab.protokt.codegen.impl.Wrapper.wrapField
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.impl.Wrapper.wrapperName
import com.toasttab.protokt.codegen.impl.buildFunSpec
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.protoc.Tag
import com.toasttab.protokt.codegen.template.Message.Message.DeserializerInfo
import com.toasttab.protokt.codegen.template.Message.Message.DeserializerInfo.Assignment
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo
import com.toasttab.protokt.rt.AbstractKtDeserializer
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.UnknownFieldSet

internal class DeserializerAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    /** Creates a {@see KtDeserializer<T>} companion object, where T is the Kotlin type for this Message.
     *
     * Contains functions:
     *  - deserialize(deserializer : KTMessageDeserializer) : T
     *  - invoke(dsl: ApiDsl.() -> Unit): Api
     *
     * */
    private fun annotateDeserializer(): TypeSpec {
        val deserializerInfo = annotateDeserializerOld()
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
                        properties.forEach { addStatement("var %L", deserializeVar(it)) }
                    }
                    addStatement("var unknownFields: %T? = null", UnknownFieldSet.Builder::class)
                    beginControlFlow("while (true)")
                    beginControlFlow("when(deserializer.readTag())")
                    addStatement("0 -> return·%N(%L)", msg.name, constructorLines(properties))
                    deserializerInfo.forEach() { addStatement("%L -> %L = %L", it.tag, it.assignment.fieldName, it.assignment.value) }
                    addStatement("else -> unknownFields = (unknownFields ?: %T.Builder()).also·{it.add(deserializer.readUnknown()) }", UnknownFieldSet::class)
                    endControlFlow()
                    endControlFlow()
                }
            )
            .apply {
                msg.nestedTypes.filterIsInstance<Message>().forEach { message ->
                    addFunction(
                        FunSpec.builder(message.name)
                            .returns(message.typeName)
                            .addParameter(
                                "dsl",
                                LambdaTypeName.get(
                                    message.dslTypeName,
                                    emptyList(),
                                    Unit::class.asTypeName()
                                )
                            )
                            .addStatement("return %T().apply(dsl).build()", message.dslTypeName)
                            .build()
                    )
                }
            }
            .build()
    }

    private fun constructorLines(properties: List<PropertyInfo>) =
        buildCodeBlock {
            properties.forEach { add("%L,\n", deserializeWrapper(it)) }
            add("%T.from(unknownFields)", UnknownFieldSet::class)
        }

    private fun annotateDeserializerOld(): List<DeserializerInfo> =
        msg.flattenedSortedFields().flatMap { (field, oneOf) ->
            field.tagList.map { tag ->
                DeserializerInfo(
                    field.repeated,
                    tag.value,
                    oneOf.fold(
                        {
                            deserializeString(
                                field,
                                ctx,
                                tag is Tag.Packed
                            ).let { value ->
                                Assignment(
                                    field.fieldName,
                                    value
                                )
                            }
                        },
                        {
                            oneofDes(it, field).let { value ->
                                Assignment(
                                    it.fieldName,
                                    value
                                )
                            }
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
        "${f.name}.${f.fieldTypeNames.getValue(ff.fieldName)}(${deserializeString(ff, ctx, false)})"

    companion object {
        fun annotateDeserializer(msg: Message, ctx: Context) =
            DeserializerAnnotator(msg, ctx).annotateDeserializer()
    }
}

fun deserializeString(f: StandardField, ctx: Context, packed: Boolean): String {
    val options = deserializeOptions(f, ctx)
    val read = CodeBlock.of("deserializer.%L", interceptReadFn(f, f.readFn()))
    val wrappedRead = options?.let { wrapField(it.wrapName, read, it.type, it.oneof) } ?: read.toString()
    return when {
        f.map -> deserializeMap(f, options, read)
        f.repeated -> """
            |(${f.fieldName} ?: mutableListOf()).apply·{
            |       deserializer.readRepeated($packed)·{
            |           add($wrappedRead)
            |       }
            |   }
        """.trimMargin()
        else -> wrappedRead.toString()
    }
}

fun deserializeMap(f: StandardField, options: Options?, read: CodeBlock): String {
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
        else -> "read${type.name.lowercase().replaceFirstChar { it.uppercase() }}(${readFnBuilder(type)})"
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
