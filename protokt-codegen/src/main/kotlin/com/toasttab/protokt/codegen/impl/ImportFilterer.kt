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

import com.toasttab.protokt.codegen.model.PPackage

object ImportFilterer {
    fun filterDuplicateSimpleNames(imports: Sequence<Import>): Set<Import> {
        val classImports =
            imports.filterIsInstance<Import.Class>().map { it.pClass }.toSet()

        val nonDuplicateImports = mutableSetOf<Import>()

        imports.forEach { import ->
            if (import is Import.Class) {
                val withSameName =
                    classImports.filter {
                        it.simpleName == import.pClass.simpleName &&
                            it != import.pClass
                    }

                if (withSameName.isEmpty()) {
                    nonDuplicateImports.add(import)
                } else if (
                    withSameName.size == 1 &&
                    withSameName.single().ppackage == PPackage.PROTOKT
                ) {
                    nonDuplicateImports.add(import)
                    nonDuplicateImports.remove(Import.Class(withSameName.single()))
                } else {
                    if (nonDuplicateImports.none {
                            it is Import.Class &&
                                it.pClass.simpleName == import.pClass.simpleName
                        }
                    ) {
                        nonDuplicateImports.add(import)
                    }
                }
            } else {
                nonDuplicateImports.add(import)
            }
        }

        return nonDuplicateImports
    }
}
