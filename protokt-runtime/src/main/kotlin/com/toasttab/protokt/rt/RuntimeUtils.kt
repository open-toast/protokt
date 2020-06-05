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

import java.util.Collections

fun processUnknown(
    deserializer: KtMessageDeserializer,
    unknown: MutableMap<Int, FieldBuilder>
) {
    val unk = deserializer.readUnknown()
    unknown[unk.fieldNumber] =
        unknown[unk.fieldNumber].let {
            when (it) {
                null -> FieldBuilder().add(unk.value)
                else -> it.add(unk.value)
            }
        }
}

fun <K, V> finishMap(map: Map<K, V>?): Map<K, V> =
    if (map.isNullOrEmpty()) {
        emptyMap()
    } else {
        Collections.unmodifiableMap(map)
    }

fun <K, V> copyMap(map: Map<K, V>): Map<K, V> =
    if (map.isEmpty()) {
        emptyMap()
    } else {
        Collections.unmodifiableMap(LinkedHashMap(map))
    }

fun <T> finishList(list: List<T>?): List<T> =
    if (list.isNullOrEmpty()) {
        emptyList()
    } else {
        Collections.unmodifiableList(list)
    }

fun <T> copyList(list: List<T>): List<T> =
    if (list.isEmpty()) {
        emptyList()
    } else {
        Collections.unmodifiableList(ArrayList(list))
    }
