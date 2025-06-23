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

package protokt.v1

import kotlin.jvm.JvmStatic

@OnlyForUseByGeneratedProtoCode
object Collections {
    @JvmStatic
    fun <K, V> unmodifiableMap(map: Map<K, V>?): Map<K, V> =
        when {
            map.isNullOrEmpty() -> emptyMap()
            map is UnmodifiableMap -> map
            else -> UnmodifiableMap(map)
        }

    @JvmStatic
    fun <K, V> copyMap(map: Map<K, V>): Map<K, V> =
        when {
            map.isEmpty() -> emptyMap()
            map is UnmodifiableMap -> map
            else -> UnmodifiableMap(LinkedHashMap(map))
        }

    @JvmStatic
    fun <T> unmodifiableList(list: List<T>?): List<T> =
        when {
            list.isNullOrEmpty() -> emptyList()
            list is UnmodifiableList -> list
            else -> UnmodifiableList(list)
        }

    @JvmStatic
    fun <T> copyList(list: List<T>): List<T> =
        when {
            list.isEmpty() -> list
            list is UnmodifiableList -> list
            else -> UnmodifiableList(ArrayList(list))
        }
}

private class UnmodifiableIterator<E>(delegate: Iterator<E>) : Iterator<E> by delegate

private class UnmodifiableListIterator<E>(delegate: ListIterator<E>) : ListIterator<E> by delegate

private open class UnmodifiableCollection<E>(private val delegate: Collection<E>) :
    Collection<E> by delegate {
    override fun iterator(): Iterator<E> =
        UnmodifiableIterator(delegate.iterator())
}

private class UnmodifiableSet<E>(delegate: Collection<E>) :
    UnmodifiableCollection<E>(delegate), Set<E>

private class UnmodifiableMapEntry<K, V>(delegate: Map.Entry<K, V>) : Map.Entry<K, V> by delegate

private class UnmodifiableMapEntries<K, V>(private val delegate: Set<Map.Entry<K, V>>) :
    UnmodifiableCollection<Map.Entry<K, V>>(delegate), Set<Map.Entry<K, V>> {

    override fun iterator(): Iterator<Map.Entry<K, V>> {
        val itr = delegate.iterator()
        return object : Iterator<Map.Entry<K, V>> by itr {
            override fun next() =
                UnmodifiableMapEntry(itr.next())
        }
    }
}

private class UnmodifiableList<T>(
    private val delegate: List<T>
) : List<T> by delegate {
    override fun iterator() =
        UnmodifiableIterator(delegate.iterator())

    override fun listIterator() =
        UnmodifiableListIterator(delegate.listIterator())

    override fun listIterator(index: Int) =
        UnmodifiableListIterator(delegate.listIterator(index))

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
    override val entries
        get() = UnmodifiableMapEntries(delegate.entries)

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
