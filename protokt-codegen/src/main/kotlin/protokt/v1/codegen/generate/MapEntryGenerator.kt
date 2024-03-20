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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.AbstractKtDeserializer
import protokt.v1.AbstractKtMessage
import protokt.v1.KtMessage
import protokt.v1.KtMessageDeserializer
import protokt.v1.KtMessageSerializer
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Wrapper.interceptDefaultValue
import protokt.v1.codegen.generate.Wrapper.interceptTypeName
import protokt.v1.codegen.generate.Wrapper.wrapField
import protokt.v1.codegen.util.DESERIALIZER
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.SizeFn
import protokt.v1.codegen.util.StandardField
import protokt.v1.codegen.util.sizeFn
import protokt.v1.reflect.FieldType
import kotlin.reflect.KProperty0

internal fun generateMapEntry(msg: Message, ctx: Context) =
    MapEntryGenerator(msg, ctx).generate()

private class MapEntryGenerator(
    private val msg: Message,
    private val ctx: Context
) {
    private val key = msg.fields[0] as StandardField
    private val value = msg.fields[1] as StandardField

    private val keyTypeName = key.interceptTypeName(ctx)
    private val valueTypeName = value.interceptTypeName(ctx)

    private val keyProp = constructorProperty("key", keyTypeName, false)
    private val valProp = constructorProperty("value", valueTypeName, false)

    private val propInfo = annotateProperties(msg, ctx)
    private val keyPropInfo = propInfo[0]
    private val valPropInfo = propInfo[1]

    fun generate() =
        TypeSpec.classBuilder(msg.className).apply {
            addModifiers(KModifier.PRIVATE)
            superclass(AbstractKtMessage::class)
            addProperty(keyProp)
            addProperty(valProp)
            addConstructor()
            addMessageSize()
            addSerialize()
            addDeserializer()
        }.build()

    private fun TypeSpec.Builder.addConstructor() {
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("key", keyTypeName)
                .addParameter("value", valueTypeName)
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
                                key,
                                value,
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
                addStatement("%L", serialize(key, ctx, keyProp))
                addStatement("%L", serialize(value, ctx, valProp))
            }
        )
    }

    private fun TypeSpec.Builder.addDeserializer() {
        addType(
            TypeSpec.companionObjectBuilder(DESERIALIZER)
                .superclass(
                    AbstractKtDeserializer::class
                        .asTypeName()
                        .parameterizedBy(msg.className)
                )
                .addFunction(
                    buildFunSpec("entrySize") {
                        returns(Int::class)
                        if (key.type.sizeFn is SizeFn.Method) {
                            addParameter("key", keyTypeName)
                        }
                        if (value.type.sizeFn is SizeFn.Method) {
                            addParameter("value", valueTypeName)
                        }
                        addStatement("return %L + %L", sizeOf(key, ctx), sizeOf(value, ctx))
                    }
                )
                .addFunction(
                    buildFunSpec("deserialize") {
                        addModifiers(KModifier.OVERRIDE)
                        addParameter("deserializer", KtMessageDeserializer::class)
                        returns(msg.className)
                        addStatement("%L", deserializeVar(keyPropInfo, ::key))
                        addStatement("%L", deserializeVar(valPropInfo, ::value))
                        addCode("\n")
                        beginControlFlow("while (true)")
                        beginControlFlow("when (deserializer.readTag())")
                        addStatement("%L", constructOnZero())
                        addStatement(
                            "${key.tag.value}u -> key = %L",
                            deserialize(key, ctx)
                        )
                        addStatement(
                            "${value.tag.value}u -> value = %L",
                            deserialize(value, ctx)
                        )
                        endControlFlow()
                        endControlFlow()
                    }
                )
                .build()
        )
    }

    private fun deserializeVar(prop: PropertyInfo, accessor: KProperty0<StandardField>): CodeBlock {
        val field = accessor.get()

        return namedCodeBlock(
            "var ${accessor.name}" +
                if (field.type == FieldType.Message || prop.wrapped || prop.nullable) {
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

    private fun constructOnZero() =
        buildCodeBlock {
            add("0u -> return %T(key", msg.className)
            if (keyPropInfo.nullable || keyPropInfo.wrapped) {
                add("?: %L", keyPropInfo.defaultValue)
            }
            add(", value")
            if (valPropInfo.nullable) {
                if (valPropInfo.wrapped) {
                    if (value.type == FieldType.Message) {
                        add("?: %L", wrapField(value, ctx, CodeBlock.of("%T {}", value.className)))
                    } else {
                        add("?: %L", valPropInfo.defaultValue)
                    }
                } else {
                    if (value.type == FieldType.Message) {
                        add("?: %T {}", value.className)
                    } else {
                        add("?: %L", interceptDefaultValue(value, valPropInfo.defaultValue, ctx))
                    }
                }
            } else {
                if (valPropInfo.wrapped) {
                    add("?: %L", valPropInfo.defaultValue)
                }
            }
            add(")")
        }
}

internal fun sizeOfCall(key: StandardField, value: StandardField, keyStr: CodeBlock, valueStr: CodeBlock) =
    if (key.type.sizeFn is SizeFn.Method) {
        if (value.type.sizeFn is SizeFn.Method) {
            CodeBlock.of("entrySize(%L,·%L)", keyStr, valueStr)
        } else {
            CodeBlock.of("entrySize(%L)", keyStr)
        }
    } else {
        if (value.type.sizeFn is SizeFn.Method) {
            CodeBlock.of("entrySize(%L)", valueStr)
        } else {
            CodeBlock.of("entrySize()")
        }
    }
