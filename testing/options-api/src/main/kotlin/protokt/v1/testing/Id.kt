/*
 * Copyright (c) 2019 Toast, Inc.
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

data class Id(val value: String)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object IdConverter : AbstractConverter<Bytes, Id>() {
    override fun wrap(unwrapped: Bytes) =
        Id(String(unwrapped.bytes))

    override fun unwrap(wrapped: Id) =
        Bytes.from(wrapped.value.toByteArray().inputStream())
}
