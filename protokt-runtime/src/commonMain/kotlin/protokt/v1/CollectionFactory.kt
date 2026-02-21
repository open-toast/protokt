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

@OnlyForUseByGeneratedProtoCode
interface CollectionFactory {
    fun <T> listBuilder(): ListBuilder<T>
    fun <K, V> mapBuilder(): MapBuilder<K, V>
    fun <T> freezeList(list: List<T>): List<T>
    fun <K, V> freezeMap(map: Map<K, V>): Map<K, V>
    fun <T> listPlus(list: List<T>, element: T): List<T>
    fun <T> listPlusAll(list: List<T>, elements: Iterable<T>): List<T>
    fun <K, V> mapPlus(map: Map<K, V>, pair: Pair<K, V>): Map<K, V>
    fun <K, V> mapPlusAll(map: Map<K, V>, pairs: Iterable<Pair<K, V>>): Map<K, V>
}

internal expect val collectionFactory: CollectionFactory
