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

package protokt.v1

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class UnknownFieldSetTest {
    @Test
    fun `serialization preserves wire order`() {
        val unknownFields =
            unknownFieldSet(
                UnknownField.lengthDelimited(15u, "abc".encodeToByteArray()),
                UnknownField.varint(15u, 123),
                UnknownField.lengthDelimited(15u, "def".encodeToByteArray()),
                UnknownField.varint(15u, 456),
                UnknownField.fixed32(2u, 0x01020304u),
                UnknownField.varint(1u, 1)
            )

        val writer = ProtoktWriter(ByteArray(unknownFields.size()))
        writer.writeUnknown(unknownFields)

        assertThat(writer.toByteArray()).isEqualTo(
            byteArrayOf(
                0x7a,
                0x03,
                0x61,
                0x62,
                0x63,
                0x78,
                0x7b,
                0x7a,
                0x03,
                0x64,
                0x65,
                0x66,
                0x78,
                0xc8.toByte(),
                0x03,
                0x15,
                0x04,
                0x03,
                0x02,
                0x01,
                0x08,
                0x01
            )
        )
    }

    @Test
    @OptIn(OnlyForUseByGeneratedProtoCode::class)
    fun `lookup retains wire order and typed projections`() {
        val unknownFields =
            unknownFieldSet(
                UnknownField.lengthDelimited(4u, byteArrayOf(1)),
                UnknownField.varint(4u, 2),
                UnknownField.lengthDelimited(4u, byteArrayOf(3))
            )

        assertThat(4u in unknownFields).isTrue()
        assertThat(unknownFields[4u]?.values).containsExactly(
            LengthDelimitedVal(Bytes.from(byteArrayOf(1))),
            VarintVal(2uL),
            LengthDelimitedVal(Bytes.from(byteArrayOf(3)))
        ).inOrder()
        assertThat(unknownFields[4u]?.varint).containsExactly(VarintVal(2uL))
        assertThat(unknownFields[4u]?.lengthDelimited).containsExactly(
            LengthDelimitedVal(Bytes.from(byteArrayOf(1))),
            LengthDelimitedVal(Bytes.from(byteArrayOf(3)))
        ).inOrder()
    }

    @Test
    fun `field equality includes wire order`() {
        val lengthThenVarint =
            unknownFieldSet(
                UnknownField.lengthDelimited(1u, byteArrayOf(1)),
                UnknownField.varint(1u, 1)
            )
        val varintThenLength =
            unknownFieldSet(
                UnknownField.varint(1u, 1),
                UnknownField.lengthDelimited(1u, byteArrayOf(1))
            )

        assertThat(lengthThenVarint).isNotEqualTo(varintThenLength)
    }

    @Test
    fun `set equality does not depend on order across field numbers`() {
        val ascending = unknownFieldSet(UnknownField.varint(1u, 1), UnknownField.varint(2u, 2))
        val descending = unknownFieldSet(UnknownField.varint(2u, 2), UnknownField.varint(1u, 1))

        assertThat(ascending).isEqualTo(descending)
        assertThat(ascending.hashCode()).isEqualTo(descending.hashCode())
    }

    @Test
    fun `public length-delimited factory copies its input`() {
        val source = byteArrayOf(1, 2, 3)
        val unknown = UnknownField.lengthDelimited(1u, source)

        source[0] = 9

        assertThat((unknown.value as LengthDelimitedVal).value.bytes).isEqualTo(byteArrayOf(1, 2, 3))
    }

    @Test
    fun `internal length-delimited factory retains owned bytes`() {
        val bytes = Bytes(byteArrayOf(1, 2, 3))
        val unknown = UnknownField.lengthDelimited(1u, bytes)

        assertThat((unknown.value as LengthDelimitedVal).value).isSameInstanceAs(bytes)
    }

    private fun unknownFieldSet(vararg fields: UnknownField) =
        UnknownFieldSet.Builder().apply { fields.forEach(::add) }.build()
}
