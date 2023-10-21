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
import protokt.v1.SizeCodecs.sizeOf
import protokt.v1.google.protobuf.BytesValue
import java.util.UUID

@AutoService(Converter::class)
object UuidBytesValueConverter : OptimizedSizeOfConverter<BytesValue, UUID> {
    override val wrapper = UUID::class

    override val wrapped = BytesValue::class

    private val sizeOfProxy =
        BytesValue { value = Bytes.from(ByteArray(16)) }

    override fun sizeOf(wrapped: UUID) =
        sizeOf(sizeOfProxy)

    override fun wrap(unwrapped: BytesValue) =
        UuidBytesConverter.wrap(unwrapped.value)

    override fun unwrap(wrapped: UUID) =
        BytesValue { value = UuidBytesConverter.unwrap(wrapped) }
}
