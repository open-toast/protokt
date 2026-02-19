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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@OptIn(OnlyForUseByGeneratedProtoCode::class)
class ReaderValidationTest {
    companion object {
        @JvmStatic
        fun codecs(): List<Codec> =
            listOf(ProtobufJavaCodec)

        private val NEGATIVE_ONE_VARINT =
            byteArrayOf(
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x01
            )

        private val INT_MAX_VARINT =
            byteArrayOf(
                0xFF.toByte(),
                0xFF.toByte(),
                0xFF.toByte(),
                0xFF.toByte(),
                0x07
            )
    }

    @Nested
    inner class NegativeLength {
        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `negative string length`(codec: Codec) {
            val bytes = byteArrayOf(0x0a) + NEGATIVE_ONE_VARINT
            val reader = codec.reader(bytes)
            reader.readTag()
            val ex = assertThrows<Exception> { reader.readString() }
            assertThat(ex).hasMessageThat().isEqualTo(WireFormat.NEGATIVE_SIZE)
        }

        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `negative bytes length`(codec: Codec) {
            val bytes = byteArrayOf(0x0a) + NEGATIVE_ONE_VARINT
            val reader = codec.reader(bytes)
            reader.readTag()
            val ex = assertThrows<Exception> { reader.readBytes() }
            assertThat(ex).hasMessageThat().isEqualTo(WireFormat.NEGATIVE_SIZE)
        }

        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `negative message length`(codec: Codec) {
            val bytes = byteArrayOf(0x0a) + NEGATIVE_ONE_VARINT
            val reader = codec.reader(bytes)
            reader.readTag()
            val ex = assertThrows<Exception> { reader.readMessage(EmptyMessage) }
            assertThat(ex).hasMessageThat().isEqualTo(WireFormat.NEGATIVE_SIZE)
        }
    }

    @Nested
    inner class PrimitivePastNestedLimit {
        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `fixed32 past nested limit`(codec: Codec) {
            val bytes = byteArrayOf(
                0x0a, // outer: field 1, LEN
                0x02, // outer: nested message length = 2
                0x0d, // inner: field 1, wire type 5 (FIXED32) - 1 of 2 bytes
                0x01, // inner: 1 byte of data - 2 of 2 bytes
                0x02,
                0x03,
                0x04 // OUTSIDE nested limit
            )
            val reader = codec.reader(bytes)
            reader.readTag()
            val ex = assertThrows<Exception> { reader.readMessage(Fixed32FieldMessage) }
            assertThat(ex).hasMessageThat().isEqualTo(WireFormat.TRUNCATED_MESSAGE)
        }

        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `fixed64 past nested limit`(codec: Codec) {
            val bytes = byteArrayOf(
                0x0a, // outer: field 1, LEN
                0x02, // outer: nested message length = 2
                0x09, // inner: field 1, wire type 1 (FIXED64)
                0x01, // inner: 1 byte of data
                0x02, 0x03, 0x04, 0x05, 0x06, 0x07 // OUTSIDE nested limit
            )
            val reader = codec.reader(bytes)
            reader.readTag()
            val ex = assertThrows<Exception> { reader.readMessage(Fixed64FieldMessage) }
            assertThat(ex).hasMessageThat().isEqualTo(WireFormat.TRUNCATED_MESSAGE)
        }

        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `varint past nested limit`(codec: Codec) {
            val bytes = byteArrayOf(
                0x0a, // outer: field 1, LEN
                0x01, // outer: nested message length = 1
                0x80.toByte(), // inside limit: continuation bit set
                0x01 // OUTSIDE limit: varint terminator
            )
            val reader = codec.reader(bytes)
            reader.readTag()
            val ex = assertThrows<Exception> { reader.readMessage(VarintFieldMessage) }
            assertThat(ex).hasMessageThat().isEqualTo(WireFormat.TRUNCATED_MESSAGE)
        }
    }

    @Nested
    inner class RecursionDepth {
        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `deep recursion rejected`(codec: Codec) {
            val bytes = buildDeeplyNestedMessage(200)
            val reader = codec.reader(bytes)
            val ex =
                assertThrows<Exception> {
                    reader.readTag()
                    reader.readMessage(RecursiveMessage)
                }
            assertThat(ex).hasMessageThat()
                .isEqualTo(WireFormat.TOO_MANY_LEVELS_OF_NESTING)
        }

        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `moderate recursion allowed`(codec: Codec) {
            val bytes = buildDeeplyNestedMessage(50)
            val reader = codec.reader(bytes)
            reader.readTag()
            reader.readMessage(RecursiveMessage)
        }
    }

    @Nested
    inner class NestedExceedsParentLimit {
        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `nested length exceeds parent`(codec: Codec) {
            val bytes = byteArrayOf(
                0x0a, // wrapper: field 1, LEN
                0x04, // wrapper: length = 4
                0x0a, // inner:   field 1, LEN
                0x64, // inner:   length = 100 (way past parent's 2 remaining)
                0x00,
                0x00 // filler
            )
            val reader = codec.reader(bytes)
            reader.readTag()
            val ex = assertThrows<Exception> { reader.readMessage(NestedLenDelimitedMessage) }
            assertThat(ex).hasMessageThat().isEqualTo(WireFormat.TRUNCATED_MESSAGE)
        }
    }

