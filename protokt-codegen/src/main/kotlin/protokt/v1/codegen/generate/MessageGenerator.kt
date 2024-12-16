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
import protokt.v1.AbstractMessage
import protokt.v1.GeneratedMessage
import protokt.v1.GeneratedProperty
import protokt.v1.UnknownFieldSet
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.CodeGenerator.generate
import protokt.v1.codegen.generate.Deprecation.handleDeprecation
import protokt.v1.codegen.generate.Implements.handleSuperInterface
import protokt.v1.codegen.generate.Nullability.nonNullPropName
import protokt.v1.codegen.util.Message

internal fun generateMessage(msg: Message, ctx: Context) =
    if (ctx.info.context.generateTypes && ctx.info.context.kotlinTarget.isPrimaryTarget) {
        MessageGenerator(msg, ctx).generate()
    } else {
        null
    }

private class MessageGenerator(
    private val msg: Message,
    private val ctx: Context
) {
    fun generate(): TypeSpec {
        val properties = annotateProperties(msg, ctx)
        val (constructorProps, delegateProps) = properties(properties)

        return TypeSpec.classBuilder(msg.className).apply {
            annotateMessageDocumentation(ctx)?.let { addKdoc(formatDoc(it)) }
            handleAnnotations()
            handleConstructor(constructorProps)
            addTypes(annotateOneofs(msg, ctx))
            handleMessageSize(constructorProps)
            addProperties(delegateProps)
            addFunction(generateSerializer(msg, propertySpecs, ctx))
            handleEquals(properties)
            handleHashCode(properties)
            handleToString(properties)
            handleBuilder(msg, properties)
            addType(generateDeserializer(msg, ctx, properties))
            addTypes(properties.mapNotNull { it.mapEntry }.map { generateMapEntry(it, ctx) })
            addTypes(msg.nestedTypes.filterNot { it is Message && it.mapEntry }.flatMap { generate(it, ctx) })
        }.build()
    }

    private fun TypeSpec.Builder.handleAnnotations() =
        apply {
            addAnnotation(
                AnnotationSpec.builder(GeneratedMessage::class)
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
    ) =
        apply {
            superclass(AbstractMessage::class)
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

    private data class MessageProperties(
        val constructorProps: List<PropertySpec>,
        val delegateProps: List<PropertySpec>
    ) {
        constructor(property: PropertySpec) : this(listOf(property), emptyList())
        constructor(constructorProp: PropertySpec, delegateProp: PropertySpec) :
            this(listOf(constructorProp), listOf(delegateProp))

        operator fun plus(props: MessageProperties) =
            MessageProperties(
                constructorProps + props.constructorProps,
                delegateProps + props.delegateProps
            )
    }

    private fun properties(properties: List<PropertyInfo>): MessageProperties =
        properties.fold(MessageProperties(emptyList(), emptyList())) { props, property ->
            if (property.generateNullableBackingProperty) {
                props + generateWithBackingProperty(property)
            } else {
                props + generateStandardProperty(property)
            }
        }

    private fun generateStandardProperty(property: PropertyInfo) =
        MessageProperties(
            PropertySpec.builder(property.name, property.propertyType).apply {
                if (property.number != null) {
                    addAnnotation(
                        AnnotationSpec.builder(GeneratedProperty::class)
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
        )

    private fun generateWithBackingProperty(property: PropertyInfo) =
        MessageProperties(
            PropertySpec.builder(property.name, property.propertyType.copy(nullable = true)).apply {
                if (property.number != null) {
                    addAnnotation(
                        AnnotationSpec.builder(GeneratedProperty::class)
                            .addMember("${property.number}")
                            .build()
                    )
                }
                initializer(property.name)
            }.build(),
            PropertySpec.builder(nonNullPropName(property.name), property.propertyType.copy(nullable = false)).apply {
                getter(
                    FunSpec.getterBuilder()
                        .addCode("return ${dereferenceNullableBackingProperty(property.name)}")
                        .build()
                )
                if (property.overrides) {
                    addModifiers(KModifier.OVERRIDE)
                }
                property.documentation?.let { addKdoc(formatDoc(it)) }
                handleDeprecation(property.deprecation)
            }.build()
        )

    private fun dereferenceNullableBackingProperty(propName: String) =
        "requireNotNull($propName) { \"$propName is assumed non-null with (protokt.property).generate_non_null_accessor but was null\" }".bindSpaces()

    private fun TypeSpec.Builder.handleMessageSize(propertySpecs: List<PropertySpec>) {
        addProperty(generateMessageSize(msg, propertySpecs, ctx))
        addFunction(
            buildFunSpec("messageSize") {
                returns(Int::class)
                addModifiers(KModifier.OVERRIDE)
                addStatement("return $MESSAGE_SIZE")
            }
        )
    }

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
        // escape the entire comment block
        "%L",
        lines.joinToString(" ") {
            if (it.isBlank()) {
                "\n\n"
            } else {
                it.removePrefix(" ")
            }
        }
    )
