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

import com.toasttab.protokt.codegen.annotators.Annotator.protoktPkg
import com.toasttab.protokt.codegen.annotators.Annotator.protoktRtPkg

class PPackage
private constructor(
    private val components: List<String>
) {
    init {
        require(components.none { it.isEmpty() }) {
            "invalid package name components: $components"
        }
    }

    val default = components.isEmpty()

    fun qualify(name: String) =
        if (this == DEFAULT) {
            name
        } else {
            "$this.$name"
        }

    override fun toString() =
        components.joinToString(".")

    override fun equals(other: Any?) =
        other is PPackage && other.components == components

    override fun hashCode() =
        components.hashCode()

    companion object {
        val DEFAULT = PPackage(emptyList())
        val KOTLIN = fromString("kotlin")
        val PROTOKT = fromString(protoktPkg)
        val PROTOKT_RT = fromString(protoktRtPkg)

        fun fromString(`package`: String) =
            if (`package`.isEmpty()) {
                DEFAULT
            } else {
                PPackage(`package`.split('.'))
            }

        fun fromClassName(
            name: String
        ): PPackage {
            val classNameStartIdx = name.indexOfFirst { it.isUpperCase() }
            validate(classNameStartIdx, name)

            // Sometimes fullyQualifiedTypeName is unqualified.
            // e.g. String, Int, GeneratedCodeInfo.Annotation, etc.
            return if (classNameStartIdx == 0) {
                DEFAULT
            } else {
                PPackage(
                    name
                        .substring(0..(classNameStartIdx - 2))
                        .split('.')
                )
            }
        }

        private fun validate(classNameStartIdx: Int, name: String) {
            if (classNameStartIdx == -1) {
                throw IllegalArgumentException("No capital letter found: $name")
            }

            if (classNameStartIdx == 1) {
                throw IllegalArgumentException(
                    "Invalid name; cannot have a package with separator in one " +
                        "char: $name"
                )
            }

            if (classNameStartIdx != 0 && name[classNameStartIdx - 1] != '.') {
                throw IllegalArgumentException(
                    "Char before first capital letter must be a package separator"
                )
            }
        }
    }
}
