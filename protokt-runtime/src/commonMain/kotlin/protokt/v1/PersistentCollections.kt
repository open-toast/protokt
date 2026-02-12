/*
 * Copyright (c) 2026 Toast, Inc.
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

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlin.jvm.JvmStatic

internal object PersistentCollections {
    @JvmStatic
    fun <T> listBuilder(): ListBuilder<T> =
        PersistentListBuilderImpl()

    @JvmStatic
    fun <K, V> mapBuilder(): MapBuilder<K, V> =
        PersistentMapBuilderImpl()

    @JvmStatic
    fun isFrozenList(list: List<*>): Boolean =
        list is PersistentList

    @JvmStatic
    fun isFrozenMap(map: Map<*, *>): Boolean =
        map is PersistentMap

    @JvmStatic
    fun <T> listPlus(list: List<T>, element: T): List<T> =
        if (list is PersistentList) {
            list.add(element)
        } else {
            ArrayList<T>(list.size + 1).apply {
                addAll(list)
                add(element)
            }
        }

    @JvmStatic
    fun <T> listPlusAll(list: List<T>, elements: Iterable<T>): List<T> =
        if (list is PersistentList) {
            val collection = elements as? Collection ?: elements.toList()
            list.addAll(collection)
        } else {
            ArrayList<T>().apply {
                addAll(list)
                addAll(elements)
            }
        }

    @JvmStatic
    fun <K, V> mapPlus(map: Map<K, V>, pair: Pair<K, V>): Map<K, V> =
        if (map is PersistentMap) {
            map.put(pair.first, pair.second)
        } else {
            LinkedHashMap(map).apply { put(pair.first, pair.second) }
        }

    @JvmStatic
    fun <K, V> mapPlusAll(map: Map<K, V>, pairs: Iterable<Pair<K, V>>): Map<K, V> =
        if (map is PersistentMap) {
            var result: PersistentMap<K, V> = map
            for ((k, v) in pairs) {
                result = result.put(k, v)
            }
            result
        } else {
            LinkedHashMap(map).apply {
                for ((k, v) in pairs) {
                    put(k, v)
                }
            }
        }
}

private class PersistentListBuilderImpl<T> : ListBuilder<T> {
    private val builder = persistentListOf<T>().builder()
    override fun add(element: T) {
        builder.add(element)
    }
    override fun addAll(elements: Iterable<T>) {
        builder.addAll(elements)
    }
    override fun build(): List<T> =
        builder.build()
}

private class PersistentMapBuilderImpl<K, V> : MapBuilder<K, V> {
    private val builder = persistentMapOf<K, V>().builder()
    override fun put(key: K, value: V) {
        builder[key] = value
    }
    override fun putAll(from: Map<K, V>) {
        builder.putAll(from)
    }
    override fun build(): Map<K, V> =
        builder.build()
}