    @Nested
    inner class IntegerOverflow {
        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `message length overflow`(codec: Codec) {
            val bytes = byteArrayOf(0x0a) + INT_MAX_VARINT + byteArrayOf(0x00, 0x00)
            val reader = codec.reader(bytes)
            reader.readTag()
            assertThrows<Exception> { reader.readMessage(EmptyMessage) }
        }

        @ParameterizedTest
        @MethodSource("protokt.v1.ReaderValidationTest#codecs")
        fun `huge string length`(codec: Codec) {
            val bytes = byteArrayOf(0x0a) + INT_MAX_VARINT + byteArrayOf(0x00)
            val reader = codec.reader(bytes)
            reader.readTag()
            val ex = assertThrows<Exception> { reader.readString() }
            assertThat(ex).hasMessageThat().isEqualTo(WireFormat.TRUNCATED_MESSAGE)
        }
    }

    private fun buildDeeplyNestedMessage(depth: Int): ByteArray {
        var inner = byteArrayOf()
        repeat(depth) {
            val lengthVarint = encodeVarint32(inner.size)
            inner = byteArrayOf(0x0a) + lengthVarint + inner
        }
        return inner
    }

    private fun encodeVarint32(value: Int): ByteArray {
        val result = mutableListOf<Byte>()
        var v = value
        while (v and 0x7f.inv() != 0) {
            result.add(((v and 0x7f) or 0x80).toByte())
            v = v ushr 7
        }
        result.add(v.toByte())
        return result.toByteArray()
    }
}

@OptIn(OnlyForUseByGeneratedProtoCode::class)
private object EmptyMessage : AbstractDeserializer<EmptyMessage>(), Message {
    override fun serializedSize() =
        0
    override fun serialize(writer: Writer) {}
    override fun serialize() =
        byteArrayOf()
    override fun deserialize(reader: Reader): EmptyMessage {
        while (true) {
            when (reader.readTag()) {
                0u -> return this
                else -> reader.readUnknown()
            }
        }
    }
}

@OptIn(OnlyForUseByGeneratedProtoCode::class)
private object Fixed32FieldMessage : AbstractDeserializer<Fixed32FieldMessage>(), Message {
    override fun serializedSize() =
        0
    override fun serialize(writer: Writer) {}
    override fun serialize() =
        byteArrayOf()
    override fun deserialize(reader: Reader): Fixed32FieldMessage {
        while (true) {
            when (reader.readTag()) {
                0u -> return this
                0x0du -> reader.readFixed32() // field 1, FIXED32
                else -> reader.readUnknown()
            }
        }
    }
}

@OptIn(OnlyForUseByGeneratedProtoCode::class)
private object Fixed64FieldMessage : AbstractDeserializer<Fixed64FieldMessage>(), Message {
    override fun serializedSize() =
        0
    override fun serialize(writer: Writer) {}
    override fun serialize() =
        byteArrayOf()
    override fun deserialize(reader: Reader): Fixed64FieldMessage {
        while (true) {
            when (reader.readTag()) {
                0u -> return this
                0x09u -> reader.readFixed64() // field 1, FIXED64
                else -> reader.readUnknown()
            }
        }
    }
}

@OptIn(OnlyForUseByGeneratedProtoCode::class)
private object VarintFieldMessage : AbstractDeserializer<VarintFieldMessage>(), Message {
    override fun serializedSize() =
        0
    override fun serialize(writer: Writer) {}
    override fun serialize() =
        byteArrayOf()
    override fun deserialize(reader: Reader): VarintFieldMessage {
        while (true) {
            when (reader.readTag()) {
                0u -> return this
                else -> reader.readUnknown()
            }
        }
    }
}

@OptIn(OnlyForUseByGeneratedProtoCode::class)
private object NestedLenDelimitedMessage : AbstractDeserializer<NestedLenDelimitedMessage>(), Message {
    override fun serializedSize() =
        0
    override fun serialize(writer: Writer) {}
    override fun serialize() =
        byteArrayOf()
    override fun deserialize(reader: Reader): NestedLenDelimitedMessage {
        while (true) {
            when (reader.readTag()) {
                0u -> return this
                0x0au -> reader.readMessage(EmptyMessage) // field 1, LEN
                else -> reader.readUnknown()
            }
        }
    }
}

@OptIn(OnlyForUseByGeneratedProtoCode::class)
private object RecursiveMessage : AbstractDeserializer<RecursiveMessage>(), Message {
    override fun serializedSize() =
        0
    override fun serialize(writer: Writer) {}
    override fun serialize() =
        byteArrayOf()
    override fun deserialize(reader: Reader): RecursiveMessage {
        while (true) {
            when (reader.readTag()) {
                0u -> return this
                0x0au -> reader.readMessage(RecursiveMessage) // field 1, LEN (self)
                else -> reader.readUnknown()
            }
        }
    }
}
