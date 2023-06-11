/*
 * Copyright (c) 2023 Toast, Inc.
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

import com.google.auto.service.AutoService
import java.time.LocalDate

@AutoService(Converter::class)
object LocalDateStringConverter : Converter<LocalDate, String> {
    override val wrapper = LocalDate::class

    override val wrapped = String::class

    override fun wrap(unwrapped: String): LocalDate =
        LocalDate.parse(unwrapped)

    override fun unwrap(wrapped: LocalDate) =
        wrapped.toString()
}

@AutoService(Converter::class)
@Deprecated("use LocalDateStringConverter or upgrade protokt")
object LocalDateConverter : Converter<LocalDate, String> {
    override val wrapper = LocalDateStringConverter.wrapper

    override val wrapped = LocalDateStringConverter.wrapped

    override fun wrap(unwrapped: String) =
        LocalDateStringConverter.wrap(unwrapped)

    override fun unwrap(wrapped: LocalDate) =
        LocalDateStringConverter.unwrap(wrapped)
}
