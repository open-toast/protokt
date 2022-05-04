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

package com.toasttab.protokt.rt

fun <K, V> finishMap(map: Map<K, V>?): Map<K, V> =
    if (map.isNullOrEmpty()) {
        emptyMap()
    } else {
        UnmodifiableMap(map)
    }

fun <K, V> copyMap(map: Map<K, V>): Map<K, V> =
    if (map.isEmpty()) {
        emptyMap()
    } else {
        UnmodifiableMap(LinkedHashMap(map))
    }

fun <T> finishList(list: List<T>?): List<T> =
    if (list.isNullOrEmpty()) {
        emptyList()
    } else {
        UnmodifiableList(list)
    }

fun <T> copyList(list: List<T>): List<T> =
    if (list.isEmpty()) {
        emptyList()
    } else {
        UnmodifiableList(ArrayList(list))
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

private class UnmodifiableList<T>(
    private val delegate: List<T>
) : List<T> by delegate {
    override fun equals(other: Any?) =
        other == delegate

    override fun hashCode() =
        delegate.hashCode()

    override fun toString() =
        delegate.toString()
}

private class UnmodifiableMap<K, V>(
    private val delegate: Map<K, V>
) : Map<K, V> by delegate {
    override val entries: Set<Map.Entry<K, V>>
        get() = UnmodifiableSet(delegate.entries)

    override val keys: Set<K>
        get() = UnmodifiableSet(delegate.keys)

    override val values: Collection<V>
        get() = UnmodifiableCollection(delegate.values)

    override fun equals(other: Any?) =
        other == delegate

    override fun hashCode() =
        delegate.hashCode()

    override fun toString() =
        delegate.toString()
}

private class UnmodifiableSet<T>(
    private val delegate: Set<T>
) : Set<T> by delegate {
    override fun equals(other: Any?) =
        other == delegate

    override fun hashCode() =
        delegate.hashCode()

    override fun toString() =
        delegate.toString()
}

private class UnmodifiableCollection<T>(
    private val delegate: Collection<T>
) : Collection<T> by delegate {
    override fun equals(other: Any?) =
        other == delegate

    override fun hashCode() =
        delegate.hashCode()

    override fun toString() =
        delegate.toString()
}
