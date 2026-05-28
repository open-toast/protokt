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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@OptIn(OnlyForUseByGeneratedProtoCode::class)
class CodecRoundTripTest {
    @ParameterizedTest
    @MethodSource("codecs")
    fun `round-trip all primitive types`(codec: Codec) {
        val size = 256
        val writer = codec.writer(size)

        // tag 1: int32 (varint)
        writer.writeTag(0x08u)
        writer.write(42)
        // tag 2: int64 (varint)
        writer.writeTag(0x10u)
        writer.write(Long.MAX_VALUE)
        // tag 3: uint32 (varint)
        writer.writeTag(0x18u)
        writer.writeUInt32(300u)
        // tag 4: uint64 (varint)
        writer.writeTag(0x20u)
        writer.writeUInt64(ULong.MAX_VALUE)
        // tag 5: sint32 (varint)
        writer.writeTag(0x28u)
        writer.writeSInt32(-42)
        // tag 6: sint64 (varint)
        writer.writeTag(0x30u)
        writer.writeSInt64(Long.MIN_VALUE)
        // tag 7: bool (varint)
        writer.writeTag(0x38u)
        writer.write(true)
        // tag 8: fixed64 (64-bit)
        writer.writeTag(0x41u)
        writer.writeFixed64(0x0102030405060708uL)
        // tag 9: sfixed64 (64-bit)
        writer.writeTag(0x49u)
        writer.writeSFixed64(-1L)
        // tag 10: double (64-bit)
        writer.writeTag(0x51u)
        writer.write(3.14159)
        // tag 11: fixed32 (32-bit)
        writer.writeTag(0x5du)
        writer.writeFixed32(0xDEADBEEFu)
        // tag 12: sfixed32 (32-bit)
        writer.writeTag(0x65u)
        writer.writeSFixed32(-1)
        // tag 13: float (32-bit)
        writer.writeTag(0x6du)
        writer.write(2.718f)
        // tag 14: string (length-delimited)
        writer.writeTag(0x72u)
        writer.write("hello, world!")
        // tag 15: bytes (length-delimited)
        writer.writeTag(0x7au)
        writer.write(byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte()))

        val bytes = writer.toByteArray()
        val reader = codec.reader(bytes)

        assertThat(reader.readTag()).isEqualTo(0x08u)
        assertThat(reader.readInt32()).isEqualTo(42)
        assertThat(reader.readTag()).isEqualTo(0x10u)
        assertThat(reader.readInt64()).isEqualTo(Long.MAX_VALUE)
        assertThat(reader.readTag()).isEqualTo(0x18u)
        assertThat(reader.readUInt32()).isEqualTo(300u)
        assertThat(reader.readTag()).isEqualTo(0x20u)
        assertThat(reader.readUInt64()).isEqualTo(ULong.MAX_VALUE)
        assertThat(reader.readTag()).isEqualTo(0x28u)
        assertThat(reader.readSInt32()).isEqualTo(-42)
        assertThat(reader.readTag()).isEqualTo(0x30u)
        assertThat(reader.readSInt64()).isEqualTo(Long.MIN_VALUE)
        assertThat(reader.readTag()).isEqualTo(0x38u)
        assertThat(reader.readBool()).isTrue()
        assertThat(reader.readTag()).isEqualTo(0x41u)
        assertThat(reader.readFixed64()).isEqualTo(0x0102030405060708uL)
        assertThat(reader.readTag()).isEqualTo(0x49u)
        assertThat(reader.readSFixed64()).isEqualTo(-1L)
        assertThat(reader.readTag()).isEqualTo(0x51u)
        assertThat(reader.readDouble()).isEqualTo(3.14159)
        assertThat(reader.readTag()).isEqualTo(0x5du)
        assertThat(reader.readFixed32()).isEqualTo(0xDEADBEEFu)
        assertThat(reader.readTag()).isEqualTo(0x65u)
        assertThat(reader.readSFixed32()).isEqualTo(-1)
        assertThat(reader.readTag()).isEqualTo(0x6du)
        assertThat(reader.readFloat()).isEqualTo(2.718f)
        assertThat(reader.readTag()).isEqualTo(0x72u)
        assertThat(reader.readString()).isEqualTo("hello, world!")
        assertThat(reader.readTag()).isEqualTo(0x7au)
        assertThat(reader.readBytes()).isEqualTo(Bytes(byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte())))
        assertThat(reader.readTag()).isEqualTo(0u)
    }

    @ParameterizedTest
    @MethodSource("codecPairs")
    fun `cross-codec byte compatibility`(writerCodec: Codec, readerCodec: Codec) {
        val size = 64
        val writer = writerCodec.writer(size)

        writer.writeTag(0x08u)
        writer.write(-1)
        writer.writeTag(0x15u)
        writer.writeFixed32(42u)
        writer.writeTag(0x1au)
        writer.write("cross-codec")

        val bytes = writer.toByteArray()
        val reader = readerCodec.reader(bytes)

        assertThat(reader.readTag()).isEqualTo(0x08u)
        assertThat(reader.readInt32()).isEqualTo(-1)
        assertThat(reader.readTag()).isEqualTo(0x15u)
        assertThat(reader.readFixed32()).isEqualTo(42u)
        assertThat(reader.readTag()).isEqualTo(0x1au)
        assertThat(reader.readString()).isEqualTo("cross-codec")
        assertThat(reader.readTag()).isEqualTo(0u)
    }

    @ParameterizedTest
    @MethodSource("codecs")
    fun `negative int32 uses 10-byte varint`(codec: Codec) {
        val writer = codec.writer(11)
        writer.writeTag(0x08u)
        writer.write(-1)

        val bytes = writer.toByteArray()
        val reader = codec.reader(bytes)

        assertThat(reader.readTag()).isEqualTo(0x08u)
        assertThat(reader.readInt32()).isEqualTo(-1)
    }

    @ParameterizedTest
    @MethodSource("codecs")
    fun `empty string round-trip`(codec: Codec) {
        val writer = codec.writer(2)
        writer.writeTag(0x0au)
        writer.write("")

        val bytes = writer.toByteArray()
        val reader = codec.reader(bytes)

        assertThat(reader.readTag()).isEqualTo(0x0au)
        assertThat(reader.readString()).isEmpty()
    }

    @ParameterizedTest
    @MethodSource("codecs")
    fun `reader with offset and length`(codec: Codec) {
        val padding = byteArrayOf(0x00, 0x00)
        val writer = codec.writer(16)
        writer.writeTag(0x08u)
        writer.write(99)
        val payload = writer.toByteArray()

        // Embed payload in a larger array with padding
        val wrapped = padding + payload + padding
        val reader = codec.reader(wrapped, padding.size, payload.size)

        assertThat(reader.readTag()).isEqualTo(0x08u)
        assertThat(reader.readInt32()).isEqualTo(99)
        assertThat(reader.readTag()).isEqualTo(0u)
    }

    companion object {
        @JvmStatic
        fun codecs(): List<Codec> =
            listOf(
                ProtoktCodec,
                loadCodec("protokt.v1.ProtobufJavaCodec"),
                loadCodec("protokt.v1.KotlinxIoCodec"),
                loadCodec("protokt.v1.OptimalKmpCodec"),
                loadCodec("protokt.v1.OptimalJvmCodec")
            )

        @JvmStatic
        fun codecPairs(): List<Array<Codec>> =
            codecs().flatMap { writer ->
                codecs().map { reader -> arrayOf(writer, reader) }
            }

        private fun loadCodec(fqcn: String): Codec =
            Class.forName(fqcn).getField("INSTANCE").get(null) as Codec
    }
}
