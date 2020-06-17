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

package com.toasttab.protokt.codegen.model

import kotlin.reflect.KCallable
import kotlin.reflect.KClass

sealed class Import {
    abstract val qualifiedName: String
    abstract val simpleName: String
    abstract val pkg: PPackage
    abstract val nested: Boolean

    data class Class(val pClass: PClass) : Import() {
        override val qualifiedName = pClass.qualifiedName
        override val pkg = pClass.ppackage
        override val simpleName = pClass.simpleName
        override val nested = pClass.enclosing.isDefined()
    }

    data class PackageMethod(
        override val pkg: PPackage,
        val name: String
    ) : Import() {
        override val qualifiedName = pkg.qualify(name)
        override val simpleName = name
        override val nested = false
    }

    data class ClassMethod(
        val enclosingClass: PClass,
        val name: String
    ) : Import() {
        override val pkg = enclosingClass.ppackage
        override val qualifiedName = "${enclosingClass.qualifiedName}.$name"
        override val simpleName = name
        override val nested = true
    }
}

fun method(pkg: String, name: String): Import =
    Import.PackageMethod(PPackage.fromString(pkg), name)

fun rtMethod(callable: KCallable<*>): Import =
    Import.PackageMethod(PPackage.PROTOKT_RT, callable.name)

fun pclass(kclass: KClass<*>): Import =
    Import.Class(PClass.fromClass(kclass))
