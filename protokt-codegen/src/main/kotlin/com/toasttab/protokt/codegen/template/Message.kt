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

package com.toasttab.protokt.codegen.template

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.toasttab.protokt.codegen.impl.Deprecation

object Message {
    object Message {
        interface FieldInfo {
            val name: String
        }

        class PropertyInfo(
            override val name: String,
            val propertyType: TypeName,
            val deserializeType: TypeName,
            val dslPropertyType: TypeName,
            val defaultValue: CodeBlock,
            val nullable: Boolean,
            val nonNullOption: Boolean,
            val fieldType: String = "",
            val repeated: Boolean = false,
            val map: Boolean = false,
            val oneof: Boolean = false,
            val wrapped: Boolean = false,
            val overrides: Boolean = false,
            val documentation: List<String>,
            val deprecation: Deprecation.RenderOptions? = null
        ) : FieldInfo

        interface FieldWriteInfo : FieldInfo {
            val fieldName: String
            val conditionals: List<ConditionalParams>

            override val name: String
                get() = fieldName
        }

        class DeserializerInfo(
            val repeated: Boolean,
            val tag: Int,
            val assignment: Assignment
        ) : FieldInfo {
            class Assignment(
                val fieldName: String,
                val value: String
            )

            override val name
                get() = assignment.fieldName
        }
    }
}
