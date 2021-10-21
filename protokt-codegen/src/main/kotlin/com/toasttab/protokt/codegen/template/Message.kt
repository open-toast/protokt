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
import com.toasttab.protokt.codegen.model.PClass

object Message {
    object Message {
        interface FieldInfo {
            val name: String
        }

        /**
         * Contains type information about a protobuf field (or oneof) that is relevant to the Kotlin interface
         */
        class PropertyInfo(
            override val name: String,
            val propertyType: TypeName,
            val deserializeType: TypeName,
            val dslPropertyType: TypeName,
            val defaultValue: CodeBlock,
            val nullable: Boolean,
            val nonNullOption: Boolean,
            val pClass: PClass? = null,
            val fieldType: String = "",
            val repeated: Boolean = false,
            val map: Boolean = false,
            val oneof: Boolean = false,
            val wrapped: Boolean = false,
            val overrides: Boolean = false,
            val documentation: List<String>,
            val deprecation: Deprecation.RenderOptions? = null
        ) : FieldInfo

        /**
         * Information relevant to reading and writing a message (and other stuff outside the standard access interface?)
         */
        interface FieldWriteInfo : FieldInfo {
            val fieldName: String
            val conditionals: List<ConditionalParams>

            override val name: String
                get() = fieldName
        }

        class SizeofInfo(
            val std: Boolean,
            override val fieldName: String,
            val skipDefaultValue: Boolean,
            /** A singleton list for standard fields; one per type for enum fields */
            override val conditionals: List<ConditionalParams>
        ) : FieldWriteInfo

        class SerializerInfo(
            val std: Boolean,
            override val fieldName: String,
            val skipDefaultValue: Boolean,
            /** A singleton list for standard fields; one per type for enum fields */
            override val conditionals: List<ConditionalParams>
        ) : FieldWriteInfo

        class DeserializerInfo(
            val std: Boolean,
            val repeated: Boolean,
            val tag: Int,
            val assignment: Assignment
        ) : FieldInfo {
            class Assignment(
                val fieldName: String,
                val value: String,
                val long: Boolean
            )

            override val name
                get() = assignment.fieldName
        }

        class Options(
            val wellKnownType: Boolean,
            val longDeserializer: Boolean
        )
    }
}
