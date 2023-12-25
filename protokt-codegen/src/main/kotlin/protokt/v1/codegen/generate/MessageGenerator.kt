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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.AbstractKtMessage
import protokt.v1.KtGeneratedMessage
import protokt.v1.KtProperty
import protokt.v1.UnknownFieldSet
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.CodeGenerator.generate
import protokt.v1.codegen.generate.Deprecation.handleDeprecation
import protokt.v1.codegen.generate.Implements.handleSuperInterface
import protokt.v1.codegen.util.Message

fun generateMessage(msg: Message, ctx: Context) =
    if (ctx.info.context.generateTypes) {
        MessageGenerator(msg, ctx).generate()
    } else {
        null
    }

private class MessageGenerator(
    private val msg: Message,
    private val ctx: Context
) {
    fun generate() =
        if (msg.mapEntry) {
            generateMapEntry(msg, ctx)
        } else {
            val properties = annotateProperties(msg, ctx)
            val propertySpecs = properties(properties)

            TypeSpec.classBuilder(msg.className).apply {
                annotateMessageDocumentation(ctx)?.let { addKdoc(formatDoc(it)) }
                handleAnnotations()
                handleConstructor(propertySpecs)
                addTypes(annotateOneofs(msg, ctx))
                handleMessageSize()
                addFunction(generateMessageSize(msg, propertySpecs, ctx))
                addFunction(generateSerializer(msg, propertySpecs, ctx))
                handleEquals(properties)
                handleHashCode(properties)
                handleToString(properties)
                handleBuilder(msg, properties)
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
    }

    private fun TypeSpec.Builder.handleConstructor(
        properties: List<PropertySpec>
    ) = apply {
        superclass(AbstractKtMessage::class)
        addProperties(properties)
        addProperty(
            PropertySpec.builder("unknownFields", UnknownFieldSet::class)
                .initializer("unknownFields")
                .build()
        )
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addModifiers(KModifier.PRIVATE)
                .addParameters(properties.map { ParameterSpec(it.name, it.type) })
                .addParameter(
                    ParameterSpec.builder("unknownFields", UnknownFieldSet::class)
                        .defaultValue("%T.empty()", UnknownFieldSet::class)
                        .build()
                )
                .build()
        )
        handleSuperInterface(msg, ctx)
    }

    private fun properties(properties: List<PropertyInfo>) =
        properties.map { property ->
            PropertySpec.builder(property.name, property.propertyType).apply {
                if (property.number != null) {
                    addAnnotation(
                        AnnotationSpec.builder(KtProperty::class)
                            .addMember("${property.number}")
                            .build()
                    )
                }
                initializer(property.name)
                if (property.overrides) {
                    addModifiers(KModifier.OVERRIDE)
                }
                property.documentation?.let { addKdoc(formatDoc(it)) }
                handleDeprecation(property.deprecation)
            }.build()
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
                            "return \"%L(\${${unknownFieldsToString(prefix = "")}}\"",
                            msg.className.simpleName
                        )
                    } else {
                        buildCodeBlock {
                            add("return \"%L(\" +\n", msg.className.simpleName)
                            toStringLines(properties).forEach(::add)
                            add(unknownFieldsToString(prefix = ", "))
                        }
                    }
                )
                .build()
        )

    private fun unknownFieldsToString(prefix: String) =
        "if (unknownFields.isEmpty()) \")\" else \"${prefix}unknownFields=\$unknownFields)\"".bindSpaces()

    private fun toStringLines(properties: List<PropertyInfo>) =
        properties.mapIndexed { idx, prop ->
            CodeBlock.of(
                "\"%N=\$%N" + if (idx == properties.size - 1) "\" + \n" else ", \" +\n".bindSpaces(),
                prop.name,
                prop.name
            )
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
