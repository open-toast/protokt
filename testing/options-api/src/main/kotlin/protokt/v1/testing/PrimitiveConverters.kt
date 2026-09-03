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
import protokt.v1.Bytes
import protokt.v1.Converter

data class BoolBox(val wrapped: Boolean)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object BooleanConverter : Converter<Boolean, BoolBox> {
    override val wireType = Boolean::class

    override val valueType = BoolBox::class

    override fun unwrap(wrapped: BoolBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Boolean) =
        BoolBox(unwrapped)
}

data class ByteArrayBox(val wrapped: Bytes)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object BytesConverter : Converter<Bytes, ByteArrayBox> {
    override val wireType = Bytes::class

    override val valueType = ByteArrayBox::class

    override fun unwrap(wrapped: ByteArrayBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Bytes) =
        ByteArrayBox(unwrapped)
}

data class DoubleBox(val wrapped: Double)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object DoubleConverter : Converter<Double, DoubleBox> {
    override val wireType = Double::class

    override val valueType = DoubleBox::class

    override fun unwrap(wrapped: DoubleBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Double) =
        DoubleBox(unwrapped)
}

data class IntBox(val wrapped: Int)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object IntConverter : Converter<Int, IntBox> {
    override val wireType = Int::class

    override val valueType = IntBox::class

    override fun unwrap(wrapped: IntBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Int) =
        IntBox(unwrapped)
}

data class UIntBox(val wrapped: UInt)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object UIntConverter : Converter<UInt, UIntBox> {
    override val wireType = UInt::class

    override val valueType = UIntBox::class

    override fun unwrap(wrapped: UIntBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: UInt) =
        UIntBox(unwrapped)
}

data class LongBox(val wrapped: Long)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object LongConverter : Converter<Long, LongBox> {
    override val wireType = Long::class

    override val valueType = LongBox::class

    override fun unwrap(wrapped: LongBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Long) =
        LongBox(unwrapped)
}

data class ULongBox(val wrapped: ULong)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object ULongConverter : Converter<ULong, ULongBox> {
    override val wireType = ULong::class

    override val valueType = ULongBox::class

    override fun unwrap(wrapped: ULongBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: ULong) =
        ULongBox(unwrapped)
}

data class FloatBox(val wrapped: Float)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object FloatConverter : Converter<Float, FloatBox> {
    override val wireType = Float::class

    override val valueType = FloatBox::class

    override fun unwrap(wrapped: FloatBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: Float) =
        FloatBox(unwrapped)
}

data class StringBox(val wrapped: String)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object StringConverter : Converter<String, StringBox> {
    override val wireType = String::class

    override val valueType = StringBox::class

    override fun unwrap(wrapped: StringBox) =
        wrapped.wrapped

    override fun wrap(unwrapped: String) =
        StringBox(unwrapped)
}
