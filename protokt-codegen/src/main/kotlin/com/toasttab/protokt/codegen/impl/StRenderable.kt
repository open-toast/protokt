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

import com.toasttab.protokt.codegen.Renderable
import com.toasttab.protokt.codegen.template.TemplateVariable
import org.stringtemplate.v4.ST

// TODO: This functionality be factored out into a pure functional library
// TODO: This imperative style is a TEMPORARY implementation detail
data class StRenderable(val st: ST) : Renderable {
    override fun render() =
        st.render()

    companion object {
        fun <T> addTo(st: StRenderable, v: TemplateVariable, t: T) {
            st.st.add(v.name, t)
        }
    }
}
