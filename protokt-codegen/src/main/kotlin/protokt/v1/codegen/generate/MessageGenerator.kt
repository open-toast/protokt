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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.AbstractMessage
import protokt.v1.Bytes
import protokt.v1.GeneratedMessage
import protokt.v1.GeneratedProperty
import protokt.v1.LazyReference
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
        val msgProps = properties(properties)

        return TypeSpec.classBuilder(msg.className).apply {
            annotateMessageDocumentation(ctx)?.let { addKdoc(formatDoc(it)) }
            handleAnnotations()
            handleConstructor(msgProps)
            addTypes(annotateOneofs(msg, ctx))
            handleMessageSize(msgProps.serializationProps)
            addProperties(msgProps.delegateProps)
            addFunction(generateSerializer(msg, msgProps.serializationProps, ctx))
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
        props: MessageProperties
    ) =
        apply {
            superclass(AbstractMessage::class)
            addProperties(props.constructorPropertySpecs)
            addProperty(
                PropertySpec.builder("unknownFields", UnknownFieldSet::class)
                    .initializer("unknownFields")
                    .build()
            )
            primaryConstructor(
                FunSpec.constructorBuilder()
                    .addModifiers(KModifier.PRIVATE)
                    .addParameters(
                        props.constructorEntries.map { entry ->
                            when (entry) {
                                is ConstructorEntry.ValProp -> ParameterSpec(entry.spec.name, entry.spec.type)
                                is ConstructorEntry.PlainParam -> entry.spec
                            }
                        }
                    )
                    .addParameter(
                        ParameterSpec.builder("unknownFields", UnknownFieldSet::class)
                            .defaultValue("%T.empty()", UnknownFieldSet::class)
                            .build()
                    )
                    .addParameters(props.trailingParams)
                    .build()
            )
            handleSuperInterface(msg, ctx)
        }

    private sealed class ConstructorEntry {
        data class ValProp(val spec: PropertySpec) : ConstructorEntry()
        data class PlainParam(val spec: ParameterSpec) : ConstructorEntry()
    }

    private data class MessageProperties(
        val constructorEntries: List<ConstructorEntry>,
        val delegateProps: List<PropertySpec>,
        val trailingParams: List<ParameterSpec>,
        val serializationProps: List<PropertySpec>
    ) {
        val constructorPropertySpecs get() =
            constructorEntries.filterIsInstance<ConstructorEntry.ValProp>().map { it.spec }

        operator fun plus(props: MessageProperties) =
            MessageProperties(
                constructorEntries + props.constructorEntries,
                delegateProps + props.delegateProps,
                trailingParams + props.trailingParams,
                serializationProps + props.serializationProps
            )
    }

    private fun properties(properties: List<PropertyInfo>): MessageProperties =
        properties.fold(MessageProperties(emptyList(), emptyList(), emptyList(), emptyList())) { props, property ->
            when {
                property.cachingInfo != null -> props + generateCachingProperty(property, property.cachingInfo)
                property.generateNullableBackingProperty -> props + generateWithBackingProperty(property)
                else -> props + generateStandardProperty(property)
            }
        }

    private fun generateStandardProperty(property: PropertyInfo): MessageProperties {
        val prop = PropertySpec.builder(property.name, property.propertyType).apply {
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

        return MessageProperties(
            constructorEntries = listOf(ConstructorEntry.ValProp(prop)),
            delegateProps = emptyList(),
            trailingParams = emptyList(),
            serializationProps = listOf(prop)
        )
    }

    private fun generateWithBackingProperty(property: PropertyInfo): MessageProperties {
        val backingProp = PropertySpec.builder(property.name, property.propertyType.copy(nullable = true)).apply {
            if (property.number != null) {
                addAnnotation(
                    AnnotationSpec.builder(GeneratedProperty::class)
                        .addMember("${property.number}")
                        .build()
                )
            }
            if (property.overrides) {
                addModifiers(KModifier.OVERRIDE)
            }
            initializer(property.name)
        }.build()

        val delegateProp = PropertySpec.builder(nonNullPropName(property.name), property.propertyType.copy(nullable = false)).apply {
            getter(
                FunSpec.getterBuilder()
                    .addCode("return ${dereferenceNullableBackingProperty(property.name, property.oneof)}")
                    .build()
            )
            property.documentation?.let { addKdoc(formatDoc(it)) }
            handleDeprecation(property.deprecation)
        }.build()

        return MessageProperties(
            constructorEntries = listOf(ConstructorEntry.ValProp(backingProp)),
            delegateProps = listOf(delegateProp),
            trailingParams = emptyList(),
            serializationProps = listOf(backingProp)
        )
    }

    private fun generateCachingProperty(property: PropertyInfo, info: CachingFieldInfo): MessageProperties {
        val wireTypeName = when (info) {
            is CachingFieldInfo.PlainString -> Bytes::class.asTypeName()
            is CachingFieldInfo.Converted -> info.wireTypeName
        }
        val nonNullPropertyType = property.propertyType.copy(nullable = false)
        val lazyRefType = LazyReference::class.asTypeName()
            .parameterizedBy(wireTypeName, nonNullPropertyType)
        val backingType = if (info.nullable) lazyRefType.copy(nullable = true) else lazyRefType

        val backingProp = PropertySpec.builder("_${property.name}", backingType)
            .addModifiers(KModifier.PRIVATE)
            .initializer("_${property.name}")
            .build()

        val valueAccessCode = if (info.nullable) "_${property.name}?.value()" else "_${property.name}.value()"

        val publicProp = PropertySpec.builder(property.name, property.propertyType).apply {
            addAnnotation(
                AnnotationSpec.builder(GeneratedProperty::class)
                    .addMember("${property.number}")
                    .build()
            )
            if (property.overrides) {
                addModifiers(KModifier.OVERRIDE)
            }
            getter(
                FunSpec.getterBuilder()
                    .addCode("return $valueAccessCode")
                    .build()
            )
            property.documentation?.let { addKdoc(formatDoc(it)) }
            handleDeprecation(property.deprecation)
        }.build()

        val delegateProps = mutableListOf(publicProp)
        if (property.generateNullableBackingProperty && info.nullable) {
            delegateProps.add(
                PropertySpec.builder(nonNullPropName(property.name), nonNullPropertyType).apply {
                    getter(
                        FunSpec.getterBuilder()
                            .addCode("return ${dereferenceNullableBackingProperty(property.name, property.oneof)}")
                            .build()
                    )
                    property.documentation?.let { addKdoc(formatDoc(it)) }
                    handleDeprecation(property.deprecation)
                }.build()
            )
        }

        return MessageProperties(
            constructorEntries = listOf(ConstructorEntry.ValProp(backingProp)),
            delegateProps = delegateProps,
            trailingParams = emptyList(),
            serializationProps = listOf(backingProp)
        )
    }

    private fun dereferenceNullableBackingProperty(propName: String, oneof: Boolean) =
        "requireNotNull($propName) { \"$propName is assumed non-null with (protokt.v1.${if (oneof) "oneof" else "property"}).generate_non_null_accessor but was null\" }".bindSpaces()

    private fun TypeSpec.Builder.handleMessageSize(propertySpecs: List<PropertySpec>) {
        addProperty(generateMessageSize(msg, propertySpecs, ctx))
        addFunction(
            buildFunSpec("serializedSize") {
                returns(Int::class)
                addModifiers(KModifier.OVERRIDE)
                addStatement("return $SERIALIZED_SIZE")
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
            CodeBlock.of("other.%N == this.%N &&\n".bindSpaces(), it.name, it.name)
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
