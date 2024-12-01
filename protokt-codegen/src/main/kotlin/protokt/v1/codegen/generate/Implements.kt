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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Nullability.treatAsNullable
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.StandardField
import kotlin.reflect.KClass

internal object Implements {
    fun StandardField.overrides(
        ctx: Context,
        msg: Message
    ) =
        msg.superInterface(ctx)
            ?.let { fieldName in ctx.info.context.classLookup.properties(it.canonicalName).map { p -> p.name } }
            ?: false

    fun TypeSpec.Builder.handleSuperInterface(implements: ClassName?, v: OneofGeneratorInfo? = null) =
        apply {
            if (implements != null) {
                if (v == null) {
                    addSuperinterface(implements)
                } else {
                    addSuperinterface(implements, v.fieldName)
                }
            }
        }

    fun TypeSpec.Builder.handleSuperInterface(msg: Message, ctx: Context) =
        apply {
            val superInterface = msg.superInterface(ctx)
            if (superInterface != null) {
                addSuperinterface(superInterface.`interface`)
                if (superInterface.delegate != null) {
                    // don't delegate because message types may be nullable
                    delegateProperties(msg, ctx, superInterface.canonicalName, superInterface.delegate)
                }
            }
        }

    private fun TypeSpec.Builder.delegateProperties(msg: Message, ctx: Context, canonicalName: String, fieldName: String) {
        val fieldsByName = msg.fields.filterIsInstance<StandardField>().associateBy { it.fieldName }

        val interfaceFields =
            ctx.info.context.classLookup
                .properties(canonicalName)
                .associateBy { it.name }

        val implementFields = interfaceFields.values.filter { it.name !in fieldsByName.keys }

        implementFields.forEach { field ->
            val standardFieldNullable = fieldsByName.getValue(fieldName).treatAsNullable
            addProperty(
                PropertySpec.builder(
                    field.name,
                    (field.returnType.classifier as KClass<*>).asTypeName()
                        .let {
                            if (standardFieldNullable) {
                                it.copy(nullable = true)
                            } else {
                                it
                            }
                        }
                )
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode(
                                "return %L" +
                                    if (standardFieldNullable) {
                                        "?"
                                    } else {
                                        ""
                                    } +
                                    ".%L",
                                fieldName,
                                field.name
                            )
                            .build()
                    )
                    .build()
            )
        }
    }

    private class SuperInterface(
        val `interface`: ClassName,
        val delegate: String?
    ) {
        val canonicalName = `interface`.canonicalName
    }

    private fun Message.superInterface(ctx: Context): SuperInterface? {
        val implements = options.protokt.implements
        return when {
            implements.isEmpty() -> null
            implements.contains(" by ") ->
                SuperInterface(
                    inferClassName(implements.substringBefore(" by "), ctx),
                    implements.substringAfter(" by ")
                )
            else -> SuperInterface(inferClassName(implements, ctx), null)
        }
    }
}
