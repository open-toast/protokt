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

import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.Wrapper.interceptValueAccess
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.protoc.Tag
import com.toasttab.protokt.codegen.protoc.wireFormat
import com.toasttab.protokt.codegen.template.Renderers.Box
import com.toasttab.protokt.codegen.template.Renderers.BoxMap
import com.toasttab.protokt.codegen.template.Renderers.NonDefaultValue

internal val StandardField.tag
    get() =
        if (repeated && packed) {
            Tag.Packed(number)
        } else {
            Tag.Unpacked(number, wireFormat)
        }

internal val StandardField.tagList
    get() =
        tag.let {
            if (repeated) {
                // For repeated fields, catch the other (packed or non-packed)
                // possiblity.
                keepIfDifferent(
                    it,
                    if (packed) {
                        Tag.Unpacked(number, wireFormat)
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

internal fun StandardField.nonDefault(ctx: Context) =
    NonDefaultValue.render(
        field = this,
        name = interceptValueAccess(this, ctx)
    )

internal fun StandardField.boxMap(ctx: Context) =
    BoxMap.render(
        type = type,
        box = unqualifiedNestedTypeName(ctx)
    )

internal fun StandardField.box(s: String) =
    Box.render(
        type = type,
        def = s
    )

internal val StandardField.unqualifiedTypeName
    get() = typePClass.nestedName

internal fun StandardField.unqualifiedNestedTypeName(ctx: Context) =
    ctx.stripRootMessageNamePrefix(unqualifiedTypeName)
