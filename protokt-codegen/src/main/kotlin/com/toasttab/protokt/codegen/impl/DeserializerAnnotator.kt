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

package com.toasttab.protokt.codegen.impl

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.impl.MessageAnnotator.Companion.IDEAL_MAX_WIDTH
import com.toasttab.protokt.codegen.impl.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.impl.Wrapper.interceptReadFn
import com.toasttab.protokt.codegen.impl.Wrapper.keyWrapped
import com.toasttab.protokt.codegen.impl.Wrapper.mapKeyConverter
import com.toasttab.protokt.codegen.impl.Wrapper.mapValueConverter
import com.toasttab.protokt.codegen.impl.Wrapper.valueWrapped
import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.impl.Wrapper.wrapperName
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.protoc.Tag
import com.toasttab.protokt.codegen.template.Message.Message.DeserializerInfo
import com.toasttab.protokt.codegen.template.Message.Message.DeserializerInfo.Assignment
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo
import com.toasttab.protokt.codegen.template.Renderers.Deserialize
import com.toasttab.protokt.codegen.template.Renderers.Deserialize.Options
import com.toasttab.protokt.codegen.template.Renderers.Read
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.codegen.template.Oneof as OneofTemplate

internal class DeserializerAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateDeserializerNew(): TypeSpec {
        val deserializerInfo = annotateDeserializerOld()
        val properties = annotateProperties(msg, ctx)

        return TypeSpec.companionObjectBuilder("Deserializer")
            .addSuperinterface(
                KtDeserializer::class
                    .asTypeName()
                    .parameterizedBy(TypeVariableName(msg.name))
            )
            .addSuperinterface(
                LambdaTypeName.get(
                    null,
                    listOf(
                        ParameterSpec.unnamed(
                            LambdaTypeName.get(
                                TypeVariableName("${msg.name}Dsl"),
                                emptyList(),
                                Unit::class.asTypeName()
                            )
                        )
                    ),
                    TypeVariableName(msg.name)
                )
            )
            .addFunction(
                FunSpec.builder("deserialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("deserializer", KtMessageDeserializer::class)
                    .returns(TypeVariableName(msg.name))
                    .addCode(
                        if (properties.isNotEmpty()) {
                            properties.joinToString("\n") { "var " + deserializeVar(it) } + "\n"
                        } else {
                            ""
                        } +
                        """
                            |var unknownFields: UnknownFieldSet.Builder? = null
                            |
                            |while (true) {
                            |  when (deserializer.readTag()) {
                            |    0 ->
                            |      return ${msg.name}(
                            |        ${constructorLines(properties)}
                            |        UnknownFieldSet.from(unknownFields)
                            |      )
                            |${assignmentLines(deserializerInfo)}
                            |    else ->
                            |      unknownFields =
                            |        (unknownFields ?: UnknownFieldSet.Builder()).also {
                            |          it.add(deserializer.readUnknown())
                            |        }
                            |  }
                            |}
                        """.trimMargin()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(TypeVariableName(msg.name))
                    .addParameter(
                        "dsl",
                        LambdaTypeName.get(
                            TypeVariableName("${msg.name}Dsl"),
                            emptyList(),
                            Unit::class.asTypeName()
                        )
                    )
                    .addCode("return ${msg.name}Dsl().apply(dsl).build()")
                    .build()
            )
            .build()
    }

    private fun constructorLines(properties: List<PropertyInfo>) =
        properties.joinToString("\n        ") {
            deserializeWrapper(it) + ","
        }


    private fun assignmentLines(deserializerInfo: List<DeserializerInfo>) =
        deserializerInfo.joinToString("\n") {
            if (it.repeated) {
                """
                    |    ${it.tag} ->
                    |      ${it.assignment.fieldName} =
                    |        ${it.assignment.value}
                """.trimMargin()
            } else {
                "    ${it.tag} -> ${it.assignment.fieldName} = ${it.assignment.value}"
            }
        }

    private fun annotateDeserializerOld(): List<DeserializerInfo> =
        msg.flattenedSortedFields().flatMap { (field, oneOf) ->
            field.tagList.map { tag ->
                DeserializerInfo(
                    oneOf.isEmpty(),
                    field.repeated,
                    tag.value,
                    oneOf.fold(
                        {
                            deserializeString(
                                field,
                                tag is Tag.Packed
                            ).let { value ->
                                Assignment(
                                    field.fieldName,
                                    value,
                                    long(field, value)
                                )
                            }
                        },
                        {
                            oneofDes(it, field).let { value ->
                                Assignment(
                                    it.fieldName,
                                    value,
                                    long(field, value)
                                )
                            }
                        }
                    )
                )
            }
        }

    private fun long(field: StandardField, value: String): Boolean {
        val spaceTaken =
            (ctx.enclosing.size * 4) + // outer indentation
                4 + // companion object
                4 + // fun deserialize
                4 + // while (true)
                4 + // when (...)
                field.tag.toString().length +
                4 + // ` -> `
                field.name.length +
                3 // ` = `

        val spaceLeft = IDEAL_MAX_WIDTH - spaceTaken

        return value.length > spaceLeft
    }

    private fun deserializeString(f: StandardField, packed: Boolean) =
        Deserialize.render(
            field = f,
            read = interceptReadFn(f, f.readFn()),
            lhs = f.fieldName,
            packed = packed,
            options = deserializeOptions(f)
        )

    private fun deserializeOptions(f: StandardField) =
        if (f.wrapped || f.keyWrapped || f.valueWrapped) {
            Options(
                wrapName = wrapperName(f, ctx).getOrElse { "" },
                keyWrap = mapKeyConverter(f, ctx),
                valueWrap = mapValueConverter(f, ctx),
                valueType = f.mapEntry?.value?.type,
                type = f.type.toString(),
                oneof = true
            )
        } else {
            null
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
        OneofTemplate.Deserialize.render(
            oneof = f.name,
            name = f.fieldTypeNames.getValue(ff.name),
            read = deserializeString(ff, false)
        )

    private fun StandardField.readFn() =
        Read.render(
            type = type,
            builder = readFnBuilder(type)
        )

    private fun StandardField.readFnBuilder(type: FieldType) =
        when (type) {
            FieldType.ENUM, FieldType.MESSAGE -> stripQualification(this)
            else -> ""
        }

    private fun stripQualification(f: StandardField) =
        stripEnclosingMessageName(f.typePClass.renderName(ctx.pkg))

    private fun stripEnclosingMessageName(s: String): String {
        var stripped = s
        for (enclosing in ctx.enclosing.reversed()) {
            if (stripped.startsWith(enclosing.name)) {
                stripped = stripped.removePrefix("${enclosing.name}.")
            } else {
                break
            }
        }
        return stripped
    }

    companion object {
        fun annotateDeserializerOld(msg: Message, ctx: Context) =
            DeserializerAnnotator(msg, ctx).annotateDeserializerOld()

        fun annotateDeserializerNew(msg: Message, ctx: Context) =
            DeserializerAnnotator(msg, ctx).annotateDeserializerNew()
    }
}
