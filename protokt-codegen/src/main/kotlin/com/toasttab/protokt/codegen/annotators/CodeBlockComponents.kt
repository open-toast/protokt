/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt.codegen.annotators

import com.toasttab.protokt.codegen.impl.namedCodeBlock

internal class CodeBlockComponents(
    private val formatWithNamedArgs: String,
    private val args: Map<String, Any> = emptyMap()
) {
    fun prepend(
        formatWithNamedArgs: String,
        args: Map<String, Any> = emptyMap()
    ): CodeBlockComponents {
        val intersect = args.keys.intersect(this.args.keys)
        check(intersect.isEmpty()) {
            "duplicate keys in args: $intersect"
        }
        return CodeBlockComponents(
            formatWithNamedArgs + " " + this.formatWithNamedArgs,
            args + this.args
        )
    }

    fun append(
        formatWithNamedArgs: String,
        args: Map<String, Any> = emptyMap()
    ) =
        this + CodeBlockComponents(formatWithNamedArgs, args)

    operator fun plus(other: CodeBlockComponents) =
        other.prepend(formatWithNamedArgs, args)

    fun toCodeBlock() =
        namedCodeBlock(formatWithNamedArgs, args)
}
