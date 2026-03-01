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

package protokt.v1

/**
 * Interface implemented by generated message builder DSL classes. Provides
 * [plus] operators that leverage structural sharing when the receiver is a
 * persistent collection, shadowing the standard-library [plus] extensions
 * within the builder scope.
 */
@OnlyForUseByGeneratedProtoCode
interface BuilderScope {
    operator fun <T> List<T>.plus(element: T): List<T> =
        collectionFactory.listPlus(this, element)

    operator fun <T> List<T>.plus(elements: Iterable<T>): List<T> =
        collectionFactory.listPlusAll(this, elements)

    operator fun <K, V> Map<K, V>.plus(pair: Pair<K, V>): Map<K, V> =
        collectionFactory.mapPlus(this, pair)

    operator fun <K, V> Map<K, V>.plus(pairs: Iterable<Pair<K, V>>): Map<K, V> =
        collectionFactory.mapPlusAll(this, pairs)
}
