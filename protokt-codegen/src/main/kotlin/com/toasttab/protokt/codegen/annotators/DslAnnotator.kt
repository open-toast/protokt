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

package com.toasttab.protokt.codegen.annotators

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.codegen.impl.bindSpaces
import com.toasttab.protokt.codegen.impl.runtimeFunction
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo
import com.toasttab.protokt.rt.UnknownFieldSet

class DslAnnotator(
    private val msg: Message,
    private val properties: List<PropertyInfo>
) {
    fun addDsl(builder: TypeSpec.Builder) {
        builder.addFunction(
            FunSpec.builder("copy")
                .returns(msg.typeName)
                .addParameter(
                    "dsl",
                    LambdaTypeName.get(
                        msg.dslTypeName,
                        emptyList(),
                        Unit::class.asTypeName()
                    )
                )
                .addCode(
                    (
                        "return " + msg.name + "Dsl().apply {\n" +
                            if (properties.isEmpty()) {
                                ""
                            } else {
                                dslLines() + "\n"
                            } +
                            """
                               |    unknownFields = this@${msg.name}.unknownFields
                               |    dsl()
                               |}.build()
                           """.trimMargin()
                        ).bindSpaces()
                )
                .build()
        )
        builder.addType(
            TypeSpec.classBuilder(msg.name + "Dsl")
                .addProperties(
                    properties.map {
                        PropertySpec.builder(it.name.removePrefix("`").removeSuffix("`"), it.dslPropertyType)
                            .mutable(true)
                            .apply {
                                if (it.deprecation != null) {
                                    addAnnotation(
                                        AnnotationSpec.builder(Deprecated::class)
                                            .apply {
                                                if (it.deprecation.message != null) {
                                                    addMember("\"" + it.deprecation.message + "\"")
                                                } else {
                                                    addMember("\"deprecated in proto\"")
                                                }
                                            }
                                            .build()
                                    )
                                }
                            }
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
                                    it.fieldType == "MESSAGE" || it.wrapped || it.nullable -> CodeBlock.of("null")
                                    else -> it.defaultValue
                                }
                            )
                            .build()
                    }
                )
                .addProperty(
                    PropertySpec.builder("unknownFields", UnknownFieldSet::class)
                        .mutable(true)
                        .initializer("UnknownFieldSet.empty()")
                        .build()
                )
                .addFunction(
                    FunSpec.builder("build")
                        .returns(msg.typeName)
                        .addCode(
                            if (properties.isEmpty()) {
                                CodeBlock.of("return %L(unknownFields)", msg.name)
                            } else {
                                CodeBlock.of("return %L(%L %L)", msg.name, buildLines(), "unknownFields")
                            }
                        )
                        .build()
                )
                .build()
        )
    }

    private fun dslLines() =
        properties.joinToString("\n") {
            "    ${it.name} = this@${msg.name}.${it.name}"
        }

    private fun buildLines() =
        buildCodeBlock {
            properties.forEach {
                add("%L,\n", deserializeWrapper(it))
            }
        }
}

fun TypeSpec.Builder.handleDsl(msg: Message, properties: List<PropertyInfo>) =
    apply { DslAnnotator(msg, properties).addDsl(this) }
