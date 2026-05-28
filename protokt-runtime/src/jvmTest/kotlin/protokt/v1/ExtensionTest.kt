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

class ExtensionTest {
    @Test
    fun `decode singular varint int32`() {
        val field = fieldOf(VarintVal(42uL))
        assertThat(ExtensionCodecs.int32.decodeSingular(field)).isEqualTo(42)
    }

    @Test
    fun `decode singular varint int64`() {
        val field = fieldOf(VarintVal(Long.MAX_VALUE.toULong()))
        assertThat(ExtensionCodecs.int64.decodeSingular(field)).isEqualTo(Long.MAX_VALUE)
    }

    @Test
    fun `decode singular varint uint32`() {
        val field = fieldOf(VarintVal(UInt.MAX_VALUE.toULong()))
        assertThat(ExtensionCodecs.uint32.decodeSingular(field)).isEqualTo(UInt.MAX_VALUE)
    }

    @Test
    fun `decode singular varint uint64`() {
        val field = fieldOf(VarintVal(ULong.MAX_VALUE))
        assertThat(ExtensionCodecs.uint64.decodeSingular(field)).isEqualTo(ULong.MAX_VALUE)
    }

    @Test
    fun `decode singular varint sint32`() {
        val encoded = ((-1 shl 1) xor (-1 shr 31)).toUInt().toULong()
        val field = fieldOf(VarintVal(encoded))
        assertThat(ExtensionCodecs.sint32.decodeSingular(field)).isEqualTo(-1)
    }

    @Test
    fun `decode singular varint sint64`() {
        val encoded = ((-1L shl 1) xor (-1L shr 63)).toULong()
        val field = fieldOf(VarintVal(encoded))
        assertThat(ExtensionCodecs.sint64.decodeSingular(field)).isEqualTo(-1L)
    }

    @Test
    fun `decode singular varint bool`() {
        assertThat(ExtensionCodecs.bool.decodeSingular(fieldOf(VarintVal(1uL)))).isTrue()
        assertThat(ExtensionCodecs.bool.decodeSingular(fieldOf(VarintVal(0uL)))).isFalse()
    }

    @Test
    fun `decode singular fixed32`() {
        val field = fieldOf(Fixed32Val(0xDEADBEEFu))
        assertThat(ExtensionCodecs.fixed32.decodeSingular(field)).isEqualTo(0xDEADBEEFu)
    }

    @Test
    fun `decode singular sfixed32`() {
        val field = fieldOf(Fixed32Val((-1).toUInt()))
        assertThat(ExtensionCodecs.sfixed32.decodeSingular(field)).isEqualTo(-1)
    }

    @Test
    fun `decode singular float`() {
        val field = fieldOf(Fixed32Val(1.5f.toRawBits().toUInt()))
        assertThat(ExtensionCodecs.float.decodeSingular(field)).isEqualTo(1.5f)
    }

    @Test
    fun `decode singular fixed64`() {
        val field = fieldOf(Fixed64Val(ULong.MAX_VALUE))
        assertThat(ExtensionCodecs.fixed64.decodeSingular(field)).isEqualTo(ULong.MAX_VALUE)
    }

    @Test
    fun `decode singular sfixed64`() {
        val field = fieldOf(Fixed64Val((-1L).toULong()))
        assertThat(ExtensionCodecs.sfixed64.decodeSingular(field)).isEqualTo(-1L)
    }

    @Test
    fun `decode singular double`() {
        val field = fieldOf(Fixed64Val(3.14.toRawBits().toULong()))
        assertThat(ExtensionCodecs.double.decodeSingular(field)).isEqualTo(3.14)
    }

    @Test
    fun `decode singular string`() {
        val field = fieldOf(LengthDelimitedVal(Bytes("hello".encodeToByteArray())))
        assertThat(ExtensionCodecs.string.decodeSingular(field)).isEqualTo("hello")
    }

    @Test
    fun `decode singular bytes`() {
        val data = byteArrayOf(1, 2, 3)
        val field = fieldOf(LengthDelimitedVal(Bytes(data)))
        assertThat(ExtensionCodecs.bytes.decodeSingular(field)?.bytes).isEqualTo(data)
    }

    @Test
    fun `decode repeated varints`() {
        val field = fieldOf(VarintVal(1uL), VarintVal(2uL), VarintVal(3uL))
        assertThat(ExtensionCodecs.int32.decodeRepeated(field)).containsExactly(1, 2, 3).inOrder()
    }

