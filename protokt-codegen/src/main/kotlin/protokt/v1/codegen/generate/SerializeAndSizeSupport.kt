/*
 * Copyright (c) 2022 Toast, Inc.
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
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Wrapper.interceptValueAccess
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.Oneof
import protokt.v1.codegen.util.StandardField
import protokt.v1.codegen.util.defaultValue
import protokt.v1.reflect.FieldType

internal fun Message.mapFields(
    ctx: Context,
    properties: List<PropertySpec>,
    skipConditionalForUnpackedRepeatedFields: Boolean,
    std: (StandardField, PropertySpec) -> CodeBlock,
    oneof: (Oneof, StandardField, PropertySpec) -> CodeBlock
): List<CodeBlock> =
    fields.zip(properties)
        .map { (field, property) ->
            when (field) {
                is StandardField ->
                    standardFieldExecution(ctx, field, property, skipConditionalForUnpackedRepeatedFields) { std(field, property) }
                is Oneof ->
                    oneofFieldExecution(field) { oneof(field, it, property) }
            }
        }

private fun standardFieldExecution(
    ctx: Context,
    field: StandardField,
    property: PropertySpec,
    skipConditional: Boolean,
    stmt: () -> CodeBlock
): CodeBlock {
    fun CodeBlock.Builder.addStmt() {
        add(stmt())
        add("\n")
    }

    return buildCodeBlock {
        if (field.repeated && !field.packed && skipConditional) {
            // skip isNotEmpty check when not packed; will short circuit correctly
            addStmt()
        } else {
            beginControlFlow("if (%L)", field.nonDefault(ctx, property))
            addStmt()
            endControlFlow()
        }
    }
}

private fun StandardField.nonDefault(ctx: Context, property: PropertySpec): CodeBlock {
    if (property.name == "_$fieldName") {
        return CodeBlock.of("%N.isNotDefault()", property)
    }

    val valueAccess = interceptValueAccess(this, ctx, CodeBlock.of("%N", fieldName))
    val defaultCheck =
        when {
            optional -> CodeBlock.of("%N != null", fieldName)
            repeated -> CodeBlock.of("%N.isNotEmpty()", fieldName)
            type == FieldType.Message -> CodeBlock.of("%N != null", fieldName)
            type == FieldType.Bytes || type == FieldType.String -> CodeBlock.of("%L.isNotEmpty()", valueAccess)
            type == FieldType.Enum -> CodeBlock.of("%L.value != 0", valueAccess)
            type == FieldType.Bool -> valueAccess
            type.scalar -> CodeBlock.of("%L != %L", valueAccess, type.defaultValue)
            else -> error("Field doesn't have nondefault check: $this, $type")
        }

    return defaultCheck
}

private fun oneofFieldExecution(
    field: Oneof,
    stmt: (StandardField) -> CodeBlock
): CodeBlock =
    buildCodeBlock {
        beginControlFlow("when (%N)", field.fieldName)
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
        } + buildCodeBlock { addStatement("null·->·Unit") }

internal fun Oneof.qualify(f: StandardField) =
    className.nestedClass(fieldTypeNames.getValue(f.fieldName))
