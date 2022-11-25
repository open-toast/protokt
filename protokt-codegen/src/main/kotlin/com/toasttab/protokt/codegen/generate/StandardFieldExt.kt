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

package com.toasttab.protokt.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.impl.FieldType.BOOL
import com.toasttab.protokt.codegen.impl.FieldType.BYTES
import com.toasttab.protokt.codegen.impl.FieldType.ENUM
import com.toasttab.protokt.codegen.impl.FieldType.MESSAGE
import com.toasttab.protokt.codegen.impl.FieldType.STRING
import com.toasttab.protokt.codegen.impl.StandardField
import com.toasttab.protokt.codegen.impl.Tag
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.impl.Wrapper.mapKeyConverter
import com.toasttab.protokt.codegen.impl.Wrapper.mapValueConverter
import com.toasttab.protokt.codegen.impl.defaultValue

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
        type == MESSAGE -> CodeBlock.of("$fieldName != null")
        type == BYTES || type == STRING -> CodeBlock.of("%L.isNotEmpty()", name)
        type == ENUM -> CodeBlock.of("%L.value != 0", name)
        type == BOOL -> name
        type.scalar -> CodeBlock.of("%L != %L", name, type.defaultValue)
        else -> throw IllegalStateException("Field doesn't have good nondefault check: $this, $type")
    }
}

internal fun StandardField.boxMap(ctx: Context): CodeBlock {
    if (type != MESSAGE) {
        return CodeBlock.of("")
    }
    val keyParam =
        mapKeyConverter(this, ctx)
            ?.let { CodeBlock.of("%T.unwrap(it.key)", it) }
            ?: CodeBlock.of("it.key")

    val valParam =
        mapValueConverter(this, ctx)
            ?.let { CodeBlock.of("%T.unwrap(it.value)", it) }
            ?: CodeBlock.of("it.value")

    return CodeBlock.of("%T(%L, %L)", className, keyParam, valParam)
}

internal fun StandardField.box(s: CodeBlock) =
    if (type.boxed) {
        CodeBlock.of("%T(%L)", type.boxer, s)
    } else {
        s
    }
