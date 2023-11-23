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

@file:Suppress("DEPRECATION")

package com.toasttab.protokt.ext

import com.google.auto.service.AutoService
import com.toasttab.protokt.BytesValue
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.asReadOnlyBuffer
import com.toasttab.protokt.rt.sizeof
import java.nio.ByteBuffer
import java.util.UUID

@Deprecated("for backwards compatibility only")
@AutoService(Converter::class)
object UuidBytesValueConverter : OptimizedSizeofConverter<UUID, BytesValue> {
    override val wrapper = UUID::class

    override val wrapped = BytesValue::class

    private val sizeofProxy =
        BytesValue { value = Bytes(ByteArray(16)) }

    override fun sizeof(wrapped: UUID) =
        sizeof(sizeofProxy)

    override fun wrap(unwrapped: BytesValue): UUID {
        val buf = unwrapped.value.asReadOnlyBuffer()

        require(buf.remaining() == 16) {
            "UUID source must have size 16; had ${buf.remaining()}"
        }

        return buf.run { UUID(long, long) }
    }

    override fun unwrap(wrapped: UUID): BytesValue =
        BytesValue {
            value = Bytes(
                ByteBuffer.allocate(16)
                    .putLong(wrapped.mostSignificantBits)
                    .putLong(wrapped.leastSignificantBits)
                    .array()
            )
        }
}
