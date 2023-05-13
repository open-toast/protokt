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

package com.toasttab.protokt.v1.codegen.generate

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.toasttab.protokt.v1.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.v1.codegen.util.Message
import com.toasttab.protokt.v1.codegen.util.StandardField

object Implements {
    fun StandardField.overrides(
        ctx: Context,
        msg: Message
    ) =
        msg.superInterface(ctx)
            ?.let { fieldName in ctx.info.context.classLookup.properties(it) }
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
                    addSuperinterface(
                        // TODO: parameterize this by the ctx package?
                        ClassName.bestGuess(msg.options.protokt.implements.substringBefore(" by ")),
                        msg.options.protokt.implements.substringAfter(" by ")
                    )
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
