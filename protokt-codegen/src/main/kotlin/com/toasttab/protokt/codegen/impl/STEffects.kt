/*
 * Copyright (c) 2019 Toast Inc.
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
import arrow.core.firstOrNone
import com.toasttab.protokt.codegen.Optics
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST
import com.toasttab.protokt.codegen.algebra.Accumulator
import com.toasttab.protokt.codegen.algebra.Effects
import com.toasttab.protokt.codegen.template.TemplateVariable

internal object STEffects : Effects<AST<TypeDesc>, Accumulator<String>> {
    override fun invoke(astList: List<AST<TypeDesc>>, acc: (String) -> Unit) {
        val imports = collectPossibleImports(astList)

        val header = StringBuilder()
        HeaderAccumulator.write(astList, imports) { header.append(it + "\n") }

        val body = StringBuilder()
        astList.forEach { effect(it) { s -> body.append(s + "\n") } }

        acc(header.toString())
        acc(ImportReplacer.replaceImports(body.toString(), imports))
    }

    private fun collectPossibleImports(astList: List<AST<TypeDesc>>): Set<Import> =
        astList.firstOrNone()
            .fold(
                { emptySet() },
                {
                    ImportResolver(it.data.desc.context, kotlinPackage(it))
                        .resolveImports(astList)
                }
            )

    private fun effect(ast: AST<TypeDesc>, acc: Accumulator<String>) {
        ast.data.type.renderable.map { template ->
            effect(Optics.annotate(ast, None), None).let {
                addInner(template as StRenderable, it)
                acc(template.render())
            }
        }
    }

    private fun effect(
        ast: AST<TypeDesc>,
        parent: Option<AST<TypeDesc>>
    ): List<String> {
        val l = ast.children.flatMap { effect(it, Some(ast)) }
        var res = ast.data.type.renderable.fold({ l }, { l + it.render() })
        if (l.isEmpty()) {
            parent.map {
                it.data.type.renderable.map { t ->
                    addInner(t as StRenderable, res)
                    res = emptyList()
                }
            }
        }
        return res
    }

    private fun addInner(t: StRenderable, strings: List<String>) {
        if (strings.isNotEmpty()) {
            StRenderable.addTo(t, Inner, strings.joinToString("\n"))
        }
    }

    object Inner : TemplateVariable {
        override val name = "inner"
    }
}
