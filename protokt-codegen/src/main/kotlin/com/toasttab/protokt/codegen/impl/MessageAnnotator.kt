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

import arrow.core.firstOrNone
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.impl.Annotator.annotate
import com.toasttab.protokt.codegen.impl.Deprecation.addDeprecationSuppression
import com.toasttab.protokt.codegen.impl.Deprecation.enclosingDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.handleDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.hasDeprecation
import com.toasttab.protokt.codegen.impl.DeserializerAnnotator.Companion.annotateDeserializer
import com.toasttab.protokt.codegen.impl.Implements.handleSuperInterface
import com.toasttab.protokt.codegen.impl.MapEntryAnnotator.Companion.annotateMapEntry
import com.toasttab.protokt.codegen.impl.MessageDocumentationAnnotator.annotateMessageDocumentation
import com.toasttab.protokt.codegen.impl.MessageSizeAnnotator.Companion.annotateMessageSizeNew
import com.toasttab.protokt.codegen.impl.OneofAnnotator.Companion.annotateOneofs
import com.toasttab.protokt.codegen.impl.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.impl.SerializerAnnotator.Companion.annotateSerializerNew
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.UnknownFieldSet

class MessageAnnotator
private constructor(
    private val msg: Message,
    private val ctx: Context
) {
    fun annotateMessage() =
        if (msg.mapEntry) {
            annotateMapEntry(msg, ctx)
        } else {
            val properties = annotateProperties(msg, ctx)

            TypeSpec.classBuilder(msg.name)
                .handleAnnotations()
                .apply {
                    val doc = annotateMessageDocumentation(ctx)
                    if (doc.isNotEmpty()) {
                        addKdoc(formatDoc(doc))
                    }
                }
                .apply {
                    if (suppressDeprecation()) {
                        addDeprecationSuppression()
                    }
                }
                .handleConstructor(properties)
                .addTypes(annotateOneofs(msg, ctx))
                .handleMessageSize()
                .addFunction(annotateMessageSizeNew(msg, ctx))
                .addFunction(annotateSerializerNew(msg, ctx))
                .handleEquals(properties)
                .handleHashCode(properties)
                .handleToString(properties)
                .handleDsl(msg, properties)
                .addType(annotateDeserializer(msg, ctx))
                .addTypes(msg.nestedTypes.flatMap { annotate(it, ctx) })
                .build()
        }

    private fun TypeSpec.Builder.handleAnnotations() = apply {
        addAnnotation(
            AnnotationSpec.builder(KtGeneratedMessage::class)
                .addMember(msg.fullProtobufTypeName.embed())
                .build()
        )
        handleDeprecation(msg.options.default.deprecated, msg.options.protokt.deprecationMessage)
    }

    private fun TypeSpec.Builder.handleConstructor(
        properties: List<PropertyInfo>
    ) = apply {
        addSuperinterface(KtMessage::class)
        addProperties(
            properties.map {
                PropertySpec.builder(it.name.removePrefix("`").removeSuffix("`"), it.propertyType)
                    .initializer(it.name)
                    .apply {
                        if (it.overrides) {
                            addModifiers(KModifier.OVERRIDE)
                        }
                    }
                    .apply {
                        if (it.documentation.isNotEmpty()) {
                            addKdoc(formatDoc(it.documentation))
                        }
                    }
                    .handleDeprecation(it.deprecation)
                    .build()
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
                .addParameters(
                    properties.map {
                        ParameterSpec(it.name.removePrefix("`").removeSuffix("`"), it.propertyType)
                    }
                )
                .addParameter(
                    ParameterSpec.builder("unknownFields", UnknownFieldSet::class)
                        .defaultValue("UnknownFieldSet.empty()")
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
                        "return other is ${msg.name} && other.unknownFields == unknownFields".bindSpaces()
                    } else {
                        """
                            |return other is ${msg.name} &&
                            |${equalsLines(properties)}
                            |    other.unknownFields == unknownFields
                        """.bindMargin()
                    }
                )
                .build()
        )

    private fun equalsLines(properties: List<PropertyInfo>) =
        properties.joinToString("\n") { "    other.${it.name} == ${it.name} &&" }

    private fun TypeSpec.Builder.handleHashCode(
        properties: List<PropertyInfo>
    ) =
        addFunction(
            FunSpec.builder(Any::hashCode.name)
                .returns(Int::class)
                .addModifiers(KModifier.OVERRIDE)
                .addCode(
                    if (properties.isEmpty()) {
                        "return unknownFields.hashCode()"
                    } else {
                        """
                            |var result = unknownFields.hashCode()
                            |${hashCodeLines(properties)}
                            |return result
                        """.bindMargin()
                    }
                )
                .build()
        )

    private fun hashCodeLines(properties: List<PropertyInfo>) =
        properties.joinToString("\n") {
            "result = 31 * result + ${it.name}.hashCode()"
        }.bindSpaces()

    private fun TypeSpec.Builder.handleToString(
        properties: List<PropertyInfo>
    ) =
        addFunction(
            FunSpec.builder(Any::toString.name)
                .returns(String::class)
                .addModifiers(KModifier.OVERRIDE)
                .addCode(
                    if (properties.isEmpty()) {
                        "return \"${msg.name}(unknownFields=\$unknownFields)\""
                    } else {
                        """
                            |return "${msg.name}(" +
                            |${toStringLines(properties)}
                            |    "unknownFields=${"$"}unknownFields)"
                        """.bindMargin()
                    }
                )
                .build()
        )

    private fun toStringLines(properties: List<PropertyInfo>) =
        properties.joinToString("\n") {
            "    \"${it.name}=\$${it.name}\" +"
        }.bindSpaces()

    private fun suppressDeprecation() =
        msg.hasDeprecation && (!enclosingDeprecation(ctx) || messageIsTopLevel())

    private fun messageIsTopLevel() =
        ctx.enclosing.firstOrNone().fold({ false }, { it == msg })

    companion object {
        const val IDEAL_MAX_WIDTH = 100

        fun annotateMessage(msg: Message, ctx: Context) =
            MessageAnnotator(msg, ctx).annotateMessage()
    }
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
