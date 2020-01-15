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

package com.toasttab.protokt.codegen

import arrow.core.Option
import arrow.optics.Lens
import arrow.optics.PLens
import com.toasttab.protokt.codegen.algebra.AST

object Optics {
    // put a rawType in an annotated rawType
    val annotatedTypeLens: Lens<AnnotatedType, Type> = PLens(
        get = { i -> i.rawType },
        set = { a, t -> a.copy(rawType = t) }
    )

    val annotatedTypeTemplateLens: Lens<AnnotatedType, Option<Template>> = PLens(
        get = { i -> i.template },
        set = { a, t -> a.copy(template = t) }
    )

    val typeDescTypeLens: Lens<TypeDesc, AnnotatedType> = PLens(
        get = { i -> i.type },
        set = { t, a -> t.copy(type = a) }
    )

    val astLens: Lens<AST<TypeDesc>, TypeDesc> = PLens(
        get = { i -> i.data },
        set = { a, t -> a.copy(data = t) }
    )

    val astChildrenLens: Lens<AST<TypeDesc>, List<AST<TypeDesc>>> = PLens(
        get = { i -> i.children },
        set = { a, l -> a.copy(children = (a.children + l)) }
    )

    fun annotate(ast: AST<TypeDesc>, t: Option<Template>) =
        astLens.set(
            ast,
            typeDescTypeLens.set(
                ast.data,
                annotatedTypeTemplateLens.set(ast.data.type, t)
            )
        )
}
