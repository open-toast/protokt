/*
 * Copyright (c) 2026 Toast Inc.
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

package com.toasttab.protokt.rt

import java.io.Serializable

/**
 * Read-only list backed by an exactly sized array. Replaces
 * `Collections.unmodifiableList(ArrayList)` for finished message fields so a list retains one
 * object and one array instead of a wrapper, an ArrayList, and an array padded to the default
 * capacity. Mutators inherited from [AbstractList] throw [UnsupportedOperationException].
 */
internal class ImmutableArrayList<T>(
    private val elements: Array<Any?>
) : AbstractList<T>(), RandomAccess, Serializable {
    override val size
        get() = elements.size

    @Suppress("UNCHECKED_CAST")
    override fun get(index: Int) =
        elements[index] as T

    companion object {
        private const val serialVersionUID = 1L

        fun <T> copyOf(list: List<T>): ImmutableArrayList<T> =
            ImmutableArrayList(
                if (list is ArrayList<*>) {
                    list.toArray()
                } else {
                    val array = arrayOfNulls<Any>(list.size)
                    var i = 0
                    for (element in list) {
                        array[i++] = element
                    }
                    array
                }
            )
    }
}
