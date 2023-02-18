/*
 * Copyright (c) 2021 Toast Inc.
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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import com.toasttab.protokt.UnknownFieldSet
import com.toasttab.protokt.codegen.generate.Deprecation.handleDeprecation
import com.toasttab.protokt.codegen.util.FieldType
import com.toasttab.protokt.codegen.util.Message

internal fun TypeSpec.Builder.handleDsl(msg: Message, properties: List<PropertyInfo>) =
    apply { DslGenerator(msg, properties).addDsl(this) }

private class DslGenerator(
    private val msg: Message,
    private val properties: List<PropertyInfo>
) {
    fun addDsl(builder: TypeSpec.Builder) {
        builder.addFunction(
            FunSpec.builder("copy")
                .returns(msg.className)
                .addParameter(
                    "dsl",
                    LambdaTypeName.get(
                        msg.dslClassName,
                        emptyList(),
                        Unit::class.asTypeName()
                    )
                )
                .addCode(
                    buildCodeBlock {
                        beginControlFlow("return %T().apply", msg.dslClassName)
                        dslLines().forEach { add("%L\n", it) }
                        addStatement("unknownFields = this@%L.unknownFields", msg.className.simpleName)
                        addStatement("dsl()")
                        endControlFlowWithoutNewline()
                        add(".build()")
                    }
                )
                .build()
        )
        builder.addType(
            TypeSpec.classBuilder(msg.dslClassName)
                .addProperties(
                    properties.map {
                        PropertySpec.builder(it.name, it.dslPropertyType)
                            .mutable(true)
                            .handleDeprecation(it.deprecation)
                            .apply {
                                if (it.map) {
                                    setter(
                                        FunSpec.setterBuilder()
                                            .addParameter("newValue", Map::class)
                                            .addCode("field = %M(newValue)", runtimeFunction("copyMap"))
                                            .build()
                                    )
                                } else if (it.repeated) {
                                    setter(
                                        FunSpec.setterBuilder()
                                            .addParameter("newValue", List::class)
                                            .addCode("field = %M(newValue)", runtimeFunction("copyList"))
                                            .build()
                                    )
                                }
                            }
                            .initializer(
                                when {
                                    it.map -> CodeBlock.of("emptyMap()")
                                    it.repeated -> CodeBlock.of("emptyList()")
                                    it.fieldType == FieldType.MESSAGE || it.wrapped || it.nullable -> CodeBlock.of("null")
                                    else -> it.defaultValue
                                }
                            )
                            .build()
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
                                            .map(::wrapDeserializedValueForConstructor)
                                            .forEach { add("%L,\n", it) }
                                        add("unknownFields\n")
                                    }
                                    add(")")
                                }
                            }
                        )
                        .build()
                )
                .build()
        )
    }

    private fun dslLines() =
        properties.map {
            CodeBlock.of(
                "%N = this@%L.%N",
                it.name,
                msg.className.simpleName,
                it.name
            )
        }
}
