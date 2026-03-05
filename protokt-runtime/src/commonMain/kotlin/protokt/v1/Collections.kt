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

@file:OptIn(OnlyForUseByGeneratedProtoCode::class)

package protokt.v1

import kotlin.jvm.JvmStatic

@OnlyForUseByGeneratedProtoCode
interface ListBuilder<T> {
    fun add(element: T)

    fun addAll(elements: Iterable<T>)

    fun build(): List<T>
}

@OnlyForUseByGeneratedProtoCode
interface MapBuilder<K, V> {
    fun put(key: K, value: V)

    fun putAll(from: Map<K, V>)

    fun build(): Map<K, V>
}

@OnlyForUseByGeneratedProtoCode
object Collections {
    @JvmStatic
    fun <T> freezeList(list: List<T>): List<T> =
        collectionFactory.freezeList(list)

    @JvmStatic
    fun <K, V> freezeMap(map: Map<K, V>): Map<K, V> =
        collectionFactory.freezeMap(map)

    @JvmStatic
    fun <T> listBuilder(): ListBuilder<T> =
        collectionFactory.listBuilder()

    @JvmStatic
    fun <K, V> mapBuilder(): MapBuilder<K, V> =
        collectionFactory.mapBuilder()
}

internal object DefaultCollectionFactory : CollectionFactory {
    override fun <T> listBuilder(): ListBuilder<T> =
        MutableListBuilderImpl()

    override fun <K, V> mapBuilder(): MapBuilder<K, V> =
        MutableMapBuilderImpl()

    override fun <T> freezeList(list: List<T>): List<T> =
        when {
            list.isEmpty() -> list
            list is UnmodifiableList -> list
            else -> UnmodifiableList(ArrayList(list))
        }

    override fun <K, V> freezeMap(map: Map<K, V>): Map<K, V> =
        when {
            map.isEmpty() -> emptyMap()
            map is UnmodifiableMap -> map
            else -> UnmodifiableMap(LinkedHashMap(map))
        }

    override fun <T> listPlus(list: List<T>, element: T): List<T> =
        ArrayList<T>(list.size + 1).apply {
            addAll(list)
            add(element)
        }

    override fun <T> listPlusAll(list: List<T>, elements: Iterable<T>): List<T> =
        ArrayList<T>().apply {
            addAll(list)
            addAll(elements)
        }

    override fun <K, V> mapPlus(map: Map<K, V>, pair: Pair<K, V>): Map<K, V> =
        LinkedHashMap(map).apply { put(pair.first, pair.second) }

    override fun <K, V> mapPlusAll(map: Map<K, V>, pairs: Iterable<Pair<K, V>>): Map<K, V> =
        LinkedHashMap(map).apply {
            for ((k, v) in pairs) {
                put(k, v)
            }
        }
}

private class MutableListBuilderImpl<T> : ListBuilder<T> {
    private val list = mutableListOf<T>()

    override fun add(element: T) {
        list.add(element)
    }

    override fun addAll(elements: Iterable<T>) {
        list.addAll(elements)
    }

    override fun build(): List<T> =
        UnmodifiableList(list)
}

private class MutableMapBuilderImpl<K, V> : MapBuilder<K, V> {
    private val map = mutableMapOf<K, V>()

    override fun put(key: K, value: V) {
        map[key] = value
    }

    override fun putAll(from: Map<K, V>) {
        map.putAll(from)
    }

    override fun build(): Map<K, V> =
        UnmodifiableMap(map)
}

internal class UnmodifiableIterator<E>(delegate: Iterator<E>) : Iterator<E> by delegate

internal class UnmodifiableListIterator<E>(delegate: ListIterator<E>) : ListIterator<E> by delegate

internal open class UnmodifiableCollection<E>(
    private val delegate: Collection<E>
) : Collection<E> by delegate {
    override fun iterator(): Iterator<E> =
        UnmodifiableIterator(delegate.iterator())
}

internal class UnmodifiableSet<E>(delegate: Collection<E>) :
    UnmodifiableCollection<E>(delegate),
    Set<E>

internal class UnmodifiableMapEntry<K, V>(delegate: Map.Entry<K, V>) : Map.Entry<K, V> by delegate

internal class UnmodifiableMapEntries<K, V>(
    private val delegate: Set<Map.Entry<K, V>>
) : UnmodifiableCollection<Map.Entry<K, V>>(delegate),
    Set<Map.Entry<K, V>> {

    override fun iterator(): Iterator<Map.Entry<K, V>> {
        val itr = delegate.iterator()
        return object : Iterator<Map.Entry<K, V>> by itr {
            override fun next() =
                UnmodifiableMapEntry(itr.next())
        }
    }
}

internal class UnmodifiableList<T>(
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

internal class UnmodifiableMap<K, V>(
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