    @Test
    fun `decode repeated fixed32`() {
        val field = fieldOf(Fixed32Val(10u), Fixed32Val(20u))
        assertThat(ExtensionCodecs.fixed32.decodeRepeated(field)).containsExactly(10u, 20u).inOrder()
    }

    @Test
    fun `decode repeated strings`() {
        val field =
            fieldOf(
                LengthDelimitedVal(Bytes("a".encodeToByteArray())),
                LengthDelimitedVal(Bytes("b".encodeToByteArray()))
            )
        assertThat(ExtensionCodecs.string.decodeRepeated(field)).containsExactly("a", "b").inOrder()
    }

    @Test
    fun `encode round-trip int32`() {
        val encoded = ExtensionCodecs.int32.encode(1u, 42)
        val decoded =
            ExtensionCodecs.int32.decodeSingular(
                buildField(encoded)
            )
        assertThat(decoded).isEqualTo(42)
    }

    @Test
    fun `encode round-trip bool`() {
        val encoded = ExtensionCodecs.bool.encode(1u, true)
        val decoded =
            ExtensionCodecs.bool.decodeSingular(
                buildField(encoded)
            )
        assertThat(decoded).isTrue()
    }

    @Test
    fun `encode round-trip string`() {
        val encoded = ExtensionCodecs.string.encode(1u, "hello")
        val decoded =
            ExtensionCodecs.string.decodeSingular(
                buildField(encoded)
            )
        assertThat(decoded).isEqualTo("hello")
    }

    @Test
    fun `decode singular returns last value for multiple entries`() {
        val field = fieldOf(VarintVal(1uL), VarintVal(2uL), VarintVal(3uL))
        assertThat(ExtensionCodecs.int32.decodeSingular(field)).isEqualTo(3)
    }

    @Test
    fun `decode singular returns null for empty field`() {
        val field = emptyField()
        assertThat(ExtensionCodecs.int32.decodeSingular(field)).isNull()
        assertThat(ExtensionCodecs.string.decodeSingular(field)).isNull()
        assertThat(ExtensionCodecs.fixed32.decodeSingular(field)).isNull()
        assertThat(ExtensionCodecs.fixed64.decodeSingular(field)).isNull()
    }

    @Test
    fun `operator get on message`() {
        val ext = Extension<TestMsg, Int>(1u, ExtensionCodecs.int32)
        val msg = TestMsg(VarintVal(42uL))
        assertThat(msg[ext]).isEqualTo(42)
    }

    @Test
    fun `operator get returns null for missing extension`() {
        val ext = Extension<TestMsg, Int>(999u, ExtensionCodecs.int32)
        val msg = TestMsg(VarintVal(42uL))
        assertThat(msg[ext]).isNull()
    }

    @Test
    fun `repeated operator get on message`() {
        val ext = RepeatedExtension<TestMsg, Int>(1u, ExtensionCodecs.int32)
        val msg =
            TestMsg(
                UnknownFieldSet.Builder().apply {
                    add(UnknownField.varint(1u, 10))
                    add(UnknownField.varint(1u, 20))
                    add(UnknownField.varint(1u, 30))
                }.build()
            )
        assertThat(msg[ext]).containsExactly(10, 20, 30).inOrder()
    }

    @Test
    fun `repeated operator get returns empty list for missing extension`() {
        val ext = RepeatedExtension<TestMsg, Int>(999u, ExtensionCodecs.int32)
        val msg = TestMsg(VarintVal(42uL))
        assertThat(msg[ext]).isEmpty()
    }

    private fun fieldOf(vararg values: UnknownValue): UnknownFieldSet.Field =
        UnknownFieldSet.Field.Builder().apply { values.forEach { add(it) } }.build()

    private fun emptyField(): UnknownFieldSet.Field =
        UnknownFieldSet.Field.Builder().build()

    private fun buildField(unknownField: UnknownField): UnknownFieldSet.Field =
        UnknownFieldSet.Field.Builder().apply { add(unknownField.value) }.build()

    @OptIn(OnlyForUseByGeneratedProtoCode::class)
    private class TestMsg(
        override val unknownFields: UnknownFieldSet
    ) : AbstractMessage() {
        constructor(value: UnknownValue) : this(
            UnknownFieldSet.Builder().apply {
                add(UnknownField.varint(1u, (value as VarintVal).value.toLong()))
            }.build()
        )

        override fun serializedSize() =
            unknownFields.size()
        override fun serialize(writer: Writer) =
            writer.writeUnknown(unknownFields)
    }
}
