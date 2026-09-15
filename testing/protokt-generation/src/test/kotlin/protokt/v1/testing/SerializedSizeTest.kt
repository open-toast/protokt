/*
 * Copyright (c) 2026 Toast, Inc.
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

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.Bytes
import protokt.v1.UnknownField
import protokt.v1.UnknownFieldSet

class SerializedSizeTest {
    private val populated =
        ToStringTest2 {
            `val` = Bytes.from(ByteArray(300))
            extra = "some extra content"
        }

    @Test
    fun `serialized size matches the serialized byte count`() {
        val withMap = TestProto2Maps { `val` = mapOf(1 to 2, 3 to 4) }

        assertThat(populated.serializedSize()).isEqualTo(populated.serialize().size)
        assertThat(withMap.serializedSize()).isEqualTo(withMap.serialize().size)
    }

    @Test
    fun `an empty message has size zero on every read`() {
        val empty = ToStringTestEmpty { }

        assertThat(empty.serializedSize()).isEqualTo(0)
        assertThat(empty.serializedSize()).isEqualTo(0)
        assertThat(empty.serialize()).isEmpty()
    }

    @Test
    fun `unknown fields count towards the serialized size`() {
        val withUnknowns =
            populated.copy {
                unknownFields =
                    UnknownFieldSet.Builder().apply {
                        add(UnknownField.fixed32(5u, 10u))
                    }.build()
            }

        assertThat(withUnknowns.serializedSize()).isGreaterThan(populated.serializedSize())
        assertThat(withUnknowns.serializedSize()).isEqualTo(withUnknowns.serialize().size)
    }
}
