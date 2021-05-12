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

import arrow.core.firstOrNone
import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.TypeDesc
import com.toasttab.protokt.codegen.template.Header.Header

object HeaderAccumulator {
    fun write(
        descs: List<TypeDesc>,
        imports: Set<Import>,
        acc: (String) -> Unit
    ) {
        descs.firstOrNone().map { f ->
            acc(
                Header.render(
                    `package` =
                        kotlinPackage(f).let {
                            if (it == PPackage.DEFAULT) {
                                null
                            } else {
                                it
                            }
                        },
                    imports = imports.map { it.qualifiedName }.sorted(),
                    version = f.desc.context.version,
                    fileName = f.desc.name
                )
            )
        }
    }
}
