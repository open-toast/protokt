/*
 * Copyright (c) 2024 Toast, Inc.
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

package protokt.v1.buf.validate

import buf.validate.conformance.cases.Bytes
import buf.validate.conformance.cases.Numbers
import buf.validate.conformance.cases.Repeated
import buf.validate.conformance.cases.Strings
import build.buf.protovalidate.ValidationResult
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.ByteString
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import org.junit.jupiter.api.Test
import protokt.v1.AbstractDeserializer
import protokt.v1.AbstractMessage
import protokt.v1.GeneratedMessage
import protokt.v1.Message
import protokt.v1.Reader
import protokt.v1.UnknownFieldSet
import protokt.v1.Writer
import protokt.v1.buf.validate.conformance.cases.MessageRequiredOneof
import protokt.v1.buf.validate.conformance.cases.Oneof
import protokt.v1.buf.validate.conformance.cases.TestMsg
import protokt.v1.buf.validate.conformance.cases.UInt64In
import protokt.v1.buf.validate.conformance.cases.bytes_file_descriptor
import protokt.v1.buf.validate.conformance.cases.messages_file_descriptor
import protokt.v1.buf.validate.conformance.cases.numbers_file_descriptor
import protokt.v1.buf.validate.conformance.cases.oneofs_file_descriptor
import protokt.v1.buf.validate.conformance.cases.repeated_file_descriptor
import protokt.v1.buf.validate.conformance.cases.strings_file_descriptor
import protokt.v1.google.protobuf.FileDescriptor

abstract class AbstractProtoktValidatorTest {
    protected val validator = ProtoktValidator()

    abstract fun validate(message: Message): ValidationResult

    private fun load(descriptor: FileDescriptor) {
        descriptor
            .toProtobufJavaDescriptor()
            .messageTypes
            .forEach {
                runCatching { validator.load(it) }
            }
    }

    private fun FileDescriptor.toProtobufJavaDescriptor(): Descriptors.FileDescriptor =
        Descriptors.FileDescriptor.buildFrom(
            DescriptorProtos.FileDescriptorProto.parseFrom(proto.serialize()),
            dependencies.map { it.toProtobufJavaDescriptor() }.toTypedArray(),
            true
        )

    @Test
    fun `test required oneof constraint`() {
        load(messages_file_descriptor.descriptor)

        val result =
            validate(
                MessageRequiredOneof {
                    one =
                        MessageRequiredOneof.One.Val(
                            TestMsg {
                                const = "foo"
                            }
                        )
                }
            )

        assertThat(result.violations).isEmpty()
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `test oneof constraint`() {
        load(oneofs_file_descriptor.descriptor)

        val result =
            validate(
                Oneof {
                    o = Oneof.O.X("foobar")
                }
            )

        assertThat(result.violations).isEmpty()
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `test uint64 in constraint`() {
        load(numbers_file_descriptor.descriptor)

        val result =
            validate(
                UInt64In {
                    `val` = 4u
                }
            )

        assertThat(result.isSuccess).isFalse()
    }

    @Test
    fun `test message with varint non-uint64 encoded purely as unknown fields (dynamic message without a dedicated type)`() {
        load(numbers_file_descriptor.descriptor)

        val result =
            validate(
                Int64.deserialize(
                    Numbers.Int64In.newBuilder()
                        .setVal(4)
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result.isSuccess).isFalse()

        val result2 =
            validate(
                Int64.deserialize(
                    Numbers.Int64In.newBuilder()
                        .setVal(3)
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result2.isSuccess).isTrue()
    }

    @Test
    fun `test message with varint uint64 encoded purely as unknown fields (dynamic message without a dedicated type)`() {
        load(numbers_file_descriptor.descriptor)

        val result =
            validate(
                UInt64.deserialize(
                    Numbers.UInt64In.newBuilder()
                        .setVal(4)
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result.isSuccess).isFalse()

        val result2 =
            validate(
                UInt64.deserialize(
                    Numbers.UInt64In.newBuilder()
                        .setVal(3)
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result2.isSuccess).isTrue()
    }

    @Test
    fun `test message with fixed32 encoded purely as unknown fields (dynamic message without a dedicated type)`() {
        load(numbers_file_descriptor.descriptor)

        val result =
            validate(
                Fixed32.deserialize(
                    Numbers.Fixed32In.newBuilder()
                        .setVal(4)
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result.isSuccess).isFalse()

        val result2 =
            validate(
                Fixed32.deserialize(
                    Numbers.Fixed32In.newBuilder()
                        .setVal(3)
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result2.isSuccess).isTrue()
    }

    @Test
    fun `test message with fixed64 encoded purely as unknown fields (dynamic message without a dedicated type)`() {
        load(numbers_file_descriptor.descriptor)

        val result =
            validate(
                Fixed64.deserialize(
                    Numbers.Fixed64In.newBuilder()
                        .setVal(4)
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result.isSuccess).isFalse()

        val result2 =
            validate(
                Fixed64.deserialize(
                    Numbers.Fixed64In.newBuilder()
                        .setVal(3)
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result2.isSuccess).isTrue()
    }

    @Test
    fun `test message with length delimited string encoded purely as unknown fields (dynamic message without a dedicated type)`() {
        load(strings_file_descriptor.descriptor)

        val result =
            validate(
                LengthDelimitedString.deserialize(
                    Strings.StringIn.newBuilder()
                        .setVal("foo")
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result.isSuccess).isFalse()

        val result2 =
            validate(
                LengthDelimitedString.deserialize(
                    Strings.StringIn.newBuilder()
                        .setVal("bar")
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result2.isSuccess).isTrue()
    }

    @Test
    fun `test message with length delimited bytes encoded purely as unknown fields (dynamic message without a dedicated type)`() {
        load(bytes_file_descriptor.descriptor)

        val result =
            validate(
                LengthDelimitedBytes.deserialize(
                    Bytes.BytesIn.newBuilder()
                        .setVal(ByteString.copyFromUtf8("foo"))
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result.isSuccess).isFalse()

        val result2 =
            validate(
                LengthDelimitedBytes.deserialize(
                    Bytes.BytesIn.newBuilder()
                        .setVal(ByteString.copyFromUtf8("bar"))
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result2.isSuccess).isTrue()
    }

    @Test
    fun `test message with repeated values encoded purely as unknown fields (dynamic message without a dedicated type)`() {
        load(repeated_file_descriptor.descriptor)

        val result =
            validate(
                RepeatedLengthDelimited.deserialize(
                    Repeated.RepeatedUnique.newBuilder()
                        .addAllVal(listOf("foo", "foo"))
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result.isSuccess).isFalse()

        val result2 =
            validate(
                RepeatedLengthDelimited.deserialize(
                    Repeated.RepeatedUnique.newBuilder()
                        .addAllVal(listOf("foo", "bar"))
                        .build()
                        .toByteArray()
                )
            )

        assertThat(result2.isSuccess).isTrue()
    }

    abstract class AbstractDynamicMessage : AbstractMessage() {
        abstract val unknownFields: UnknownFieldSet

        override fun messageSize() =
            unknownFields.size()

        override fun serialize(writer: Writer) {
            writer.writeUnknown(unknownFields)
        }
    }

    @GeneratedMessage("buf.validate.conformance.cases.Int64In")
    class Int64(
        override val unknownFields: UnknownFieldSet,
    ) : AbstractDynamicMessage() {
        companion object : AbstractDeserializer<Int64>() {
            @JvmStatic
            override fun deserialize(reader: Reader): Int64 {
                val unknownFields = UnknownFieldSet.Builder()

                while (true) {
                    when (reader.readTag()) {
                        0u -> return Int64(UnknownFieldSet.from(unknownFields))
                        else -> unknownFields.add(reader.readUnknown())
                    }
                }
            }
        }
    }

    @GeneratedMessage("buf.validate.conformance.cases.UInt64In")
    class UInt64(
        override val unknownFields: UnknownFieldSet,
    ) : AbstractDynamicMessage() {
        companion object : AbstractDeserializer<UInt64>() {
            @JvmStatic
            override fun deserialize(reader: Reader): UInt64 {
                val unknownFields = UnknownFieldSet.Builder()

                while (true) {
                    when (reader.readTag()) {
                        0u -> return UInt64(UnknownFieldSet.from(unknownFields))
                        else -> unknownFields.add(reader.readUnknown())
                    }
                }
            }
        }
    }

    @GeneratedMessage("buf.validate.conformance.cases.Fixed32In")
    class Fixed32(
        override val unknownFields: UnknownFieldSet,
    ) : AbstractDynamicMessage() {
        companion object : AbstractDeserializer<Fixed32>() {
            @JvmStatic
            override fun deserialize(reader: Reader): Fixed32 {
                val unknownFields = UnknownFieldSet.Builder()

                while (true) {
                    when (reader.readTag()) {
                        0u -> return Fixed32(UnknownFieldSet.from(unknownFields))
                        else -> unknownFields.add(reader.readUnknown())
                    }
                }
            }
        }
    }

    @GeneratedMessage("buf.validate.conformance.cases.Fixed64In")
    class Fixed64(
        override val unknownFields: UnknownFieldSet,
    ) : AbstractDynamicMessage() {
        companion object : AbstractDeserializer<Fixed64>() {
            @JvmStatic
            override fun deserialize(reader: Reader): Fixed64 {
                val unknownFields = UnknownFieldSet.Builder()

                while (true) {
                    when (reader.readTag()) {
                        0u -> return Fixed64(UnknownFieldSet.from(unknownFields))
                        else -> unknownFields.add(reader.readUnknown())
                    }
                }
            }
        }
    }

    @GeneratedMessage("buf.validate.conformance.cases.StringIn")
    class LengthDelimitedString(
        override val unknownFields: UnknownFieldSet,
    ) : AbstractDynamicMessage() {
        companion object : AbstractDeserializer<LengthDelimitedString>() {
            @JvmStatic
            override fun deserialize(reader: Reader): LengthDelimitedString {
                val unknownFields = UnknownFieldSet.Builder()

                while (true) {
                    when (reader.readTag()) {
                        0u -> return LengthDelimitedString(UnknownFieldSet.from(unknownFields))
                        else -> unknownFields.add(reader.readUnknown())
                    }
                }
            }
        }
    }

    @GeneratedMessage("buf.validate.conformance.cases.BytesIn")
    class LengthDelimitedBytes(
        override val unknownFields: UnknownFieldSet,
    ) : AbstractDynamicMessage() {
        companion object : AbstractDeserializer<LengthDelimitedBytes>() {
            @JvmStatic
            override fun deserialize(reader: Reader): LengthDelimitedBytes {
                val unknownFields = UnknownFieldSet.Builder()

                while (true) {
                    when (reader.readTag()) {
                        0u -> return LengthDelimitedBytes(UnknownFieldSet.from(unknownFields))
                        else -> unknownFields.add(reader.readUnknown())
                    }
                }
            }
        }
    }

    @GeneratedMessage("buf.validate.conformance.cases.RepeatedUnique")
    class RepeatedLengthDelimited(
        override val unknownFields: UnknownFieldSet,
    ) : AbstractDynamicMessage() {
        companion object : AbstractDeserializer<RepeatedLengthDelimited>() {
            @JvmStatic
            override fun deserialize(reader: Reader): RepeatedLengthDelimited {
                val unknownFields = UnknownFieldSet.Builder()

                while (true) {
                    when (reader.readTag()) {
                        0u -> return RepeatedLengthDelimited(UnknownFieldSet.from(unknownFields))
                        else -> unknownFields.add(reader.readUnknown())
                    }
                }
            }
        }
    }
}
