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

import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.PPackage
import kotlin.reflect.KClass

sealed class Import {
    abstract val qualifiedName: String
    abstract val nestedName: String
    abstract val pkg: PPackage

    data class Class(val pClass: PClass) : Import() {
        override val qualifiedName
            get() = pClass.qualifiedName

        override val pkg: PPackage
            get() = pClass.ppackage

        override val nestedName: String
            get() = pClass.nestedName
    }

    data class Method(override val pkg: PPackage, val name: String) : Import() {
        override val qualifiedName
            get() = "$pkg.$name"

        override val nestedName: String
            get() = name
    }
}

fun method(pkg: String, name: String): Import =
    Import.Method(PPackage.fromString(pkg), name)

fun rtMethod(name: String): Import =
    method("com.toasttab.protokt.rt", name)

fun pclass(kclass: KClass<*>): Import =
    Import.Class(PClass.fromClass(kclass))
