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
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.impl.Annotator.annotate
import com.toasttab.protokt.codegen.impl.Deprecation.enclosingDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.hasDeprecation
import com.toasttab.protokt.codegen.impl.Deprecation.renderOptions
import com.toasttab.protokt.codegen.impl.DeserializerAnnotator.Companion.annotateDeserializer
import com.toasttab.protokt.codegen.impl.Implements.doesImplement
import com.toasttab.protokt.codegen.impl.Implements.implements
import com.toasttab.protokt.codegen.impl.MapEntryAnnotator.Companion.annotateMapEntry
import com.toasttab.protokt.codegen.impl.MessageDocumentationAnnotator.annotateMessageDocumentation
import com.toasttab.protokt.codegen.impl.OneofAnnotator.Companion.annotateOneofs
import com.toasttab.protokt.codegen.impl.PropertyAnnotator.Companion.annotateProperties
import com.toasttab.protokt.codegen.impl.SerializerAnnotator.Companion.annotateSerializerNew
import com.toasttab.protokt.codegen.impl.MessageSizeAnnotator.Companion.annotateMessageSizeNew
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.template.Message.Message.MessageInfo
import com.toasttab.protokt.codegen.template.Message.Message.Options
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.codegen.template.Message.Message as MessageTemplate

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
            val messageInfo = messageInfo()
            MessageTemplate.render(
                message = messageInfo(),
                properties = annotateProperties(msg, ctx),
                oneofs = annotateOneofs(msg, ctx),
                sizeof = listOf(), // annotateMessageSizeOld(msg, ctx),
                serialize = listOf(), // annotateSerializer(msg, ctx),
                deserialize = annotateDeserializer(msg, ctx),
                nested = listOf(), // nestedTypes(),
                options = options()
            )
            TypeSpec.classBuilder(msg.name)
                .handleAnnotations(messageInfo)
                .addKdoc(formatDoc(messageInfo.documentation))
                .handleConstructor(properties)
                .handleMessageSize()
                .addFunction(annotateMessageSizeNew(msg, ctx))
                .addFunction(annotateSerializerNew(msg, ctx))
                .handleEquals(properties)
                .handleHashCode(properties)
                .addTypes(msg.nestedTypes.mapNotNull { annotate(it, ctx) })
                .build()
        }

    private fun TypeSpec.Builder.handleAnnotations(
        messageInfo: MessageInfo
    ) = apply {
        addAnnotation(
            AnnotationSpec.builder(KtGeneratedMessage::class)
                .addMember("\"" + msg.fullProtobufTypeName + "\"")
                .build()
        )
        if (messageInfo.deprecation != null) {
            addAnnotation(
                AnnotationSpec.builder(Deprecated::class)
                    .apply {
                        if (messageInfo.deprecation.message != null) {
                            addMember("\"" + messageInfo.deprecation.message + "\"")
                        }
                    }
                    .build()
            )
        }
    }

    private fun TypeSpec.Builder.handleConstructor(
        properties: List<PropertyInfo>
    ) = apply {
        addSuperinterface(KtMessage::class)
        addProperties(
            properties.map {
                PropertySpec.builder(it.name, TypeVariableName(it.propertyType))
                    .initializer(it.name)
                    .addKdoc(formatDoc(it.documentation))
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
                .addParameters(
                    properties.map {
                        ParameterSpec(it.name, TypeVariableName(it.propertyType))
                    }
                )
                .addParameter(
                    ParameterSpec.builder("unknownFields", UnknownFieldSet::class)
                        .defaultValue("UnknownFieldSet.empty()")
                        .build()
                )
                .build()
        )
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
                        "return other is ${msg.name} && other.unknownFields == unknownFields".replace(" ", "·")
                    } else {
                        """
                            |return other is ${msg.name} &&·
                            |${equalsLines(properties)}
                            |  other.unknownFields == unknownFields
                        """.trimMargin()
                    }
                )
                .build()
        )

    private fun equalsLines(properties: List<PropertyInfo>) =
        properties.joinToString(" &&\n") { "  other.${it.name} == ${it.name}" } + " &&"

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
                        """.trimMargin()
                    }
                )
                .build()
        )

    private fun hashCodeLines(properties: List<PropertyInfo>) =
        properties.joinToString("\n") {
            "result = 31 * result + ${it.name}.hashCode()"
        }

    private fun formatDoc(lines: List<String>) =
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

    private fun messageInfo() =
        MessageInfo(
            name = msg.name,
            doesImplement = msg.doesImplement,
            implements = msg.implements,
            documentation = annotateMessageDocumentation(ctx),
            deprecation = deprecation(),
            suppressDeprecation = suppressDeprecation(),
            fullTypeName = msg.fullProtobufTypeName
        )

    private fun deprecation() =
        if (msg.options.default.deprecated) {
            renderOptions(
                msg.options.protokt.deprecationMessage
            )
        } else {
            null
        }

    private fun suppressDeprecation() =
        msg.hasDeprecation && (!enclosingDeprecation(ctx) || messageIsTopLevel())

    private fun messageIsTopLevel() =
        ctx.enclosing.firstOrNone().fold({ false }, { it == msg })

    private fun options(): Options {
        val lengthAsOneLine =
            ctx.enclosing.size * 4 +
                4 + // companion indentation
                63 + // `override fun deserialize(deserializer: KtMessageDeserializer): `
                msg.name.length +
                2 // ` {`

        return Options(
            wellKnownType = ctx.pkg == PPackage.PROTOKT,
            longDeserializer = lengthAsOneLine > IDEAL_MAX_WIDTH
        )
    }

    companion object {
        const val IDEAL_MAX_WIDTH = 100

        fun annotateMessage(msg: Message, ctx: Context) =
            MessageAnnotator(msg, ctx).annotateMessage()
    }
}
