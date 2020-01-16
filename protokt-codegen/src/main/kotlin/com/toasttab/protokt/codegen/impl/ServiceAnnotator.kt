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

import com.toasttab.protokt.codegen.ServiceType
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST

internal object ServiceAnnotator {
    @Suppress("UNUSED_PARAMETER")
    fun annotateService(ast: AST<TypeDesc>, s: ServiceType): AST<TypeDesc> {
        ast.data.type.template.map { t ->
            STTemplate.addTo(t as STTemplate, MethodSt) {
                // TODO
            }
        }
        return ast
    }
}
