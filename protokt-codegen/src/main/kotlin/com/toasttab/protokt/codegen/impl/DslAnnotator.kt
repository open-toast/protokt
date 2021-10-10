package com.toasttab.protokt.codegen.impl

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
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
                .addParameter(
                    "dsl",
                    LambdaTypeName.get(
                        TypeVariableName(msg.name + "Dsl"),
                        emptyList(),
                        Unit::class.asTypeName()
                    )
                )
                .addCode(
                    msg.name + " {\n" +
                        if (properties.isEmpty()) {
                            ""
                        } else {
                            dslLines() + "\n"
                        } +
                        """
                       |  unknownFields = this@${msg.name}.unknownFields
                       |  dsl()
                       |}
                   """.trimMargin()
                )
                .build()
        )
        builder.addType(
            TypeSpec.classBuilder(msg.name + "Dsl")
                .addProperties(
                    properties.map {
                        PropertySpec.builder(it.name, TypeVariableName(it.dslPropertyType.removeSuffix("?")).copy(nullable = it.nullable))
                            .mutable(true)
                            .apply {
                                if (it.deprecation != null) {
                                    addAnnotation(
                                        AnnotationSpec.builder(Deprecated::class)
                                            .apply {
                                                if (it.deprecation.message != null) {
                                                    addMember("\"" + it.deprecation.message + "\"")
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
                                            .addParameter("newValue", Any::class)
                                            .addCode("field = copyMap(newValue)")
                                            .build()
                                    )
                                } else if (it.repeated) {
                                    setter(
                                        FunSpec.setterBuilder()
                                            .addParameter("newValue", Any::class)
                                            .addCode("field = copyList(newValue)")
                                            .build()
                                    )
                                }
                            }
                            .initializer(
                                when {
                                    it.map -> "emptyMap()"
                                    it.repeated -> "emptyList()"
                                    it.fieldType == "MESSAGE" || it.wrapped || it.nullable -> "null"
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
                        .build()
                )
                .build()
        )
    }

    private fun dslLines() =
        properties.joinToString("\n") {
            "  ${it.name} = this@${msg.name}.${it.name}"
        }
}

fun TypeSpec.Builder.handleDsl(msg: Message, properties: List<PropertyInfo>) =
    apply { DslAnnotator(msg, properties).addDsl(this) }
