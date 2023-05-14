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

package com.toasttab.protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.v1.AbstractKtDeserializer
import com.toasttab.protokt.v1.AbstractKtMessage
import com.toasttab.protokt.v1.KtMessage
import com.toasttab.protokt.v1.KtMessageDeserializer
import com.toasttab.protokt.v1.KtMessageSerializer
import com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.v1.codegen.util.DESERIALIZER
import com.toasttab.protokt.v1.codegen.util.FieldType
import com.toasttab.protokt.v1.codegen.util.MapEntry
import com.toasttab.protokt.v1.codegen.util.Message
import com.toasttab.protokt.v1.codegen.util.StandardField
import kotlin.reflect.KProperty0

fun generateMapEntry(msg: Message, ctx: Context) =
    MapEntryGenerator(msg, ctx).generate()

private class MapEntryGenerator(
    private val msg: Message,
    private val ctx: Context
) {
    private val key = msg.fields[0] as StandardField
    private val value = msg.fields[1] as StandardField

    fun generate() =
        TypeSpec.classBuilder(msg.className).apply {
            addModifiers(KModifier.PRIVATE)
            superclass(AbstractKtMessage::class)
            addProperty(constructorProperty("key", key.className))
            addProperty(constructorProperty("value", value.className))
            addConstructor()
            addMessageSize()
            addSerialize()
            addDeserializer()
        }.build()

    private fun TypeSpec.Builder.addConstructor() {
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("key", key.className)
                .addParameter("value", value.className)
                .build()
        )
    }

    private fun TypeSpec.Builder.addMessageSize() {
        addProperty(
            PropertySpec.builder(KtMessage::messageSize.name, Int::class)
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addCode(
                            "return·%L",
                            sizeOfCall(
                                MapEntry(key, value),
                                CodeBlock.of("key"),
                                CodeBlock.of("value")
                            )
                        )
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
                addStatement("%L", serialize(key, ctx))
                addStatement("%L", serialize(value, ctx))
            }
        )
    }

    private fun TypeSpec.Builder.addDeserializer() {
        val propInfo = annotateProperties(msg, ctx)

        addType(
            TypeSpec.companionObjectBuilder(DESERIALIZER)
                .superclass(
                    AbstractKtDeserializer::class
                        .asTypeName()
                        .parameterizedBy(msg.className)
                )
                .addFunction(
                    buildFunSpec("entrySize") {
                        if (key.type.sizeFn is FieldType.Method) {
                            addParameter("key", key.className)
                        }
                        if (value.type.sizeFn is FieldType.Method) {
                            addParameter("value", value.className)
                        }
                        addStatement("return %L + %L", sizeOf(key, ctx), sizeOf(value, ctx))
                    }
                )
                .addFunction(
                    buildFunSpec("deserialize") {
                        addModifiers(KModifier.OVERRIDE)
                        addParameter("deserializer", KtMessageDeserializer::class)
                        returns(msg.className)
                        addStatement("%L", deserializeVar(propInfo, ::key))
                        addStatement("%L", deserializeVar(propInfo, ::value))
                        addCode("\n")
                        beginControlFlow("while (true)")
                        beginControlFlow("when (deserializer.readTag())")
                        addStatement("%L", constructOnZero(value))
                        addStatement(
                            "${key.tag.value} -> key = %L",
                            deserialize(key, ctx)
                        )
                        addStatement(
                            "${value.tag.value} -> value = %L",
                            deserialize(value, ctx)
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
                "value" to deserializeVarInitialState(prop)
            )
        )
    }

    private fun constructOnZero(f: StandardField) =
        buildCodeBlock {
            add("0 -> return %T(key, value", msg.className)
            if (f.type == FieldType.MESSAGE) {
                add(
                    CodeBlock.of(
                        " ?: %T().build()",
                        value.className.nestedClass("${value.className.simpleName}Dsl")
                    )
                )
            }
            add(")")
        }
}

fun sizeOfCall(mapEntry: MapEntry, keyStr: CodeBlock, valueStr: CodeBlock) =
    if (mapEntry.key.type.sizeFn is FieldType.Method) {
        if (mapEntry.value.type.sizeFn is FieldType.Method) {
            CodeBlock.of("entrySize(%L,·%L)", keyStr, valueStr)
        } else {
            CodeBlock.of("entrySize(%L)", keyStr)
        }
    } else {
        if (mapEntry.value.type.sizeFn is FieldType.Method) {
            CodeBlock.of("entrySize(%L)", valueStr)
        } else {
            CodeBlock.of("entrySize()")
        }
    }
