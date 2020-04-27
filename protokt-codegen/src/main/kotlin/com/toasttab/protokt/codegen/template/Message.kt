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
            entry: MapEntryInfo,
            serialize: List<SerializerInfo>,
            deserialize: List<DeserializerInfo>,
            sizeof: List<SizeofInfo>,
            properties: List<PropertyInfo>,
            oneofs: List<String>,
            nested: List<String>,
            options: Options
        ) =
            renderArgs(
                message,
                entry,
                serialize,
                deserialize,
                sizeof,
                properties,
                oneofs,
                nested,
                options
            )

        class MessageInfo(
            val name: String,
            val doesImplement: Boolean,
            val implements: String,
            val documentation: List<String>,
            val deprecation: Deprecation.RenderOptions?,
            val suppressDeprecation: Boolean,
            val fullTypeName: String
        )

        class MapEntryInfo(
            val entry: Boolean,
            val kType: String,
            val vType: String
        )

        class PropertyInfo(
            val name: String,
            val type: String,
            val defaultValue: String,
            val messageType: String = "",
            val repeated: Boolean = false,
            val map: Boolean = false,
            val oneOf: Boolean = false,
            val nullable: Boolean = true,
            val wrapped: Boolean = false,
            val nonNullOption: Boolean,
            val overrides: Boolean = false,
            val documentation: List<String>,
            val deprecation: Deprecation.RenderOptions? = null
        )

        class SizeofInfo(
            val std: Boolean,
            val fieldName: String,
            val skipDefaultValue: Boolean,
            /** A singleton list for standard fields; one per type for enum fields */
            val conditionals: List<ConditionalParams>
        )

        class SerializerInfo(
            val std: Boolean,
            val fieldName: String,
            val skipDefaultValue: Boolean,
            /** A singleton list for standard fields; one per type for enum fields */
            val conditionals: List<ConditionalParams>
        )

        class DeserializerInfo(
            val std: Boolean,
            val repeated: Boolean,
            val tag: String,
            val assignment: Assignment
        ) {
            class Assignment(
                val fieldName: String,
                val value: String,
                val long: Boolean
            )
        }

        class Options(
            val wellKnownType: Boolean,
            val longDeserializer: Boolean
        )
    }
}
