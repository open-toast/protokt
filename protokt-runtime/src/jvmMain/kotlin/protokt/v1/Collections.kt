/*
 * Copyright (c) 2023 Toast, Inc.
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

import java.util.Collections

actual object Collections {
    @JvmStatic
    actual fun <K, V> unmodifiableMap(map: Map<K, V>?): Map<K, V> =
        if (map.isNullOrEmpty()) {
            emptyMap()
        } else {
            _unmodifiableMap(map)
        }

    @JvmStatic
    actual fun <K, V> copyMap(map: Map<K, V>): Map<K, V> =
        if (map.isEmpty()) {
            emptyMap()
        } else {
            _unmodifiableMap(LinkedHashMap(map))
        }

    @JvmStatic
    actual fun <T> unmodifiableList(list: List<T>?): List<T> =
        if (list.isNullOrEmpty()) {
            emptyList()
        } else {
            _unmodifiableList(list)
        }

    @JvmStatic
    actual fun <T> copyList(list: List<T>): List<T> =
        if (list.isEmpty()) {
            emptyList()
        } else {
            _unmodifiableList(ArrayList(list))
        }

    private fun <T> _unmodifiableList(list: List<T>) =
        Collections.unmodifiableList(list)

    private fun <K, V> _unmodifiableMap(map: Map<K, V>) =
        Collections.unmodifiableMap(map)
}
