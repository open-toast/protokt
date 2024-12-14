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

package protokt.v1.testing

import com.google.auto.service.AutoService
import protokt.v1.AbstractConverter
import protokt.v1.Bytes
import protokt.v1.Converter

data class BoolBox(val wrapped: Boolean)

@AutoService(Converter::class)
object BooleanConverter : AbstractConverter<Boolean, BoolBox>() {
    override fun unwrap(wrapped: BoolBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Boolean) =
        BoolBox(unwrapped)
}

data class ByteArrayBox(val wrapped: Bytes)

@AutoService(Converter::class)
object BytesConverter : AbstractConverter<Bytes, ByteArrayBox>() {
    override fun unwrap(wrapped: ByteArrayBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Bytes) =
        ByteArrayBox(unwrapped)
}

data class DoubleBox(val wrapped: Double)

@AutoService(Converter::class)
object DoubleConverter : AbstractConverter<Double, DoubleBox>() {
    override fun unwrap(wrapped: DoubleBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Double) =
        DoubleBox(unwrapped)
}

data class IntBox(val wrapped: Int)

@AutoService(Converter::class)
object IntConverter : AbstractConverter<Int, IntBox>() {
    override fun unwrap(wrapped: IntBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Int) =
        IntBox(unwrapped)
}

data class UIntBox(val wrapped: UInt)

@AutoService(Converter::class)
object UIntConverter : AbstractConverter<UInt, UIntBox>() {
    override fun unwrap(wrapped: UIntBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: UInt) =
        UIntBox(unwrapped)
}

data class LongBox(val wrapped: Long)

@AutoService(Converter::class)
object LongConverter : AbstractConverter<Long, LongBox>() {
    override fun unwrap(wrapped: LongBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Long) =
        LongBox(unwrapped)
}

data class ULongBox(val wrapped: ULong)

@AutoService(Converter::class)
object ULongConverter : AbstractConverter<ULong, ULongBox>() {
    override fun unwrap(wrapped: ULongBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: ULong) =
        ULongBox(unwrapped)
}

data class FloatBox(val wrapped: Float)

@AutoService(Converter::class)
object FloatConverter : AbstractConverter<Float, FloatBox>() {
    override fun unwrap(wrapped: FloatBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Float) =
        FloatBox(unwrapped)
}

data class StringBox(val wrapped: String)

@AutoService(Converter::class)
object StringConverter : AbstractConverter<String, StringBox>() {
    override fun unwrap(wrapped: StringBox) =
        wrapped.wrapped
    
    override fun wrap(unwrapped: String) =
        StringBox(unwrapped)
}
