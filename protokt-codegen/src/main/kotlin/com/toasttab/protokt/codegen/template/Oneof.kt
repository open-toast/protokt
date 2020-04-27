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

object Oneof : StTemplate(
    StGroup.Oneof,
    "oneof"
) {
    fun render(name: String, types: Map<String, Info>, options: Options) =
        zipRender(name, types, options)

    class Info(
        val fieldName: String,
        val type: String,
        val documentation: List<String>,
        val deprecation: Deprecation.RenderOptions?
    )

    class Options(
        val doesImplement: Boolean,
        val implements: String
    )
}
