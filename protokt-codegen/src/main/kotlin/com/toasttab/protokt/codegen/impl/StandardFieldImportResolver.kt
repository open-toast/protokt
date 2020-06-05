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
import com.toasttab.protokt.codegen.impl.Wrapper.foldFieldWrap
import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.model.pclass
import com.toasttab.protokt.codegen.model.rtMethod
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.BytesSlice
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UInt32
import com.toasttab.protokt.rt.copyList
import com.toasttab.protokt.rt.copyMap
import com.toasttab.protokt.rt.finishList
import com.toasttab.protokt.rt.finishMap
import com.toasttab.protokt.rt.sizeofMap
import kotlin.reflect.KCallable

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
                rtMethod(COPY_LIST),
                rtMethod(FINISH_LIST)
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
            setOf(
                rtMethod(SIZEOF_MAP),
                rtMethod(COPY_MAP),
                rtMethod(FINISH_MAP)
            )
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

        f.foldFieldWrap(
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

private val FINISH_LIST = finishList<Any>()

private fun <T> finishList(): KCallable<*> {
    val finishList: (List<T>?) -> List<T> = ::finishList
    return finishList as KCallable<*>
}

private val COPY_LIST = copyList<Any>()

private fun <T> copyList(): KCallable<*> {
    val copyList: (List<T>) -> List<T> = ::copyList
    return copyList as KCallable<*>
}

private val SIZEOF_MAP = sizeofMap<Any, Any>()

private fun <K, V> sizeofMap(): KCallable<*> {
    val sizeofMap: (Map<K, V>, Tag, (K, V) -> Int) -> Int = ::sizeofMap
    return sizeofMap as KCallable<*>
}

private val COPY_MAP = copyMap<Any, Any>()

private fun <K, V> copyMap(): KCallable<*> {
    val copyMap: (Map<K, V>) -> Map<K, V> = ::copyMap
    return copyMap as KCallable<*>
}

private val FINISH_MAP = finishMap<Any, Any>()

private fun <K, V> finishMap(): KCallable<*> {
    val finishMap: (Map<K, V>?) -> Map<K, V> = ::finishMap
    return finishMap as KCallable<*>
}
