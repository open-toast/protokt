/*
 * Copyright (c) 2021 Toast Inc.
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

object Descriptor {
    object Descriptor : StTemplate(StGroup.Descriptor) {
        fun render(
            fileDescriptorObjectName: String,
            parts: List<List<String>>,
            dependencies: List<String>,
            longDependencies: Boolean,
            descriptorExtensionProperties: List<String>
        ) =
            renderArgs(
                fileDescriptorObjectName,
                parts,
                dependencies,
                longDependencies,
                descriptorExtensionProperties
            )
    }

    object MessageDescriptorProperty : StTemplate(StGroup.Descriptor) {
        fun render(
            topLevel: Boolean,
            typeName: String,
            qualification: String?,
            fileDescriptorObjectName: String,
            index: Int,
            suppressDeprecation: Boolean
        ) =
            renderArgs(
                topLevel,
                typeName,
                qualification,
                fileDescriptorObjectName,
                index,
                suppressDeprecation
            )
    }

    object EnumDescriptorProperty : StTemplate(StGroup.Descriptor) {
        fun render(
            topLevel: Boolean,
            typeName: String,
            qualification: String?,
            fileDescriptorObjectName: String,
            index: Int,
            suppressDeprecation: Boolean
        ) =
            renderArgs(
                topLevel,
                typeName,
                qualification,
                fileDescriptorObjectName,
                index,
                suppressDeprecation
            )
    }
}
