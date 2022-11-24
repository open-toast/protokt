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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.annotators.MessageSizeAnnotator.Companion.sizeOf
import com.toasttab.protokt.codegen.annotators.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.annotators.PropertyAnnotator.PropertyInfo
import com.toasttab.protokt.codegen.annotators.SerializerAnnotator.Companion.serialize
import com.toasttab.protokt.codegen.impl.bindSpaces
import com.toasttab.protokt.codegen.impl.buildFunSpec
import com.toasttab.protokt.codegen.impl.constructorProperty
import com.toasttab.protokt.codegen.impl.namedCodeBlock
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.rt.AbstractKtDeserializer
import com.toasttab.protokt.rt.AbstractKtMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import kotlin.reflect.KProperty0

class MapEntryAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private val entryInfo = resolveMapEntry(msg)
    private val keyPropertyType = entryInfo.key.className
    private val valPropertyType = entryInfo.value.className

    private fun annotateMapEntry() =
        TypeSpec.classBuilder(msg.name).apply {
            addModifiers(KModifier.PRIVATE)
            superclass(AbstractKtMessage::class)
            addProperty(constructorProperty("key", keyPropertyType))
            addProperty(constructorProperty("value", valPropertyType))
            addConstructor()
            addMessageSize()
            addSerialize()
            addDeserializer()
        }.build()

    private fun TypeSpec.Builder.addConstructor() {
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("key", keyPropertyType)
                .addParameter("value", valPropertyType)
                .build()
        )
    }

    private fun TypeSpec.Builder.addMessageSize() {
        addProperty(
            PropertySpec.builder(KtMessage::messageSize.name, Int::class)
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addCode("return sizeof(key, value)".bindSpaces())
                        .build()
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.addSerialize() {
        addFunction(
            buildFunSpec("serialize") {
                addModifiers(KModifier.OVERRIDE)
                addParameter("serializer", KtMessageSerializer::class)
                addStatement("%L", serialize(entryInfo.key, ctx))
                addStatement("%L", serialize(entryInfo.value, ctx))
            }
        )
    }

    private fun TypeSpec.Builder.addDeserializer() {
        val propInfo = annotateProperties(msg, ctx)

        addType(
            TypeSpec.companionObjectBuilder("Deserializer")
                .superclass(
                    AbstractKtDeserializer::class
                        .asTypeName()
                        .parameterizedBy(msg.typeName)
                )
                .addFunction(
                    buildFunSpec("sizeof") {
                        addParameter("key", keyPropertyType)
                        addParameter("value", valPropertyType)
                        addStatement("return %L + %L", sizeOf(entryInfo.key, ctx), sizeOf(entryInfo.value, ctx))
                    }
                )
                .addFunction(
                    buildFunSpec("deserialize") {
                        addModifiers(KModifier.OVERRIDE)
                        addParameter("deserializer", KtMessageDeserializer::class)
                        returns(msg.typeName)
                        addStatement("%L", deserializeVar(propInfo, entryInfo::key))
                        addStatement("%L", deserializeVar(propInfo, entryInfo::value))
                        beginControlFlow("while (true)")
                        beginControlFlow("when (deserializer.readTag())")
                        addStatement("%L", constructOnZero(entryInfo.value))
                        addStatement(
                            "${entryInfo.key.tag.value} -> key = %L",
                            deserialize(entryInfo.key, ctx, false)
                        )
                        addStatement(
                            "${entryInfo.value.tag.value} -> value = %L",
                            deserialize(entryInfo.value, ctx, false)
                        )
                        endControlFlow()
                        endControlFlow()
                    }
                )
                .build()
        )
    }

    private fun deserializeVar(propInfo: List<PropertyInfo>, accessor: KProperty0<StandardField>): CodeBlock {
        val field = accessor.get()
        val prop = propInfo.single { it.name == field.fieldName }

        return namedCodeBlock(
            "var ${accessor.name}" +
                if (field.type == FieldType.MESSAGE) {
                    ": %type:T"
                } else {
                    ""
                } + " = %value:L",
            mapOf(
                "type" to prop.deserializeType,
                "value" to deserializeValue(prop)
            )
        )
    }

    private fun constructOnZero(f: StandardField) =
        CodeBlock.of(
            "0 -> return ${msg.name}(key, value" +
                if (f.type == FieldType.MESSAGE) {
                    " ?: %T().build()"
                } else {
                    ""
                } + ")",
            entryInfo.value.className.nestedClass("${valPropertyType.simpleName}Dsl")
        )

    companion object {
        fun annotateMapEntry(msg: Message, ctx: Context) =
            MapEntryAnnotator(msg, ctx).annotateMapEntry()
    }
}
