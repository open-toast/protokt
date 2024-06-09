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
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import protokt.v1.codegen.generate.CodeGenerator.Context
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
            if (msg.options.protokt.implements.isNotEmpty()) {
                if (msg.options.protokt.implements.delegates()) {
                    val interfaceClassName = inferClassName(msg.options.protokt.implements.substringBefore(" by "), ctx)
                    addSuperinterface(interfaceClassName)

                    val fieldsByName = msg.fields.filterIsInstance<StandardField>().associateBy { it.fieldName }

                    // can't actually delegate because message types are nullable
                    val interfaceFields =
                        ctx.info.context.classLookup.properties(interfaceClassName.canonicalName)
                            .associateBy { it.name }

                    interfaceFields.values.forEach {
                        require(it.returnType.isMarkedNullable) {
                            "Delegated properties must be nullable because message types are nullable"
                        }
                    }
                    val delegatedFields = interfaceFields.values.filter { it.name !in fieldsByName.keys }

                    delegatedFields.forEach {
                        addProperty(
                            PropertySpec.builder(
                                it.name,
                                (it.returnType.classifier as KClass<*>).asTypeName().copy(nullable = true)
                            )
                                .addModifiers(KModifier.OVERRIDE)
                                .getter(
                                    FunSpec.getterBuilder()
                                        .addCode(
                                            CodeBlock.of(
                                                "return %L?.%L",
                                                msg.options.protokt.implements.substringAfter(" by "),
                                                it.name
                                            )
                                        )
                                        .build()
                                )
                                .build()
                        )
                    }
                } else {
                    addSuperinterface(msg.superInterface(ctx)!!)
                }
            }
        }

    private fun String.delegates() =
        contains(" by ")

    private fun Message.superInterface(ctx: Context) =
        options.protokt.implements.let {
            if (it.isNotEmpty() && !it.delegates()) {
                inferClassName(it, ctx)
            } else {
                null
            }
        }
}
