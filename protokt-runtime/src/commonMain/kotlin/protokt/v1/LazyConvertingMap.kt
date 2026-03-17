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

@OptIn(OnlyForUseByGeneratedProtoCode::class)
@OnlyForUseByGeneratedProtoCode
class LazyConvertingMap<KotlinK : Any, KotlinV : Any>(
    private val backing: Map<Any?, Any?>,
    private val keyWrapped: Boolean,
    private val valueWrapped: Boolean,
    private val keyConverter: Converter<*, *>?,
    private val valueConverter: Converter<*, *>?
) : AbstractMap<KotlinK, KotlinV>() {
    override val size: Int get() = backing.size

    @Suppress("UNCHECKED_CAST")
    override fun get(key: KotlinK): KotlinV? {
        val lookupKey = if (keyWrapped) LazyReference(key as Any, keyConverter as Converter<Any, Any>) else key
        val raw = backing[lookupKey] ?: return null
        return if (valueWrapped) (raw as LazyReference<*, *>).value() as KotlinV else raw as KotlinV
    }

    @Suppress("UNCHECKED_CAST")
    override fun containsKey(key: KotlinK): Boolean {
        val lookupKey = if (keyWrapped) LazyReference(key as Any, keyConverter as Converter<Any, Any>) else key
        return backing.containsKey(lookupKey)
    }

    override val entries: Set<Map.Entry<KotlinK, KotlinV>>
        get() = ConvertingEntrySet()

    @Suppress("UNCHECKED_CAST")
    fun <WireK, WireV> wireEntryForEach(action: (WireK, WireV) -> Unit) {
        for ((k, v) in backing) {
            val wireK = if (keyWrapped) (k as LazyReference<*, *>).wireValue() as WireK else k as WireK
            val wireV = if (valueWrapped) (v as LazyReference<*, *>).wireValue() as WireV else v as WireV
            action(wireK, wireV)
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun plus(pair: Pair<KotlinK, KotlinV>): LazyConvertingMap<KotlinK, KotlinV> {
        val bk = if (keyWrapped) LazyReference(pair.first as Any, keyConverter as Converter<Any, Any>) else pair.first
        val bv = if (valueWrapped) LazyReference(pair.second as Any, valueConverter as Converter<Any, Any>) else pair.second
        val newBacking = collectionFactory.mapPlus(backing, (bk to bv))
        return LazyConvertingMap(newBacking, keyWrapped, valueWrapped, keyConverter, valueConverter)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun plus(other: Map<out KotlinK, KotlinV>): LazyConvertingMap<KotlinK, KotlinV> {
        if (other.isEmpty()) return this
        if (other is LazyConvertingMap<*, *>) {
            var newBacking = backing
            for ((k, v) in other.backing) {
                newBacking = collectionFactory.mapPlus(newBacking, k to v)
            }
            return LazyConvertingMap(newBacking, keyWrapped, valueWrapped, keyConverter, valueConverter)
        }
        var newBacking = backing
        for ((k, v) in other) {
            val bk = if (keyWrapped) LazyReference(k as Any, keyConverter as Converter<Any, Any>) else k
            val bv = if (valueWrapped) LazyReference(v as Any, valueConverter as Converter<Any, Any>) else v
            newBacking = collectionFactory.mapPlus(newBacking, bk to bv)
        }
        return LazyConvertingMap(newBacking, keyWrapped, valueWrapped, keyConverter, valueConverter)
    }

    @Suppress("UNCHECKED_CAST")
    private inner class ConvertingEntrySet : AbstractSet<Map.Entry<KotlinK, KotlinV>>() {
        override val size: Int get() = backing.size

        override fun iterator(): Iterator<Map.Entry<KotlinK, KotlinV>> {
            val backingIterator = backing.entries.iterator()
            return object : Iterator<Map.Entry<KotlinK, KotlinV>> {
                override fun hasNext() =
                    backingIterator.hasNext()

                override fun next(): Map.Entry<KotlinK, KotlinV> {
                    val entry = backingIterator.next()
                    val key = if (keyWrapped) (entry.key as LazyReference<*, *>).value() as KotlinK else entry.key as KotlinK
                    val value = if (valueWrapped) (entry.value as LazyReference<*, *>).value() as KotlinV else entry.value as KotlinV
                    return object : Map.Entry<KotlinK, KotlinV> {
                        override val key: KotlinK = key
                        override val value: KotlinV = value

                        override fun hashCode(): Int =
                            key.hashCode() xor value.hashCode()

                        override fun equals(other: Any?): Boolean =
                            other is Map.Entry<*, *> && key == other.key && value == other.value

                        override fun toString(): String =
                            "$key=$value"
                    }
                }
            }
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <KotlinK : Any, KotlinV : Any> fromKotlin(
            kotlinMap: Map<KotlinK, KotlinV>,
            keyWrapped: Boolean,
            valueWrapped: Boolean,
            keyConverter: Converter<*, *>?,
            valueConverter: Converter<*, *>?
        ): LazyConvertingMap<KotlinK, KotlinV> {
            val backing = linkedMapOf<Any?, Any?>()
            for ((k, v) in kotlinMap) {
                val bk = if (keyWrapped) LazyReference(k as Any, keyConverter as Converter<Any, Any>) else k
                val bv = if (valueWrapped) LazyReference(v as Any, valueConverter as Converter<Any, Any>) else v
                backing[bk] = bv
            }
            return LazyConvertingMap(backing, keyWrapped, valueWrapped, keyConverter, valueConverter)
        }
    }
}
