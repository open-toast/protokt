/*
 * Copyright (c) 2019 Toast, Inc.
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

import java.util.Collections

@Deprecated("for backwards compatibility only")
fun <K, V> finishMap(map: Map<K, V>?): Map<K, V> =
    if (map.isNullOrEmpty()) {
        emptyMap()
    } else {
        unmodifiableMap(map)
    }

@Deprecated("for backwards compatibility only")
fun <K, V> copyMap(map: Map<K, V>): Map<K, V> =
    if (map.isEmpty()) {
        emptyMap()
    } else {
        unmodifiableMap(LinkedHashMap(map))
    }

@Deprecated("for backwards compatibility only")
fun <T> finishList(list: List<T>?): List<T> =
    if (list.isNullOrEmpty()) {
        emptyList()
    } else {
        unmodifiableList(list)
    }

@Deprecated("for backwards compatibility only")
fun <T> copyList(list: List<T>): List<T> =
    if (list.isEmpty()) {
        emptyList()
    } else {
        unmodifiableList(ArrayList(list))
    }

internal inline fun <reified T> T.equalsUsingSequence(
    other: Any?,
    size: (T) -> Int,
    asSequence: (T) -> Sequence<*>
) =
    other is T &&
        size(this) == size(other) &&
        asSequence(this).zip(asSequence(other)).all { (l, r) -> l == r }

internal fun hashCodeUsingSequence(asSequence: Sequence<*>) =
    asSequence.fold(1) { hash, elt -> 31 * hash + elt.hashCode() }

private fun <T> unmodifiableList(list: List<T>): List<T> =
    Collections.unmodifiableList(list)

private fun <K, V> unmodifiableMap(map: Map<K, V>): Map<K, V> =
    Collections.unmodifiableMap(map)
