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

package com.toasttab.protokt.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.impl.Message
import com.toasttab.protokt.codegen.impl.Nullability.hasNonNullOption
import com.toasttab.protokt.codegen.impl.Oneof
import com.toasttab.protokt.codegen.impl.StandardField

fun Message.mapFields(
    ctx: Context,
    std: (StandardField) -> CodeBlock,
    oneof: (Oneof, StandardField) -> CodeBlock,
    oneofPreControlFlow: CodeBlock.Builder.(Oneof) -> Unit = {}
): List<CodeBlock> =
    fields.map { field ->
        when (field) {
            is StandardField ->
                standardFieldExecution(ctx, field) { std(field) }
            is Oneof ->
                oneofFieldExecution(field, { oneof(field, it) }, oneofPreControlFlow)
        }
    }

private fun standardFieldExecution(
    ctx: Context,
    field: StandardField,
    stmt: () -> CodeBlock
): CodeBlock {
    val statement = stmt()
    return if (field.hasNonNullOption) {
        buildCodeBlock {
            add(statement)
            add("\n")
        }
    } else {
        buildCodeBlock {
            beginControlFlow("if (%L)", field.nonDefault(ctx))
            add(statement)
            add("\n")
            endControlFlow()
        }
    }
}

private fun oneofFieldExecution(
    field: Oneof,
    stmt: (StandardField) -> CodeBlock,
    preControlFlow: CodeBlock.Builder.(Oneof) -> Unit
): CodeBlock =
    buildCodeBlock {
        preControlFlow(field)
        beginControlFlow("when (${field.fieldName})")
        oneofInstanceConditionals(field) { stmt(it) }.forEach(::add)
        endControlFlow()
    }

private fun oneofInstanceConditionals(f: Oneof, stmt: (StandardField) -> CodeBlock) =
    f.fields
        .sortedBy { it.number }
        .map {
            buildCodeBlock {
                addStatement("is·%T·->\n%L", f.qualify(it), stmt(it))
            }
        }
        .let {
            if (f.hasNonNullOption) {
                it
            } else {
                it + buildCodeBlock { addStatement("null·->·Unit") }
            }
        }
