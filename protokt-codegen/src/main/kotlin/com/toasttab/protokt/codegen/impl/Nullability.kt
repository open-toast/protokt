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

import com.toasttab.protokt.codegen.impl.Wrapper.wrapped
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Field
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField

internal object Nullability {
    val Field.hasNonNullOption
        get() =
            when (this) {
                is StandardField -> options.protokt.nonNull
                is Oneof -> options.protokt.nonNull
            }

    val Field.nullable
        get() =
            isKotlinRepresentationNullable && !hasNonNullOption

    private val Field.isKotlinRepresentationNullable
        get() =
            when (this) {
                is StandardField -> (type == FieldType.MESSAGE && !repeated) || optional
                is Oneof -> true
            }

    private val StandardField.isWrappedNonRepeatedPrimitive
        get() =
            wrapped &&
                !repeated &&
                type !in setOf(FieldType.MESSAGE, FieldType.ENUM)

    fun propertyType(o: Oneof) =
        if (o.hasNonNullOption) {
            o.name
        } else {
            o.renderNullableType()
        }

    fun propertyType(f: StandardField, type: String) =
        if (f.nullable) {
            renderNullable(type)
        } else {
            type
        }

    fun deserializeType(f: StandardField, type: String) =
        if (
            f.repeated ||
            f.nullable ||
            f.isKotlinRepresentationNullable ||
            f.isWrappedNonRepeatedPrimitive
        ) {
            renderNullable(type)
        } else {
            type
        }

    fun dslPropertyType(f: StandardField, type: String) =
        if (
            f.isKotlinRepresentationNullable ||
            f.isWrappedNonRepeatedPrimitive
        ) {
            renderNullable(type)
        } else {
            type
        }

    fun Oneof.renderNullableType() =
        renderNullable(name)

    private fun renderNullable(s: String) =
        "$s?"
}
