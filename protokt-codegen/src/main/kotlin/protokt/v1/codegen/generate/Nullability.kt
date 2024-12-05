/*
 * Copyright (c) 2020 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.TypeName
import protokt.v1.codegen.generate.Wrapper.wrapped
import protokt.v1.codegen.util.Field
import protokt.v1.codegen.util.Oneof
import protokt.v1.codegen.util.StandardField
import protokt.v1.reflect.FieldType

internal object Nullability {
    val Field.generateNonNullAccessor
        get() = this is StandardField && options.protokt.generateNonNullAccessor

    val Field.nullable
        get() = isKotlinRepresentationNullable

    private val Field.isKotlinRepresentationNullable
        get() =
            when (this) {
                is StandardField -> (type == FieldType.Message && !repeated) || optional
                is Oneof -> true
            }

    private val StandardField.isWrappedNonRepeatedPrimitive
        get() =
            wrapped &&
                !repeated &&
                type !in setOf(FieldType.Message, FieldType.Enum)

    fun propertyType(o: Oneof) =
        if (o.generateNonNullAccessor) {
            o.className
        } else {
            o.className.copy(nullable = true)
        }

    fun propertyType(f: StandardField, type: TypeName, wrapperRequiresNullability: Boolean) =
        if (f.nullable || wrapperRequiresNullability) {
            type.copy(nullable = true)
        } else {
            type
        }

    fun deserializeType(f: StandardField, type: TypeName) =
        if (
            f.repeated ||
            f.nullable ||
            f.isKotlinRepresentationNullable ||
            f.isWrappedNonRepeatedPrimitive
        ) {
            type.copy(nullable = true)
        } else {
            type
        }

    fun dslPropertyType(f: StandardField, type: TypeName) =
        if (
            f.isKotlinRepresentationNullable ||
            f.isWrappedNonRepeatedPrimitive
        ) {
            type.copy(nullable = true)
        } else {
            type
        }

    fun nonNullPropName(propName: String) =
        "require${propName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
}
