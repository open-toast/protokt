/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.ext

import com.google.auto.service.AutoService
import com.toasttab.protokt.rt.sizeof
import java.nio.ByteBuffer
import java.util.UUID

@AutoService(Converter::class)
object UuidConverter : OptimizedSizeofConverter<UUID, Sequence<Byte>> {
    override val wrapper = UUID::class

    // note: this probably has to be a TypeReference, or Converter an abstract class
    override val wrapped = Sequence::class

    private val sizeofProxy = ByteArray(16)

    override fun sizeof(wrapped: UUID) =
        sizeof(sizeofProxy)

    override fun wrap(unwrapped: Sequence<Byte>): UUID {
        val iterator = unwrapped.iterator()

        val buf = ByteBuffer.allocate(16)
        var taken = 0
        while (iterator.hasNext() && taken++ < 16) {
            buf.put(iterator.next())
        }

        while (iterator.hasNext()) {
            taken++
        }

        require(taken == 16) {
            "UUID source must have size 16; source had size $taken"
        }

        return buf.run { UUID(long, long) }
    }

    override fun unwrap(wrapped: UUID): Sequence<Byte> =
        ByteBuffer.allocate(16)
            .putLong(wrapped.mostSignificantBits)
            .putLong(wrapped.leastSignificantBits)
            .array()
            .asSequence()
}
