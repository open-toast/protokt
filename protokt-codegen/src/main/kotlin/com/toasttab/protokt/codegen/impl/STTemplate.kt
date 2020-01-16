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

import com.toasttab.protokt.codegen.Template
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroupFile

// TODO: This functionality be factored out into a pure functional library
// TODO: This imperative style is a TEMPORARY implementation detail
internal data class STTemplate(val st: ST) : Template {
    override fun render(): String {
        return st.render()
    }

    companion object {
        private val templates = mapOf(
            Templates to STGroupFile(Templates.value),
            Services to STGroupFile(Services.value),
            Renderers to STGroupFile(Renderers.value),
            Options to STGroupFile(Options.value)
        )

        fun <T> toTemplate(tt: TemplateSt<T>): STTemplate {
            return STTemplate(
                templates.getValue(tt.group).getInstanceOf(tt.name))
        }

        fun <T : Var> addTo(
            st: STTemplate,
            tt: TemplateSt<T>,
            fn: (T) -> Any
        ) {
            tt.vars.forEach { st.st.add(it.value, fn(it)) }
        }

        fun <T> addTo(st: STTemplate, v: Var, t: T) {
            st.st.add(v.value, t)
        }
    }
}

internal fun <T : Var> TemplateSt<T>.render(params: List<Pair<T, Any>>) =
    render(*params.toTypedArray())

internal fun <T : Var> TemplateSt<T>.render(
    vararg params: Pair<Var, Any>
): String {
    val template = STTemplate.toTemplate(this).st
    params.forEach { (k, v) ->
        when (k) {
            in vars -> template.add(k.value, v)
            else -> error("${k.value} not found in $name")
        }
    }
    return template.render()
}
