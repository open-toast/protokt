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

package protokt.v1.codegen

import org.junit.jupiter.api.Test
import protokt.v1.AbstractConverter
import protokt.v1.Bytes
import protokt.v1.OptimizedSizeOfConverter
import protokt.v1.SizeCodecs
import java.nio.ByteBuffer
import java.util.UUID

class ProtoktCodegenTest : AbstractProtoktCodegenTest() {
    @Test
    fun `step through code generation with debugger`() {
        runPlugin("test.proto")
            .orFail()
            .response
            .fileList
            .forEach {
                println(it.name)
                println(it.content)
            }
    }
}

object UuidBytesConverter : AbstractConverter<Bytes, UUID>(), OptimizedSizeOfConverter<Bytes, UUID> {
    override val acceptsDefaultValue = false

    private val sizeOfProxy = ByteArray(16)

    override fun sizeOf(wrapped: UUID) =
        SizeCodecs.sizeOf(sizeOfProxy)

    override fun wrap(unwrapped: Bytes): UUID {
        val buf = unwrapped.asReadOnlyBuffer()

        require(buf.remaining() == 16) {
            "UUID source must have size 16; had ${buf.remaining()}"
        }

        return buf.run { UUID(long, long) }
    }

    override fun unwrap(wrapped: UUID): Bytes =
        Bytes.from(
            ByteBuffer.allocate(16)
                .putLong(wrapped.mostSignificantBits)
                .putLong(wrapped.leastSignificantBits)
                .array()
        )
}
