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

import com.github.andrewoma.dexx.kollection.ImmutableSet
import com.github.andrewoma.dexx.kollection.immutableSetOf
import com.toasttab.protokt.codegen.impl.Wrapper.converter
import com.toasttab.protokt.codegen.impl.Wrapper.foldWrap
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.BytesSlice
import com.toasttab.protokt.rt.UInt32

class StandardFieldImportResolver(
    private val f: StandardField,
    private val ctx: ProtocolContext,
    private val pkg: PPackage
) {
    fun imports(): ImmutableSet<Import> =
        listImports() +
            mapImports() +
            fieldTypeImports() +
            wrapperTypeImports()

    private fun listImports() =
        if (f.repeated && !f.map) {
            immutableSetOf(
                rtMethod("copyList"),
                rtMethod("finishList")
            ) +
                if (f.packed) {
                    immutableSetOf(pclass(UInt32::class))
                } else {
                    immutableSetOf()
                }
        } else {
            immutableSetOf()
        }

    private fun mapImports() =
        if (f.map) {
            setOf(rtMethod("sizeofMap"))
        } else {
            setOf()
        }

    private fun fieldTypeImports() =
        f.type.inlineRepresentation?.let {
            listOf(pclass(it))
        } ?: emptyList()

    private fun wrapperTypeImports(): Set<Import> {
        val set = mutableSetOf<Import>()

        if (f.options.protokt.bytesSlice) {
            set.add(pclass(BytesSlice::class))
        } else {
            set.add(Import.Class(f.typePClass))
        }

        f.foldWrap(
            pkg,
            ctx,
            { },
            { wrapper, wrapped ->
                if (wrapped == ByteArray::class) {
                    set.add(pclass(Bytes::class))
                }
                set.add(pclass(wrapper))
                set.add(pclass(converter(wrapper, wrapped, ctx)::class))
            }
        )

        return set
    }
}
