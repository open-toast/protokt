/*
 * Copyright (c) 2026 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

internal fun TypeName.equalsExpression(left: CodeBlock, right: CodeBlock): CodeBlock =
    CodeBlock.of("%L == %L", canonicalBits(left), canonicalBits(right))

internal fun TypeName.hashCodeExpression(value: CodeBlock): CodeBlock =
    CodeBlock.of("%L.hashCode()", canonicalBits(value))

internal val TypeName.isFloatingPoint: Boolean
    get() = copy(nullable = false) == Float::class.asTypeName() || copy(nullable = false) == Double::class.asTypeName()

private fun TypeName.canonicalBits(value: CodeBlock): CodeBlock =
    if (isFloatingPoint) {
        CodeBlock.of(if (isNullable) "%L?.toBits()" else "%L.toBits()", value)
    } else {
        value
    }
