/*
 * Copyright (c) 2019 Toast Inc.
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

import com.toasttab.protokt.codegen.impl.Annotator.Context
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField

object Deprecation {
    fun enclosingDeprecation(ctx: Context): Boolean {
        return if (ctx.enclosing.isEmpty()) {
            false
        } else {
            ctx.enclosing
                .subList(0, ctx.enclosing.size)
                .any { it.hasDeprecation }
        }
    }

    val Message.hasDeprecation: Boolean
        get() =
            options.default.deprecated ||
                fields.any {
                    when (it) {
                        is StandardField -> it.deprecated
                        is Oneof -> it.fields.any(StandardField::deprecated)
                    }
                } ||
                nestedTypes.any {
                    when (it) {
                        is Message -> it.hasDeprecation
                        is Enum -> it.hasDeprecation
                        else -> false
                    }
                }

    val Enum.hasDeprecation
        get() =
            options.default.deprecated ||
                values.any { it.options.default.deprecated }

    fun renderOptions(message: String) =
        RenderOptions(message.ifBlank { null })

    class RenderOptions(
        val message: String?
    )
}
