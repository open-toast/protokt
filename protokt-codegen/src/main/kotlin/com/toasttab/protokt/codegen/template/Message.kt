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

import com.toasttab.protokt.codegen.impl.Deprecation

object Message {
    object Message : StTemplate(StGroup.Message) {
        fun render(
            message: MessageInfo,
            serialize: List<SerializerInfo>,
            deserialize: List<DeserializerInfo>,
            sizeof: List<SizeofInfo>,
            properties: List<PropertyInfo>,
            oneofs: List<String>,
            nested: List<String>,
            reflect: ReflectInfo,
            options: Options
        ) =
            renderArgs(
                message,
                serialize,
                deserialize,
                sizeof,
                properties,
                oneofs,
                nested,
                reflect,
                options
            )

        class MessageInfo(
            val name: String,
            val doesImplement: Boolean,
            val implements: String,
            val documentation: List<String>,
            val deprecation: Deprecation.RenderOptions?,
            val suppressDeprecation: Boolean
        )

        interface FieldInfo {
            val name: String
        }

        class PropertyInfo(
            override val name: String,
            val propertyType: String,
            val deserializeType: String,
            val dslPropertyType: String,
            val defaultValue: String,
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

        data class ReflectInfo(
            val fileDescriptorObjectName: String,
            val index: Int,
            val parentName: String?
        )

        class Options(
            val wellKnownType: Boolean,
            val longDeserializer: Boolean
        )
    }
}
