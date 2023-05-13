/*
 * Copyright (c) 2020 Toast, Inc.
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

package com.toasttab.protokt.v1.testing.options

import com.google.auto.service.AutoService
import com.toasttab.protokt.v1.Bytes
import com.toasttab.protokt.v1.Converter

data class BoolBox(val wrapped: Boolean)

@AutoService(Converter::class)
object BooleanConverter : Converter<BoolBox, Boolean> {
    override val wrapped = Boolean::class
    override val wrapper = BoolBox::class
    override fun unwrap(wrapped: BoolBox) = wrapped.wrapped
    override fun wrap(unwrapped: Boolean) = BoolBox(unwrapped)
}

data class ByteArrayBox(val wrapped: Bytes)

@AutoService(Converter::class)
object BytesConverter : Converter<ByteArrayBox, Bytes> {
    override val wrapped = Bytes::class
    override val wrapper = ByteArrayBox::class
    override fun unwrap(wrapped: ByteArrayBox) = wrapped.wrapped
    override fun wrap(unwrapped: Bytes) = ByteArrayBox(unwrapped)
}

data class DoubleBox(val wrapped: Double)

@AutoService(Converter::class)
object DoubleConverter : Converter<DoubleBox, Double> {
    override val wrapped = Double::class
    override val wrapper = DoubleBox::class
    override fun unwrap(wrapped: DoubleBox) = wrapped.wrapped
    override fun wrap(unwrapped: Double) = DoubleBox(unwrapped)
}

data class IntBox(val wrapped: Int)

@AutoService(Converter::class)
object IntConverter : Converter<IntBox, Int> {
    override val wrapped = Int::class
    override val wrapper = IntBox::class
    override fun unwrap(wrapped: IntBox) = wrapped.wrapped
    override fun wrap(unwrapped: Int) = IntBox(unwrapped)
}

data class LongBox(val wrapped: Long)

@AutoService(Converter::class)
object LongConverter : Converter<LongBox, Long> {
    override val wrapped = Long::class
    override val wrapper = LongBox::class
    override fun unwrap(wrapped: LongBox) = wrapped.wrapped
    override fun wrap(unwrapped: Long) = LongBox(unwrapped)
}

data class FloatBox(val wrapped: Float)

@AutoService(Converter::class)
object FloatConverter : Converter<FloatBox, Float> {
    override val wrapped = Float::class
    override val wrapper = FloatBox::class
    override fun unwrap(wrapped: FloatBox) = wrapped.wrapped
    override fun wrap(unwrapped: Float) = FloatBox(unwrapped)
}

data class StringBox(val wrapped: String)

@AutoService(Converter::class)
object StringConverter : Converter<StringBox, String> {
    override val wrapped = String::class
    override val wrapper = StringBox::class
    override fun unwrap(wrapped: StringBox) = wrapped.wrapped
    override fun wrap(unwrapped: String) = StringBox(unwrapped)
}
