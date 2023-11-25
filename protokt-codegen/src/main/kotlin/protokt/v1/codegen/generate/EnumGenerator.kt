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

import com.google.protobuf.DescriptorProtos.DescriptorProto.ENUM_TYPE_FIELD_NUMBER
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto.VALUE_FIELD_NUMBER
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.KtEnum
import protokt.v1.KtEnumDeserializer
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Deprecation.handleDeprecation
import protokt.v1.codegen.util.Enum

fun generateEnum(e: Enum, ctx: Context) =
    if (ctx.info.context.generateTypes) {
        EnumGenerator(e, ctx).generate()
    } else {
        null
    }

private class EnumGenerator(
    val e: Enum,
    val ctx: Context
) {
    private val enumPath = listOf(ENUM_TYPE_FIELD_NUMBER, e.index)

    fun generate() =
        TypeSpec.classBuilder(e.className).apply {
            addModifiers(KModifier.SEALED)
            superclass(KtEnum::class)
            addKDoc()
            handleDeprecation(e.options.default.deprecated, e.options.protokt.deprecationMessage)
            addConstructor()
            addEnumValues()
            addDeserializer()
        }.build()

    private fun TypeSpec.Builder.addKDoc() {
        baseLocation(ctx, enumPath)
            ?.cleanDocumentation()
            ?.let { addKdoc(formatDoc(it)) }
    }

    private fun TypeSpec.Builder.addKDoc(value: Enum.Value) =
        apply {
            baseLocation(ctx, enumPath + listOf(VALUE_FIELD_NUMBER, value.index))
                ?.cleanDocumentation()
                ?.let { addKdoc(formatDoc(it)) }
        }

    private fun TypeSpec.Builder.addConstructor() {
        addProperty(constructorProperty("value", Int::class, true))
        addProperty(constructorProperty("name", String::class, true))
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("value", Int::class)
                .addParameter("name", String::class)
                .build()
        )
    }

    private fun TypeSpec.Builder.addEnumValues() {
        addTypes(
            e.values.map {
                TypeSpec.objectBuilder(it.valueName).apply {
                    superclass(e.className)
                    addKDoc(it)
                    addSuperclassConstructorParameter(it.number.toString())
                    addSuperclassConstructorParameter("\"${it.valueName}\"")
                    handleDeprecation(it.options.default.deprecated, it.options.protokt.deprecationMessage)
                }.build()
            }
        )
        addType(
            TypeSpec.classBuilder("UNRECOGNIZED")
                .superclass(e.className)
                .addSuperclassConstructorParameter("value")
                .addSuperclassConstructorParameter("\"UNRECOGNIZED\"")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("value", Int::class)
                        .build()
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.addDeserializer() {
        addType(
            TypeSpec.companionObjectBuilder(e.deserializerClassName.simpleName)
                .addSuperinterface(
                    KtEnumDeserializer::class
                        .asTypeName()
                        .parameterizedBy(e.className)
                )
                .addFunction(
                    buildFunSpec("from") {
                        addModifiers(KModifier.OVERRIDE)
                        returns(e.className)
                        addParameter("value", Int::class)
                        addCode(
                            buildCodeBlock {
                                beginControlFlow("return when (value)")
                                cases().forEach(::addStatement)
                                addStatement("else -> UNRECOGNIZED(value)")
                                endControlFlowWithoutNewline()
                            }
                        )
                    }
                )
                .build()
        )
    }

    private fun cases() =
        e.values
            .distinctBy { it.number }
            .map { "${it.number} -> ${it.valueName}" }
}
