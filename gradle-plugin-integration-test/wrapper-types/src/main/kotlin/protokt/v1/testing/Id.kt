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

package protokt.v1.testing

import com.google.auto.service.AutoService
import protokt.v1.Bytes
import protokt.v1.Converter

data class Id(val value: String)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object IdConverter : Converter<Bytes, Id> {
    override val wrapper = Id::class

    override val wrapped = Bytes::class

    override fun wrap(unwrapped: Bytes) =
        Id(String(unwrapped.bytes))

    override fun unwrap(wrapped: Id) =
        Bytes.from(wrapped.value.toByteArray())
}
