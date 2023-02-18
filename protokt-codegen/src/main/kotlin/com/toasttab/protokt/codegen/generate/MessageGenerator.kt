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

package com.toasttab.protokt.codegen.generate

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.AbstractKtMessage
import com.toasttab.protokt.KtGeneratedMessage
import com.toasttab.protokt.UnknownFieldSet
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.generate.CodeGenerator.generate
import com.toasttab.protokt.codegen.generate.Deprecation.addDeprecationSuppression
import com.toasttab.protokt.codegen.generate.Deprecation.enclosingDeprecation
import com.toasttab.protokt.codegen.generate.Deprecation.handleDeprecation
import com.toasttab.protokt.codegen.generate.Deprecation.hasDeprecation
import com.toasttab.protokt.codegen.generate.Implements.handleSuperInterface
import com.toasttab.protokt.codegen.util.Message

fun generateMessage(msg: Message, ctx: Context) =
    MessageGenerator(msg, ctx).generate()

private class MessageGenerator(
    private val msg: Message,
    private val ctx: Context
) {
    fun generate() =
        if (msg.mapEntry) {
            generateMapEntry(msg, ctx)
        } else {
            val properties = annotateProperties(msg, ctx)

            TypeSpec.classBuilder(msg.className).apply {
                annotateMessageDocumentation(ctx)?.let { addKdoc(formatDoc(it)) }
                handleAnnotations()
                handleConstructor(properties)
                addTypes(annotateOneofs(msg, ctx))
                handleMessageSize()
                addFunction(generateMessageSize(msg, ctx))
                addFunction(generateSerializer(msg, ctx))
                handleEquals(properties)
                handleHashCode(properties)
                handleToString(properties)
                handleDsl(msg, properties)
                addType(generateDeserializer(msg, ctx, properties))
                addTypes(msg.nestedTypes.flatMap { generate(it, ctx) })
            }.build()
        }

    private fun TypeSpec.Builder.handleAnnotations() = apply {
        addAnnotation(
            AnnotationSpec.builder(KtGeneratedMessage::class)
                .addMember(msg.fullProtobufTypeName.embed())
                .build()
        )
        handleDeprecation(
            msg.options.default.deprecated,
            msg.options.protokt.deprecationMessage
        )
        if (suppressDeprecation()) {
            addDeprecationSuppression()
        }
    }

    private fun TypeSpec.Builder.handleConstructor(
        properties: List<PropertyInfo>
    ) = apply {
        superclass(AbstractKtMessage::class)
        addProperties(
            properties.map { property ->
                PropertySpec.builder(property.name, property.propertyType).apply {
                    initializer(property.name)
                    if (property.overrides) {
                        addModifiers(KModifier.OVERRIDE)
                    }
                    property.documentation?.let { addKdoc(formatDoc(it)) }
                    handleDeprecation(property.deprecation)
                }.build()
            }
        )
        addProperty(
            PropertySpec.builder("unknownFields", UnknownFieldSet::class)
                .initializer("unknownFields")
                .build()
        )
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addModifiers(KModifier.PRIVATE)
                .addParameters(properties.map { ParameterSpec(it.name, it.propertyType) })
                .addParameter(
                    ParameterSpec.builder("unknownFields", UnknownFieldSet::class)
                        .defaultValue("%T.empty()", UnknownFieldSet::class)
                        .build()
                )
                .build()
        )
        handleSuperInterface(msg, ctx)
    }

    private fun TypeSpec.Builder.handleMessageSize() =
        addProperty(
            PropertySpec.builder("messageSize", Int::class)
                .addModifiers(KModifier.OVERRIDE)
                .delegate("lazy { messageSize() }")
                .build()
        )

    private fun TypeSpec.Builder.handleEquals(
        properties: List<PropertyInfo>
    ) =
        addFunction(
            FunSpec.builder(Any::equals.name)
                .returns(Boolean::class)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("other", Any::class.asTypeName().copy(nullable = true))
                .addCode(
                    if (properties.isEmpty()) {
                        CodeBlock.of(
                            "return other is %T && other.unknownFields == unknownFields".bindSpaces(),
                            msg.className
                        )
                    } else {
                        buildCodeBlock {
                            add(
                                "return \nother is %T &&\n".bindSpaces(),
                                msg.className
                            )
                            equalsLines(properties).forEach(::add)
                            add("other.unknownFields == unknownFields".bindSpaces())
                        }
                    }
                )
                .build()
        )

    private fun equalsLines(properties: List<PropertyInfo>) =
        properties.map {
            CodeBlock.of("other.%N == %N &&\n".bindSpaces(), it.name, it.name)
        }

    private fun TypeSpec.Builder.handleHashCode(
        properties: List<PropertyInfo>
    ) =
        addFunction(
            FunSpec.builder(Any::hashCode.name)
                .returns(Int::class)
                .addModifiers(KModifier.OVERRIDE)
                .addCode(
                    if (properties.isEmpty()) {
                        CodeBlock.of("return unknownFields.hashCode()")
                    } else {
                        buildCodeBlock {
                            addStatement("var result = unknownFields.hashCode()")
                            hashCodeLines(properties).forEach(::add)
                            addStatement("return result")
                        }
                    }
                )
                .build()
        )

    private fun hashCodeLines(properties: List<PropertyInfo>) =
        properties.map {
            CodeBlock.of("result = 31 * result + %N.hashCode()\n".bindSpaces(), it.name)
        }

    private fun TypeSpec.Builder.handleToString(
        properties: List<PropertyInfo>
    ) =
        addFunction(
            FunSpec.builder(Any::toString.name)
                .returns(String::class)
                .addModifiers(KModifier.OVERRIDE)
                .addCode(
                    if (properties.isEmpty()) {
                        CodeBlock.of(
                            "return \"%L(unknownFields=\$unknownFields)\"",
                            msg.className.simpleName
                        )
                    } else {
                        buildCodeBlock {
                            add(
                                "return \"%L(\" +\n",
                                msg.className.simpleName
                            )
                            toStringLines(properties).forEach(::add)
                            add("\"unknownFields=${"$"}unknownFields)\"")
                        }
                    }
                )
                .build()
        )

    private fun toStringLines(properties: List<PropertyInfo>) =
        properties.map {
            CodeBlock.of("\"%N=\$%N, \" +\n".bindSpaces(), it.name, it.name)
        }

    private fun suppressDeprecation() =
        msg.hasDeprecation && (!enclosingDeprecation(ctx) || messageIsTopLevel())

    private fun messageIsTopLevel() =
        msg.className.simpleNames.size == 1
}

fun formatDoc(lines: List<String>) =
    CodeBlock.of(
        "%L", // escape the entire comment block
        lines.joinToString(" ") {
            if (it.isBlank()) {
                "\n\n"
            } else {
                it.removePrefix(" ")
            }
        }
    )
