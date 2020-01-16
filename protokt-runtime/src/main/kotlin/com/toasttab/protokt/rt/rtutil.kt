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
    unknown: MutableMap<Int, Unknown>
) {
    val unk = deserializer.readUnknown()
    unknown[unk.fieldNum] = unknown[unk.fieldNum].let {
        when (it) {
            null -> unk
            else ->
                when (val v = it.value) {
                    is ListVal ->
                        Unknown(unk.fieldNum, ListVal(v.value + unk.value))
                    else ->
                        Unknown(unk.fieldNum, ListVal(listOf(v, unk.value)))
                }
        }
    }
}

fun <K, V> finishMap(map: Map<K, V>?): Map<K, V> =
    if (map.isNullOrEmpty()) {
        emptyMap()
    } else {
        Collections.unmodifiableMap(map)
    }

fun <T> finishList(list: List<T>?): List<T> =
    if (list.isNullOrEmpty()) {
        emptyList()
    } else {
        Collections.unmodifiableList(list)
    }
