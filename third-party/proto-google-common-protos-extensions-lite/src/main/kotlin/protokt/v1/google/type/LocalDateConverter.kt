/*
 * Copyright (c) 2022 Toast, Inc.
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

package protokt.v1.google.type

import com.google.auto.service.AutoService
import protokt.v1.AbstractConverter
import protokt.v1.Converter
import java.time.LocalDate

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object LocalDateConverter : AbstractConverter<Date, LocalDate>() {
    override fun wrap(unwrapped: Date): LocalDate =
        LocalDate.of(unwrapped.year, unwrapped.month, unwrapped.day)

    override fun unwrap(wrapped: LocalDate) =
        Date {
            year = wrapped.year
            month = wrapped.monthValue
            day = wrapped.dayOfMonth
        }
}
