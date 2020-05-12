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

object Entry {
    object Entry : StTemplate(StGroup.Entry) {
        fun render(
            name: String,
            key: PropertyInfo,
            value: PropertyInfo
        ) =
            renderArgs(name, key, value)

        class PropertyInfo(
            val propertyType: String,
            val messageType: String,
            val deserializeType: String,
            val sizeof: String,
            val serialize: String,
            val defaultValue: String,
            val deserialize: DeserializerInfo
        )

        class DeserializerInfo(
            val tag: String,
            val assignment: String
        )
    }
}
