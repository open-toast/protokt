package com.toasttab.protokt.codegen.impl

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo

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
    }

    private fun dslLines() =
        properties.joinToString("\n") {
            "  ${it.name} = this@${msg.name}.${it.name}"
        }
}

fun TypeSpec.Builder.handleDsl(msg: Message, properties: List<PropertyInfo>) =
    apply { DslAnnotator(msg, properties).addDsl(this) }
