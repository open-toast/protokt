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

package com.toasttab.protokt.codegen.annotators

import com.squareup.kotlinpoet.CodeBlock
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.impl.Wrapper.mapKeyConverter
import com.toasttab.protokt.codegen.impl.Wrapper.mapValueConverter
import com.toasttab.protokt.codegen.impl.defaultValue
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.protoc.Tag

internal val StandardField.tag
    get() =
        if (repeated && packed) {
            Tag.Packed(number)
        } else {
            Tag.Unpacked(number, type.wireType)
        }

internal val StandardField.tagList
    get() =
        tag.let {
            if (repeated) {
                // For repeated fields, catch the other (packed or non-packed)
                // possibility.
                keepIfDifferent(
                    it,
                    if (packed) {
                        Tag.Unpacked(number, type.wireType)
                    } else {
                        Tag.Packed(number)
                    }
                )
            } else {
                listOf(it)
            }
        }.sorted()

private fun keepIfDifferent(tag: Tag, other: Tag) =
    if (tag.value == other.value) {
        listOf(tag)
    } else {
        listOf(tag, other)
    }

internal val StandardField.deprecated
    get() = options.default.deprecated

internal fun StandardField.nonDefault(ctx: Context): CodeBlock {
    val name = interceptValueAccess(this, ctx)
    return when {
        optional -> CodeBlock.of("$fieldName != null")
        repeated -> CodeBlock.of("$fieldName.isNotEmpty()")
        type == FieldType.MESSAGE -> CodeBlock.of("$fieldName != null")
        type == FieldType.BYTES || type == FieldType.STRING -> CodeBlock.of("%L.isNotEmpty()", name)
        type == FieldType.ENUM -> CodeBlock.of("%L.value != 0", name)
        type == FieldType.BOOL -> name
        type.scalar -> CodeBlock.of("%L != %L", name, type.defaultValue)
        else -> throw IllegalStateException("Field doesn't have good nondefault check: $this, $type")
    }
}

internal fun StandardField.boxMap(ctx: Context): CodeBlock {
    if (type != FieldType.MESSAGE) {
        return CodeBlock.of("")
    }
    val keyParam = mapKeyConverter(this, ctx)?.let { CodeBlock.of("$it.unwrap(it.key)") } ?: CodeBlock.of("it.key")
    val valParam = mapValueConverter(this, ctx)?.let { CodeBlock.of("$it.unwrap(it.value)") } ?: CodeBlock.of("it.value")
    return CodeBlock.of("%T(%L, %L)", typePClass.toTypeName(), keyParam, valParam)
}

internal fun StandardField.box(s: CodeBlock) =
    if (type.boxed) {
        CodeBlock.of("%T(%L)", type.boxer, s)
    } else {
        s
    }
