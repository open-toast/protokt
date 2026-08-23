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
import org.junit.jupiter.api.assertThrows

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
    fun `decode packed varints`() {
        val unsigned =
            fieldOf(
                packedValue(3) {
                    writeUInt64(1uL)
                    writeUInt64(150uL)
                }
            )
        val zigzag =
            fieldOf(
                packedValue(2) {
                    writeUInt64(1uL)
                    writeUInt64(2uL)
                }
            )
        val bool =
            fieldOf(
                packedValue(2) {
                    writeUInt64(0uL)
                    writeUInt64(2uL)
                }
            )

        assertThat(ExtensionCodecs.int32.decodeRepeated(unsigned)).containsExactly(1, 150).inOrder()
        assertThat(ExtensionCodecs.int64.decodeRepeated(unsigned)).containsExactly(1L, 150L).inOrder()
        assertThat(ExtensionCodecs.uint32.decodeRepeated(unsigned)).containsExactly(1u, 150u).inOrder()
        assertThat(ExtensionCodecs.uint64.decodeRepeated(unsigned)).containsExactly(1uL, 150uL).inOrder()
        assertThat(ExtensionCodecs.sint32.decodeRepeated(zigzag)).containsExactly(-1, 1).inOrder()
        assertThat(ExtensionCodecs.sint64.decodeRepeated(zigzag)).containsExactly(-1L, 1L).inOrder()
        assertThat(ExtensionCodecs.bool.decodeRepeated(bool)).containsExactly(false, true).inOrder()
        assertThat(ExtensionCodecs.enum(TestEnumDeserializer).decodeRepeated(unsigned))
            .containsExactly(TestEnum(1), TestEnum(150))
            .inOrder()
    }

    @Test
    fun `decode packed fixed32 values`() {
        val unsigned =
            fieldOf(
                packedValue(8) {
                    writeFixed32(1u)
                    writeFixed32(UInt.MAX_VALUE)
                }
            )
        val float =
            fieldOf(
                packedValue(8) {
                    write(1.5f)
                    write(-0.0f)
                }
            )

        assertThat(ExtensionCodecs.fixed32.decodeRepeated(unsigned)).containsExactly(1u, UInt.MAX_VALUE).inOrder()
        assertThat(ExtensionCodecs.sfixed32.decodeRepeated(unsigned)).containsExactly(1, -1).inOrder()
        assertThat(ExtensionCodecs.float.decodeRepeated(float).map { it.toRawBits() })
            .containsExactly(1.5f.toRawBits(), (-0.0f).toRawBits())
            .inOrder()
    }

    @Test
    fun `decode packed fixed64 values`() {
        val unsigned =
            fieldOf(
                packedValue(16) {
                    writeFixed64(1uL)
                    writeFixed64(ULong.MAX_VALUE)
                }
            )
        val double =
            fieldOf(
                packedValue(16) {
                    write(1.5)
                    write(-0.0)
                }
            )

        assertThat(ExtensionCodecs.fixed64.decodeRepeated(unsigned)).containsExactly(1uL, ULong.MAX_VALUE).inOrder()
        assertThat(ExtensionCodecs.sfixed64.decodeRepeated(unsigned)).containsExactly(1L, -1L).inOrder()
        assertThat(ExtensionCodecs.double.decodeRepeated(double).map { it.toRawBits() })
            .containsExactly(1.5.toRawBits(), (-0.0).toRawBits())
            .inOrder()
    }

    @Test
    fun `mixed packed and unpacked values retain wire order`() {
        val field =
            fieldOf(
                VarintVal(1uL),
                packedValue(2) {
                    writeUInt64(2uL)
                    writeUInt64(3uL)
                },
                VarintVal(4uL)
            )

        assertThat(ExtensionCodecs.int32.decodeRepeated(field)).containsExactly(1, 2, 3, 4).inOrder()
    }

    @Test
    fun `empty packed values make no contribution`() {
        val field =
            fieldOf(
                VarintVal(1uL),
                LengthDelimitedVal(Bytes.empty()),
                VarintVal(2uL)
            )

        assertThat(ExtensionCodecs.int32.decodeRepeated(field)).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `singular scalar extensions ignore packed values`() {
        val field = fieldOf(packedValue(1) { writeUInt64(1uL) })

        assertThat(ExtensionCodecs.int32.decodeSingular(field)).isNull()
    }

    @Test
    fun `malformed packed values throw decode exception`() {
        val malformedVarint = fieldOf(LengthDelimitedVal(Bytes.from(ByteArray(10) { 0x80.toByte() })))
        val truncatedFixed32 = fieldOf(LengthDelimitedVal(Bytes.from(byteArrayOf(1, 2, 3))))
        val truncatedFixed64 = fieldOf(LengthDelimitedVal(Bytes.from(byteArrayOf(1, 2, 3, 4, 5, 6, 7))))

        assertThrows<ProtoktDecodeException> { ExtensionCodecs.int32.decodeRepeated(malformedVarint) }
        assertThrows<ProtoktDecodeException> { ExtensionCodecs.fixed32.decodeRepeated(truncatedFixed32) }
        assertThrows<ProtoktDecodeException> { ExtensionCodecs.fixed64.decodeRepeated(truncatedFixed64) }
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

    private fun packedValue(size: Int, write: Writer.() -> Unit): LengthDelimitedVal {
        val writer = ProtoktWriter(ByteArray(size))
        writer.write()
        return LengthDelimitedVal(Bytes(writer.toByteArray()))
    }

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

private class TestEnum(
    override val value: Int
) : Enum() {
    override val name = "VALUE_$value"
}

private object TestEnumDeserializer : EnumDeserializer<TestEnum> {
    override fun deserialize(value: Int) =
        TestEnum(value)
}
