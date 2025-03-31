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
object Int32ValueConverter : com.toasttab.protokt.ext.Converter<Int, Int32Value> {
    override val wrapper = Int::class

    override val wrapped = Int32Value::class

    override fun wrap(unwrapped: Int32Value) =
        unwrapped.value

    override fun unwrap(wrapped: Int) =
        Int32Value { value = wrapped }
}
