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
import protokt.v1.OptimizedSizeOfConverter
import protokt.v1.SizeCodecs.sizeOf

data class CachingId(
    internal val value: Bytes
)

@SuppressWarnings("rawtypes")
@AutoService(Converter::class)
object CachingIdConverter : AbstractConverter<Bytes, CachingId>(), OptimizedSizeOfConverter<Bytes, CachingId> {
    override fun sizeOf(wrapped: CachingId) =
        sizeOf(wrapped.value)

    override fun unwrap(wrapped: CachingId) =
        wrapped.value

    override fun wrap(unwrapped: Bytes) =
        CachingId(unwrapped)
}
