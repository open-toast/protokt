/*
 * Copyright (c) 2021 Toast, Inc.
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
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import protokt.v1.BuilderDsl
import protokt.v1.BuilderScope
import protokt.v1.Bytes
import protokt.v1.LazyConvertingList
import protokt.v1.LazyConvertingMap
import protokt.v1.LazyReference
import protokt.v1.StringConverter
import protokt.v1.UnknownFieldSet
import protokt.v1.codegen.generate.Deprecation.handleDeprecation
import protokt.v1.codegen.util.BUILDER
import protokt.v1.codegen.util.Message
import protokt.v1.reflect.FieldType

internal fun TypeSpec.Builder.handleBuilder(msg: Message, properties: List<PropertyInfo>) =
    apply { BuilderGenerator(msg, properties).addBuilder(this) }

private class BuilderGenerator(
    private val msg: Message,
    private val properties: List<PropertyInfo>
) {
    fun addBuilder(builder: TypeSpec.Builder) {
        builder.addFunction(
            FunSpec.builder("toBuilder")
                .returns(msg.builderClassName)
                .addCode("return %T.from(this)", msg.builderClassName)
                .build()
        )
        builder.addFunction(
            FunSpec.builder("copy")
                .returns(msg.className)
                .addParameter(
                    "builder",
                    LambdaTypeName.get(
                        msg.builderClassName,
                        emptyList(),
                        Unit::class.asTypeName()
                    )
                )
                .addCode("return toBuilder().apply { builder() }.build()")
                .build()
        )
        builder.addType(
            TypeSpec.classBuilder(msg.builderClassName)
                .addAnnotation(BuilderDsl::class)
                .addSuperinterface(BuilderScope::class)
                .addProperties(
                    properties.flatMap { prop ->
                        if (prop.cachingInfo != null) {
                            cachingBuilderProperties(prop, prop.cachingInfo)
                        } else {
                            listOf(
                                PropertySpec.builder(prop.name, prop.builderPropertyType)
                                    .mutable(true)
                                    .handleDeprecation(prop.deprecation)
                                    .apply {
                                        if (prop.mapCachingInfo != null) {
                                            setter(convertingMapSetter(prop.mapCachingInfo))
                                        } else if (prop.repeatedCachingInfo != null) {
                                            setter(convertingListSetter(prop.repeatedCachingInfo))
                                        } else if (prop.isMap) {
                                            setter(
                                                FunSpec.setterBuilder()
                                                    .addParameter("newValue", Map::class)
                                                    .addCode("field = %M(newValue)", freezeMap)
                                                    .build()
                                            )
                                        } else if (prop.repeated) {
                                            setter(
                                                FunSpec.setterBuilder()
                                                    .addParameter("newValue", List::class)
                                                    .addCode("field = %M(newValue)", freezeList)
                                                    .build()
                                            )
                                        }
                                    }
                                    .initializer(
                                        when {
                                            prop.isMap -> CodeBlock.of("emptyMap()")
                                            prop.repeated -> CodeBlock.of("emptyList()")
                                            prop.fieldType == FieldType.Message || prop.wrapped || prop.nullable -> CodeBlock.of("null")
                                            else -> prop.defaultValue
                                        }
                                    )
                                    .build()
                            )
                        }
                    }
                )
                .addProperty(
                    PropertySpec.builder("unknownFields", UnknownFieldSet::class)
                        .mutable(true)
                        .initializer("%T.empty()", UnknownFieldSet::class)
                        .build()
                )
                .addFunction(
                    FunSpec.builder("build")
                        .returns(msg.className)
                        .addCode(
                            if (properties.isEmpty()) {
                                CodeBlock.of("return %T(unknownFields)", msg.className)
                            } else {
                                buildCodeBlock {
                                    add("return %T(\n", msg.className)
                                    withIndent {
                                        properties
                                            .map { wrapDeserializedValueForConstructor(it, fromBuilder = true) }
                                            .forEach { add("%L,\n", it) }
                                        add("unknownFields\n")
                                    }
                                    add(")")
                                }
                            }
                        )
                        .build()
                )
                .addType(
                    TypeSpec.companionObjectBuilder("Factory")
                        .addFunction(fromFunction())
                        .build()
                )
                .build()
        )
    }

    private fun cachingBuilderProperties(prop: PropertyInfo, info: CachingFieldInfo): List<PropertySpec> {
        val wireTypeName = when (info) {
            is CachingFieldInfo.PlainString -> Bytes::class.asTypeName()
            is CachingFieldInfo.Converted -> info.wireTypeName
        }
        val nonNullPropertyType = prop.propertyType.copy(nullable = false)
        val lazyRefType = LazyReference::class.asTypeName()
            .parameterizedBy(wireTypeName, nonNullPropertyType)
        val converterRef = when (info) {
            is CachingFieldInfo.PlainString -> CodeBlock.of("%T", StringConverter::class)
            is CachingFieldInfo.Converted -> CodeBlock.of("%T", info.converterClassName)
        }
        val refPropName = "_${prop.name}Ref"

        val refProp = PropertySpec.builder(refPropName, lazyRefType.copy(nullable = true))
            .mutable(true)
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()

        val isNullable = prop.builderPropertyType.isNullable

        val getter = if (isNullable) {
            FunSpec.getterBuilder()
                .addCode("return %N?.value()", refPropName)
                .build()
        } else {
            FunSpec.getterBuilder()
                .addCode("return %N?.value() ?: %L", refPropName, prop.defaultValue)
                .build()
        }

        val setter = if (isNullable) {
            FunSpec.setterBuilder()
                .addParameter("newValue", prop.builderPropertyType)
                .addCode("%N = newValue?.let { %T(it, %L) }", refPropName, LazyReference::class, converterRef)
                .build()
        } else {
            FunSpec.setterBuilder()
                .addParameter("newValue", prop.builderPropertyType)
                .addCode("%N = %T(newValue, %L)", refPropName, LazyReference::class, converterRef)
                .build()
        }

        val publicProp = PropertySpec.builder(prop.name, prop.builderPropertyType)
            .mutable(true)
            .handleDeprecation(prop.deprecation)
            .getter(getter)
            .setter(setter)
            .build()

        return listOf(refProp, publicProp)
    }

    private fun convertingListSetter(info: RepeatedCachingInfo): FunSpec {
        val converterRef = when (info) {
            is RepeatedCachingInfo.PlainString -> CodeBlock.of("%T", StringConverter::class)
            is RepeatedCachingInfo.Converted -> CodeBlock.of("%T", info.converterClassName)
        }
        return FunSpec.setterBuilder()
            .addParameter("newValue", List::class)
            .addCode(
                "field = if (newValue is %T<*, *>) newValue else %T.fromKotlin(newValue, %L)",
                LazyConvertingList::class,
                LazyConvertingList::class,
                converterRef
            )
            .build()
    }

    private fun convertingMapSetter(info: MapCachingInfo): FunSpec {
        val keyConverterRef = if (info.keyConverterClassName != null) {
            CodeBlock.of("%T", info.keyConverterClassName)
        } else {
            CodeBlock.of("null")
        }
        val valueConverterRef = if (info.valueConverterClassName != null) {
            CodeBlock.of("%T", info.valueConverterClassName)
        } else {
            CodeBlock.of("null")
        }
        return FunSpec.setterBuilder()
            .addParameter("newValue", Map::class)
            .addCode(
                "field = if (newValue is %T<*, *>) newValue else %T.fromKotlin(newValue, %L, %L, %L, %L)",
                LazyConvertingMap::class,
                LazyConvertingMap::class,
                info.keyWrapped,
                info.valueWrapped,
                keyConverterRef,
                valueConverterRef
            )
            .build()
    }

    private fun fromFunction(): FunSpec =
        FunSpec.builder("from")
            .addModifiers(KModifier.INTERNAL)
            .addParameter("msg", msg.className)
            .returns(msg.builderClassName)
            .addCode(
                buildCodeBlock {
                    beginControlFlow("return %T().also", msg.builderClassName)
                    properties.forEach { prop ->
                        if (prop.cachingInfo != null) {
                            addStatement("it.%N = msg.%N", "_${prop.name}Ref", "_${prop.name}")
                        } else {
                            addStatement("it.%N = msg.%N", prop.name, prop.name)
                        }
                    }
                    addStatement("it.unknownFields = msg.unknownFields")
                    endControlFlow()
                }
            )
            .build()
}

val Message.builderClassName
    get() = className.nestedClass(BUILDER)
