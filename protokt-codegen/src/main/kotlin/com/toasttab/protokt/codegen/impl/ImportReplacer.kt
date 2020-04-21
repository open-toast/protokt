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

object ImportReplacer {
    fun replaceImports(code: String, imports: Set<Import>) =
        imports
            // replace longer imports first, as they might be clobbered
            // by parent imports with the same prefix
            .sortedByDescending { it.qualifiedName }
            .fold(code) { body, import ->
                body.replace(
                    // qualified name followed by a non-identifier character
                    "${Regex.escape(import.qualifiedName)}([^a-zA-Z])".toRegex(),
                    "${import.simpleName}$1"
                )
            }
}
