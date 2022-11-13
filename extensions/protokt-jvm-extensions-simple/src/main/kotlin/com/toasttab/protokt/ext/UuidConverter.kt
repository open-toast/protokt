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
object UuidConverter : OptimizedSizeofConverter<UUID, ByteArray> {
    override val wrapper = UUID::class

    override val wrapped = ByteArray::class

    private val sizeofProxy = ByteArray(16)

    override fun sizeof(wrapped: UUID) =
        sizeof(sizeofProxy)

    override fun wrap(unwrapped: ByteArray): UUID {
        require(unwrapped.size == 16) {
            "UUID source must have size 16; had ${unwrapped.size}"
        }

        return ByteBuffer.wrap(unwrapped)
            .run { UUID(long, long) }
    }

    override fun unwrap(wrapped: UUID): ByteArray =
        ByteBuffer.allocate(16)
            .putLong(wrapped.mostSignificantBits)
            .putLong(wrapped.leastSignificantBits)
            .array()
}

object UuidConverter2 {
    fun wrap(unwrapped: Sequence<Byte>): UUID {
        val iterator = unwrapped.iterator()

        val mostSignificantBits = extractLong(iterator)
        val leastSignificantBits = extractLong(iterator)

        var extra = 0
        while (iterator.hasNext()) {
            extra++
        }

        require(extra == 0) {
            "UUID source must have size 16; source had size ${16 + extra}"
        }

        return UUID(mostSignificantBits, leastSignificantBits)
    }

    private fun extractLong(iterator: Iterator<Byte>): Long {
        var long = 0L
        repeat(8) {
            require(iterator.hasNext())
            long = long shl 8
            long = long.or(iterator.next().toLong())
        }
        return long
    }

    fun unwrap(wrapped: UUID): Sequence<Byte> =
        longAsSequence(wrapped.mostSignificantBits) + longAsSequence(wrapped.leastSignificantBits)

    private fun longAsSequence(long: Long) =
        sequence {
            var tmp = java.lang.Long.reverseBytes(long)
            repeat(8) {
                yield(tmp.toByte())
                tmp = tmp ushr 8
            }
        }
}
