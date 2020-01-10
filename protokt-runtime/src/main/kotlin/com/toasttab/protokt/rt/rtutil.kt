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
