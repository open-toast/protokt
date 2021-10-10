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

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.impl.DeserializerAnnotator.Companion.annotateDeserializer
import com.toasttab.protokt.codegen.impl.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.impl.SerializerAnnotator.Companion.annotateSerializerOld
import com.toasttab.protokt.codegen.impl.SizeofAnnotator.Companion.annotateSizeof
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.template.Entry.Entry
import com.toasttab.protokt.codegen.template.Entry.Entry.DeserializerInfo
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.codegen.template.Message.Message as MessageTemplate

class MapEntryAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    private fun annotateMapEntry(): TypeSpec {
        val entryInfo = resolveMapEntry(msg)
        val desInfo = annotateDeserializer(msg, ctx)
        val sizeInfo = annotateSizeof(msg, ctx)
        val serInfo = annotateSerializerOld(msg, ctx)
        val propInfo = annotateProperties(msg, ctx)

        val keyProp =
            prop(
                entryInfo.key,
                entryInfo.key.unqualifiedTypeName,
                sizeInfo,
                serInfo,
                desInfo,
                propInfo
            )

        val valProp =
            prop(
                entryInfo.value,
                entryInfo.value.typePClass.renderName(ctx.pkg),
                sizeInfo,
                serInfo,
                desInfo,
                propInfo
            )

        return TypeSpec.classBuilder(msg.name)
            .addModifiers(KModifier.PRIVATE)
            .addSuperinterface(KtMessage::class)
            .addProperty(
                PropertySpec.builder("key",TypeVariableName(entryInfo.key.unqualifiedTypeName))
                    .initializer("key")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("value",TypeVariableName(entryInfo.value.unqualifiedTypeName))
                    .initializer("value")
                    .build()
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        "key",
                        TypeVariableName(entryInfo.key.unqualifiedTypeName)
                    )
                    .addParameter(
                        "value",
                        TypeVariableName(entryInfo.value.unqualifiedTypeName)
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder(KtMessage::messageSize.name, Int::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode("return sizeof(key, value)")
                            .build()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("serialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("serializer", KtMessageSerializer::class)
                    .addCode(serInfo.consequent(entryInfo.key))
                    .addCode("\n")
                    .addCode(serInfo.consequent(entryInfo.value))
                    .build()
            )
            .addType(
                TypeSpec.companionObjectBuilder("Deserializer")
                    .addSuperinterface(
                        KtDeserializer::class
                            .asTypeName()
                            .parameterizedBy(TypeVariableName(msg.name)))
                    .addFunction(
                        FunSpec.builder("sizeof")
                            .addParameter("key", TypeVariableName(entryInfo.key.unqualifiedTypeName))
                            .addParameter("value", TypeVariableName(entryInfo.value.unqualifiedTypeName))
                            .addCode(
                                "return ${sizeInfo.consequent(entryInfo.key)} + ${sizeInfo.consequent(entryInfo.value)}".replace(" ", "·")
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("deserialize")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("deserializer", KtMessageDeserializer::class)
                            .returns(TypeVariableName(msg.name))
                            .addCode(
                                """
                                    var key${deserializeVar(entryInfo.key, propInfo.single(entryInfo.key))}
                                    var value${deserializeVar(entryInfo.value, propInfo.single(entryInfo.value))}
                            
                                    while (true) {
                                      when (deserializer.readTag()) {
                                        0 -> return ${msg.name}(key, value${orDefault(entryInfo.value, valProp)})
                                        ${keyProp.deserialize.tag} -> key = ${keyProp.deserialize.assignment}
                                        ${valProp.deserialize.tag} -> value = ${valProp.deserialize.assignment}
                                      }
                                    }
                                """.trimIndent()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun deserializeVar(f: StandardField, p: PropertyInfo) =
        if (f.type == FieldType.MESSAGE) {
            ": " + deserializeType(p)
        } else {
            ""
        } + " = " + deserializeValue(p)

    private fun orDefault(f: StandardField, p: Entry.PropertyInfo) =
        if (f.type == FieldType.MESSAGE) {
            " ?: " + p.propertyType + " {}"
        } else {
            ""
        }

    private fun prop(
        f: StandardField,
        type: String,
        sizeofInfo: List<MessageTemplate.SizeofInfo>,
        serializerInfo: List<MessageTemplate.SerializerInfo>,
        deserializerInfo: List<MessageTemplate.DeserializerInfo>,
        propInfo: List<PropertyInfo>
    ) =
        Entry.PropertyInfo(
            propertyType = type,
            messageType = f.type.toString(),
            deserializeType = propInfo.single(f).deserializeType,
            sizeof = sizeofInfo.consequent(f),
            serialize = serializerInfo.consequent(f),
            defaultValue = propInfo.single(f).defaultValue,
            deserialize = deserialize(deserializerInfo, f)
        )

    private fun deserialize(
        deserializerInfo: List<MessageTemplate.DeserializerInfo>,
        f: StandardField
    ) =
        deserializerInfo.single(f).let {
            DeserializerInfo(
                tag = it.tag,
                assignment = it.assignment.value
            )
        }

    private fun <T : MessageTemplate.FieldInfo> List<T>.single(
        f: StandardField
    ) =
        single { it.name == f.fieldName }

    private fun List<MessageTemplate.FieldWriteInfo>.consequent(
        f: StandardField
    ) =
        single(f).conditionals.single().consequent

    companion object {
        fun annotateMapEntry(msg: Message, ctx: Context) =
            MapEntryAnnotator(msg, ctx).annotateMapEntry()
    }
}
