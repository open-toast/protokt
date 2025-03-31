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

package com.toasttab.protokt

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
object DoubleValueConverter : com.toasttab.protokt.ext.Converter<Double, DoubleValue> {
    override val wrapper = Double::class

    override val wrapped = DoubleValue::class

    override fun wrap(unwrapped: DoubleValue) =
        unwrapped.value

    override fun unwrap(wrapped: Double) =
        DoubleValue { value = wrapped }
}
