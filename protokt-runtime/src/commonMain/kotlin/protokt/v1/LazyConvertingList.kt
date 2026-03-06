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

@Suppress("UNCHECKED_CAST")
@OptIn(OnlyForUseByGeneratedProtoCode::class)
@OnlyForUseByGeneratedProtoCode
class LazyConvertingList<WireT : Any, KotlinT : Any>(
    private val backing: List<*>,
    private val converter: Converter<WireT, KotlinT>
) : AbstractList<KotlinT>() {
    override val size: Int get() = backing.size

    override fun get(index: Int): KotlinT =
        (backing[index] as LazyReference<WireT, KotlinT>).value()

    fun wireGet(index: Int): WireT =
        (backing[index] as LazyReference<WireT, KotlinT>).wireValue()

    fun wireForEach(action: (WireT) -> Unit) {
        for (ref in backing) action((ref as LazyReference<WireT, KotlinT>).wireValue())
    }

    operator fun plus(element: KotlinT): LazyConvertingList<WireT, KotlinT> {
        val newBacking = collectionFactory.listPlus(backing as List<Any?>, LazyReference(element, converter))
        return LazyConvertingList(newBacking, converter)
    }

    operator fun plus(elements: Iterable<KotlinT>): LazyConvertingList<WireT, KotlinT> {
        val newBacking = collectionFactory.listPlusAll(backing as List<Any?>, elements.map { LazyReference(it, converter) })
        return LazyConvertingList(newBacking, converter)
    }

    companion object {
        fun <WireT : Any, KotlinT : Any> fromKotlin(
            kotlinList: List<KotlinT>,
            converter: Converter<WireT, KotlinT>
        ): LazyConvertingList<WireT, KotlinT> =
            LazyConvertingList(kotlinList.map { LazyReference(it, converter) }, converter)
    }
}
