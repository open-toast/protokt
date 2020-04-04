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
import com.toasttab.protokt.codegen.PluginContext
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.Wrapper.converter
import com.toasttab.protokt.codegen.impl.Wrapper.foldWrap
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.BytesSlice
import com.toasttab.protokt.rt.Fixed32
import com.toasttab.protokt.rt.Fixed64
import com.toasttab.protokt.rt.Int32
import com.toasttab.protokt.rt.Int64
import com.toasttab.protokt.rt.PType.FIXED32
import com.toasttab.protokt.rt.PType.FIXED64
import com.toasttab.protokt.rt.PType.INT32
import com.toasttab.protokt.rt.PType.INT64
import com.toasttab.protokt.rt.PType.SFIXED32
import com.toasttab.protokt.rt.PType.SFIXED64
import com.toasttab.protokt.rt.PType.SINT32
import com.toasttab.protokt.rt.PType.SINT64
import com.toasttab.protokt.rt.PType.UINT32
import com.toasttab.protokt.rt.PType.UINT64
import com.toasttab.protokt.rt.SFixed32
import com.toasttab.protokt.rt.SFixed64
import com.toasttab.protokt.rt.SInt32
import com.toasttab.protokt.rt.SInt64
import com.toasttab.protokt.rt.UInt32
import com.toasttab.protokt.rt.UInt64
import kotlin.reflect.KClass

class StandardFieldImportResolver(
    private val f: StandardField,
    private val ctx: PluginContext,
    private val pkg: PPackage
) {
    fun imports(): ImmutableSet<Import> =
        listImports() +
            mapImports() +
            ptypeImports() +
            wrapperTypeImports()

    private fun listImports() =
        if (f.repeated) {
            immutableSetOf<Import>(
                rtMethod("copyList"),
                rtMethod("finishList")
            ) +
                if (f.packed) {
                    immutableSetOf<Import>(pclass(UInt32::class))
                } else {
                    immutableSetOf()
                }
        } else {
            immutableSetOf()
        }

    private fun mapImports() =
        if (f.map) {
            setOf(
                rtMethod("sizeofMap"),
                rtMethod("finishList")
            )
        } else {
            setOf()
        }

    private fun ptypeImports() =
        listOfNotNull(
            when (f.type) {
                FIXED32 -> Fixed32::class
                FIXED64 -> Fixed64::class
                INT32 -> Int32::class
                INT64 -> Int64::class
                SFIXED32 -> SFixed32::class
                SFIXED64 -> SFixed64::class
                SINT32 -> SInt32::class
                SINT64 -> SInt64::class
                UINT32 -> UInt32::class
                UINT64 -> UInt64::class
                else -> null
            }?.let { pclass(it) }
        )

    private fun wrapperTypeImports(): Set<Import> {
        val set = mutableSetOf<Import>()

        if (f.options.protokt.bytesSlice) {
            set.add(pclass(BytesSlice::class))
        } else {
            set.add(Import.Class(f.typePClass(ctx)))
        }

        f.foldWrap(
            pkg,
            ctx,
            { },
            { wrapper, wrapped ->
                if (wrapped == ByteArray::class) {
                    set.add(pclass(Bytes::class))
                }
                addIfNotInPackage(set, wrapper)
                addIfNotInPackage(set, converter(wrapper, wrapped, ctx)::class)
            }
        )

        return set
    }

    private fun addIfNotInPackage(set: MutableSet<Import>, kclass: KClass<*>) {
        if (PPackage.fromClassName(kclass.java.name) != pkg) {
            set.add(pclass(kclass))
        }
    }
}
