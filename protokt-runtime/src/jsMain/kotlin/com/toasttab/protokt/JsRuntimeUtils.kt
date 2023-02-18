/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt

actual fun <T> unmodifiableList(list: List<T>): List<T> =
    UnmodifiableList(list)

actual fun <K, V> unmodifiableMap(map: Map<K, V>): Map<K, V> =
    UnmodifiableMap(map)

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
    class Entry<K, V>(
        override val key: K,
        override val value: V
    ) : Map.Entry<K, V>

    override val entries by lazy {
        UnmodifiableSet(delegate.entries.mapTo(HashSet(delegate.entries.size)) { (k, v) -> Entry(k, v) })
    }

    override val keys
        get() = UnmodifiableSet(delegate.keys)

    override val values
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
