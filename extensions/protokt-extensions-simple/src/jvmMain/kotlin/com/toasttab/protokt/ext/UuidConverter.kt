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

@file:Suppress("DEPRECATION")

package com.toasttab.protokt.ext

import com.toasttab.protokt.rt.sizeof
import java.nio.ByteBuffer
import java.util.UUID

@Deprecated("for backwards compatibility only")
object UuidConverter : OptimizedSizeofConverter<UUID, ByteArray> {
    override val wrapper = UUID::class

    override val wrapped = ByteArray::class

    private val sizeofProxy = ByteArray(16)

    override fun sizeof(@Suppress("UNUSED_PARAMETER") wrapped: UUID) =
        sizeof(sizeofProxy)

    override fun wrap(unwrapped: ByteArray): UUID {
        val buf = ByteBuffer.wrap(unwrapped)

        require(buf.remaining() == 16) {
            "UUID source must have size 16; had ${buf.remaining()}"
        }

        return buf.run { UUID(long, long) }
    }

    override fun unwrap(wrapped: UUID): ByteArray =
        ByteBuffer.allocate(16)
            .putLong(wrapped.mostSignificantBits)
            .putLong(wrapped.leastSignificantBits)
            .array()
}
