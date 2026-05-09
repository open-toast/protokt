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
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.AbstractDeserializer
import protokt.v1.AbstractMessage
import protokt.v1.Bytes
import protokt.v1.Reader
import protokt.v1.StringConverter
import protokt.v1.UnknownFieldSet
import protokt.v1.Writer
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Wrapper.interceptDefaultValue
import protokt.v1.codegen.generate.Wrapper.interceptTypeName
import protokt.v1.codegen.generate.Wrapper.wrapField
import protokt.v1.codegen.util.DESERIALIZER
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.SizeFn
import protokt.v1.codegen.util.StandardField
import protokt.v1.codegen.util.defaultValue
import protokt.v1.codegen.util.sizeFn
import protokt.v1.reflect.FieldType
import kotlin.reflect.KProperty0

internal fun generateMapEntry(msg: Message, ctx: Context, mapCachingInfo: MapCachingInfo? = null) =
    MapEntryGenerator(msg, ctx, mapCachingInfo).generate()

private class MapEntryGenerator(
    private val msg: Message,
    private val ctx: Context,
    private val mapCachingInfo: MapCachingInfo?
) {
    private val key = msg.fields[0] as StandardField
    private val value = msg.fields[1] as StandardField

    private val keyTypeName: TypeName =
        if (mapCachingInfo?.keyWireTypeName != null) {
            mapCachingInfo.keyWireTypeName
        } else {
            key.interceptTypeName(ctx)
        }

    private val valueTypeName: TypeName =
        if (mapCachingInfo?.valueWireTypeName != null) {
            mapCachingInfo.valueWireTypeName
        } else {
            value.interceptTypeName(ctx)
        }

    private val keyProp = constructorProperty("key", keyTypeName, false)
    private val valProp = constructorProperty("value", valueTypeName, false)

    private val propInfo = annotateProperties(msg, ctx)
    private val keyPropInfo = propInfo[0]
    private val valPropInfo = propInfo[1]

    fun generate() =
        TypeSpec.classBuilder(msg.className).apply {
            addModifiers(KModifier.PRIVATE)
            superclass(AbstractMessage::class)
            addProperty(keyProp)
            addProperty(valProp)
            addProperty(
                PropertySpec.builder("unknownFields", UnknownFieldSet::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%T.empty()", UnknownFieldSet::class)
                    .build()
            )
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
        addFunction(
            buildFunSpec(protokt.v1.Message::serializedSize.name) {
                returns(Int::class)
                addModifiers(KModifier.OVERRIDE)
                addStatement(
                    "return·%L",
                    sizeOfCall(
                        key,
                        value,
                        CodeBlock.of("key"),
                        CodeBlock.of("value")
                    )
                )
            }
        )
    }

    private fun TypeSpec.Builder.addSerialize() {
        addFunction(
            buildFunSpec("serialize") {
                addModifiers(KModifier.OVERRIDE)
                addParameter(WRITER, Writer::class)
                if (mapCachingInfo != null) {
                    // Wire-typed fields: write directly without unwrapping
                    addStatement(
                        "$WRITER.writeTag(${key.tag.value}u).%L",
                        key.writeWire(CodeBlock.of("key"))
                    )
                    addStatement(
                        "$WRITER.writeTag(${value.tag.value}u).%L",
                        value.writeWire(CodeBlock.of("value"))
                    )
                } else {
                    addStatement("%L", serialize(key, ctx, keyProp, mapEntry = true))
                    addStatement("%L", serialize(value, ctx, valProp, mapEntry = true))
                }
            }
        )
    }

    private fun StandardField.writeWire(value: CodeBlock) =
        CodeBlock.of("%L(%L)", type.writeFn, value)

    private fun TypeSpec.Builder.addDeserializer() {
        addType(
            TypeSpec.companionObjectBuilder(DESERIALIZER)
                .superclass(
                    AbstractDeserializer::class
                        .asTypeName()
                        .parameterizedBy(msg.className)
                )
                .addFunction(
                    buildFunSpec("entrySize") {
                        returns(Int::class)
                        if (mapCachingInfo != null) {
                            // Wire-typed entrySize: parameters use wire types, no unwrapping
                            if (key.type.sizeFn is SizeFn.Method) {
                                addParameter("key", keyTypeName)
                            }
                            if (value.type.sizeFn is SizeFn.Method) {
                                addParameter("value", valueTypeName)
                            }
                            addStatement(
                                "return %L + %L",
                                wireSizeOf(key, "key"),
                                wireSizeOf(value, "value")
                            )
                        } else {
                            if (key.type.sizeFn is SizeFn.Method) {
                                addParameter("key", keyTypeName)
                            }
                            if (value.type.sizeFn is SizeFn.Method) {
                                addParameter("value", valueTypeName)
                            }
                            addStatement("return %L + %L", sizeOf(key, ctx, mapEntry = true), sizeOf(value, ctx, mapEntry = true))
                        }
                    }
                )
                .addFunction(
                    buildFunSpec("deserialize") {
                        addModifiers(KModifier.OVERRIDE)
                        addParameter(READER, Reader::class)
                        returns(msg.className)
                        if (mapCachingInfo != null) {
                            addStatement("%L", wireDeserializeVar(key, "key", mapCachingInfo.keyWrapped))
                            addStatement("%L", wireDeserializeVar(value, "value", mapCachingInfo.valueWrapped))
                            addCode("\n")
                            beginControlFlow("while (true)")
                            beginControlFlow("when ($READER.readTag())")
                            addStatement("%L", wireConstructOnZero())
                            addStatement(
                                "${key.tag.value}u -> key = %L",
                                wireDeserialize(key, mapCachingInfo.keyWrapped, mapCachingInfo.keyIsString)
                            )
                            addStatement(
                                "${value.tag.value}u -> value = %L",
                                wireDeserialize(value, mapCachingInfo.valueWrapped, mapCachingInfo.valueIsString)
                            )
                            endControlFlow()
                            endControlFlow()
                        } else {
                            addStatement("%L", deserializeVar(keyPropInfo, ::key))
                            addStatement("%L", deserializeVar(valPropInfo, ::value))
                            addCode("\n")
                            beginControlFlow("while (true)")
                            beginControlFlow("when ($READER.readTag())")
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
                    }
                )
                .build()
        )
    }

    // Wire-type helper: size of a field using wire types (no unwrapping)
    private fun wireSizeOf(field: StandardField, varName: String): CodeBlock {
        val tagSize = CodeBlock.of("%M(${field.tag}u)", sizeOf)
        return when (val fn = field.type.sizeFn) {
            is SizeFn.Const -> CodeBlock.of("%L + %L", tagSize, fn.size)
            is SizeFn.Method -> CodeBlock.of("%L + %M(%L)", tagSize, fn.method, varName)
        }
    }

    // Wire-type helper: declare deserialize var with wire type
    private fun wireDeserializeVar(field: StandardField, varName: String, isWrapped: Boolean): CodeBlock {
        val wireType = if (isWrapped) {
            if (varName == "key") keyTypeName else valueTypeName
        } else {
            if (varName == "key") keyTypeName else valueTypeName
        }
        val needsTypeAnnotation = field.type == FieldType.Message || isWrapped ||
            (field.type == FieldType.String && (if (varName == "key") mapCachingInfo!!.keyIsString else mapCachingInfo!!.valueIsString))
        return when {
            needsTypeAnnotation ->
                CodeBlock.of("var %L: %T = %L", varName, wireType.copy(nullable = true), "null")

            field.type == FieldType.Enum ->
                CodeBlock.of("var %L = %T.deserialize(0)", varName, field.className)

            else ->
                CodeBlock.of("var %L = %L", varName, field.type.defaultValue)
        }
    }

    // Wire-type helper: deserialize a field value (no wrapping)
    private fun wireDeserialize(field: StandardField, isWrapped: Boolean, isString: Boolean): CodeBlock {
        if (isString) {
            // Plain string field: use readValidatedBytes for lazy string allocation
            return CodeBlock.of("%T.readValidatedBytes($READER)", StringConverter::class)
        }
        // Wrapped field or other: read the wire value directly
        return CodeBlock.of("$READER.%L", field.readFn())
    }

    // Wire-type helper: construct on zero tag
    private fun wireConstructOnZero(): CodeBlock =
        buildCodeBlock {
            add("0u -> return %T(key", msg.className)
            val keyIsConverted = mapCachingInfo!!.keyWrapped
            if (keyIsConverted) {
                // Wire default for key
                val wireDefault = wireDefaultForField(key, mapCachingInfo.keyIsString)
                add(" ?: %L", wireDefault)
            } else if (keyPropInfo.nullable || keyPropInfo.wrapped) {
                add(" ?: %L", keyPropInfo.defaultValue)
            }
            add(", value")
            val valueIsConverted = mapCachingInfo.valueWrapped
            if (valueIsConverted) {
                val wireDefault = wireDefaultForField(value, mapCachingInfo.valueIsString)
                if (value.type == FieldType.Message) {
                    // Message-typed wrapped values are nullable
                    add(" ?: %L", wireDefault)
                } else {
                    add(" ?: %L", wireDefault)
                }
            } else if (valPropInfo.nullable) {
                if (valPropInfo.wrapped) {
                    if (value.type == FieldType.Message) {
                        add(" ?: %L", wrapField(value, ctx, CodeBlock.of("%T {}", value.className)))
                    } else {
                        add(" ?: %L", valPropInfo.defaultValue)
                    }
                } else {
                    if (value.type == FieldType.Message) {
                        add(" ?: %T {}", value.className)
                    } else {
                        add(" ?: %L", interceptDefaultValue(value, valPropInfo.defaultValue, ctx))
                    }
                }
            } else if (value.type == FieldType.Message) {
                // Message-typed non-wrapped values: wireDeserializeVar makes them nullable
                add(" ?: %T {}", value.className)
            } else if (valPropInfo.wrapped) {
                add(" ?: %L", valPropInfo.defaultValue)
            }
            add(")")
        }

    private fun wireDefaultForField(field: StandardField, isString: Boolean): CodeBlock =
        when {
            isString -> CodeBlock.of("%T.empty()", Bytes::class)
            field.type == FieldType.Message -> CodeBlock.of("%T {}", field.className)
            field.type == FieldType.Enum -> CodeBlock.of("%T.deserialize(0)", field.className)
            else -> field.type.defaultValue
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
