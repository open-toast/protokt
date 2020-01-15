/*
 * Copyright (c) 2020 Toast Inc.
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

package com.toasttab.protokt.codegen.impl

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.toasttab.protokt.codegen.Optics
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.algebra.Accumulator
import com.toasttab.protokt.codegen.algebra.Effects

internal object STEffects : Effects<AST<TypeDesc>, Accumulator<String>> {
    override fun invoke(astList: List<AST<TypeDesc>>, acc: (String) -> Unit) {
        HeaderAccumulator.writeHeader(astList, acc)
        astList.forEach { effect(it, acc) }
    }

    private fun effect(ast: AST<TypeDesc>, acc: Accumulator<String>) {
        ast.data.type.template.map { template ->
            effect(Optics.annotate(ast, None), None).let {
                addInner(template as STTemplate, it)
                acc(template.render())
            }
        }
    }

    private fun effect(
        ast: AST<TypeDesc>,
        parent: Option<AST<TypeDesc>>
    ): List<String> {
        val l = ast.children.flatMap { effect(it, Some(ast)) }
        var res = ast.data.type.template.fold({ l }, { l + it.render() })
        if (l.isEmpty()) {
            parent.map {
                it.data.type.template.map { t ->
                    addInner(t as STTemplate, res)
                    res = emptyList()
                }
            }
        }
        return res
    }

    private fun addInner(t: STTemplate, strings: List<String>) {
        if (strings.isNotEmpty()) {
            STTemplate.addTo(t, InnerMessageVar, strings.joinToString("\n"))
        }
    }
}
