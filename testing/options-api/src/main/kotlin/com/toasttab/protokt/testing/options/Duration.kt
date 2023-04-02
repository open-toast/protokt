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

package com.toasttab.protokt.testing.options

import com.google.auto.service.AutoService
import com.toasttab.protokt.Converter

data class Duration(val value: com.toasttab.protokt.Duration)

@AutoService(Converter::class)
object DurationConverter : Converter<Duration, com.toasttab.protokt.Duration> {
    override val wrapper = Duration::class

    override val wrapped = com.toasttab.protokt.Duration::class

    override fun wrap(unwrapped: com.toasttab.protokt.Duration) =
        Duration(unwrapped)

    override fun unwrap(wrapped: Duration) =
        wrapped.value
}
