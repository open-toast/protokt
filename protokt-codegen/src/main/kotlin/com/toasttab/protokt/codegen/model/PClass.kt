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

package com.toasttab.protokt.codegen.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.toasttab.protokt.codegen.impl.STAnnotator.protoktExtFqcn
import kotlin.reflect.KClass

data class PClass(
    val simpleName: String,
    val ppackage: PPackage,
    val enclosing: Option<PClass>
) {
    // do not fully qualify items in com.toasttab.protokt.ext; use a wildcard
    // import (see HeaderAccumulator.kt)
    val qualifiedName
        get() =
            "${if (ppackage.default) "" else "$ppackage."}$nestedName"
                .removePrefix("$protoktExtFqcn.")

    val nestedName: String
        get() =
            enclosing.fold(
                { simpleName },
                { "${it.nestedName}.$simpleName" }
            )

    fun isInPackage(ppackage: PPackage) =
        this.ppackage == ppackage

    fun unqualify(pkg: PPackage) =
        if (isInPackage(pkg)) {
            nestedName
        } else {
            qualifiedName
        }

    fun qualify(pkg: PPackage): PClass {
        check(ppackage.default) {
            "cannot qualify non-default package class: $this, $pkg"
        }
        return if (pkg.default) {
            this
        } else {
            fromName("$pkg.$nestedName")
        }
    }

    companion object {
        fun fromClass(klass: KClass<*>) =
            fromName(klass.qualifiedName!!)

        fun fromName(name: String) =
            PPackage.fromClassName(name).let {
                PClass(
                    name.substringAfterLast('.'),
                    it,
                    enclosing(name, it)
                )
            }

        private fun enclosing(
            fqName: String,
            ppackage: PPackage
        ): Option<PClass> {
            val withoutLastDot = fqName.substringBeforeLast('.')
            val components = withoutLastDot.split('.')
            components.forEach { check(it.isNotEmpty()) { "$fqName invalid" } }

            return if (ppackage.default) {
                if (withoutLastDot == fqName) {
                    return None
                }

                if (components.size == 1) {
                    Some(PClass(components.single(), ppackage, None))
                } else {
                    Some(
                        PClass(
                            components.last(),
                            ppackage,
                            enclosing(withoutLastDot, ppackage)
                        )
                    )
                }
            } else {
                if (
                    components.isEmpty() ||
                    components.last().first().isLowerCase()
                ) {
                    None
                } else {
                    Some(
                        PClass(
                            components.last(),
                            ppackage,
                            enclosing(withoutLastDot, ppackage)
                        )
                    )
                }
            }
        }
    }
}

fun PClass.possiblyQualify(pkg: PPackage) =
    if (ppackage.default) {
        qualify(pkg)
    } else {
        this
    }
