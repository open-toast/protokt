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
import com.toasttab.protokt.codegen.impl.bindIndent
import com.toasttab.protokt.codegen.impl.bindSpaces
import com.toasttab.protokt.codegen.impl.constructorProperty
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.rt.AbstractKtDeserializer
import com.toasttab.protokt.rt.AbstractKtMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer

class MapEntryAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private val entryInfo = resolveMapEntry(msg)
    private val keyPropertyType = entryInfo.key.typePClass.toTypeName()
    private val valPropertyType = entryInfo.value.typePClass.toTypeName()

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
            FunSpec.builder("serialize")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("serializer", KtMessageSerializer::class)
                .addCode(serialize(entryInfo.key, ctx))
                .addCode("\n")
                .addCode(serialize(entryInfo.value, ctx))
                .build()
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
                    FunSpec.builder("sizeof")
                        .addParameter("key", keyPropertyType)
                        .addParameter("value", valPropertyType)
                        .addCode(
                            "return ${sizeOf(entryInfo.key, ctx)} + ${sizeOf(entryInfo.value, ctx)}".bindSpaces()
                        )
                        .build()
                )
                .addFunction(
                    FunSpec.builder("deserialize")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("deserializer", KtMessageDeserializer::class)
                        .returns(msg.typeName)
                        .addNamedCode(
                            """
                                var key${deserializeVar(entryInfo.key, propInfo.single { entryInfo.key.fieldName == it.name })}
                                var value${deserializeVar(entryInfo.value, propInfo.single { entryInfo.value.fieldName == it.name })}
                        
                                while (true) {
                                  when (deserializer.readTag()) {
                                    0 -> return ${msg.name}(key, value${orDefault(entryInfo.value)})
                                    ${entryInfo.key.tag.value} -> key = ${deserializeString(entryInfo.key, ctx, false)}
                                    ${entryInfo.value.tag.value} -> value = ${deserializeString(entryInfo.value, ctx, false)}
                                  }
                                }
                            """.bindIndent(),
                            mapOf("valueClassDsl" to entryInfo.value.typePClass.nest("${valPropertyType.simpleName}Dsl").toTypeName())
                        )
                        .build()
                )
                .build()
        )
    }

    private fun deserializeVar(f: StandardField, p: PropertyInfo) =
        if (f.type == FieldType.MESSAGE) {
            ": " + deserializeType(p)
        } else {
            ""
        } + " = " + deserializeValue(p)

    private fun orDefault(f: StandardField) =
        if (f.type == FieldType.MESSAGE) {
            " ?: %valueClassDsl:T().build()"
        } else {
            ""
        }

    companion object {
        fun annotateMapEntry(msg: Message, ctx: Context) =
            MapEntryAnnotator(msg, ctx).annotateMapEntry()
    }
}
