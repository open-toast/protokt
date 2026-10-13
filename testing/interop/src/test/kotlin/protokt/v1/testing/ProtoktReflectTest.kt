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

package protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Struct
import com.google.protobuf.Value
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import proto3_unittest.UnittestProto3
import protokt.v1.Bytes
import protokt.v1.ProtoktDecodeException
import protokt.v1.UnknownField
import protokt.v1.UnknownFieldSet
import protokt.v1.google.protobuf.getField
import protokt.v1.google.protobuf.hasField
import protokt.v1.proto3_unittest.TestAllTypes
import protokt.v1.proto3_unittest.TestEmptyMessage

class ProtoktReflectTest {
    private val context = getContextReflectively()

    @ParameterizedTest
    @MethodSource("optionalDescriptors")
    fun `hasField behavior matches for non-optional proto3 scalar fields`(field: FieldDescriptor) {
        val protoktDefault = TestAllTypes {}
        val javaDefault = UnittestProto3.TestAllTypes.getDefaultInstance()

        assertThat(javaDefault.hasField(field)).isFalse()
        assertThat(protoktDefault.hasField(field)).isFalse()

        assertThat(protoktDefault.getField(field)?.let(context::convertValue))
            .isEqualTo(javaDefault.getField(field))
    }

    @Test
    fun `unknown repeated fields combine packed and unpacked values in wire order`() {
        val field = descriptor.findFieldByName("repeated_int32")
        val message =
            unknownMessage(
                UnknownField.varint(field.number.toUInt(), 1),
                UnknownField.fixed32(field.number.toUInt(), 99u),
                UnknownField.lengthDelimited(field.number.toUInt(), byteArrayOf(2, 3)),
                UnknownField.varint(field.number.toUInt(), 4)
            )

        assertThat(message.getField(field)).isEqualTo(listOf(1, 2, 3, 4))
    }

    @Test
    fun `unknown scalar fields use their descriptor encoding`() {
        val field = descriptor.findFieldByName("optional_sint32")
        val message = unknownMessage(UnknownField.varint(field.number.toUInt(), 3))

        assertThat(message.getField(field)).isEqualTo(-2)
    }

    @ParameterizedTest
    @MethodSource("optionalDescriptors")
    fun `unknown scalar values match descriptor types`(field: FieldDescriptor) {
        val value: Any =
            when (field.javaType) {
                FieldDescriptor.JavaType.INT -> -1
                FieldDescriptor.JavaType.LONG -> -1L
                FieldDescriptor.JavaType.FLOAT -> 1.5f
                FieldDescriptor.JavaType.DOUBLE -> 1.5
                FieldDescriptor.JavaType.BOOLEAN -> true
                FieldDescriptor.JavaType.STRING -> "hello"
                FieldDescriptor.JavaType.BYTE_STRING -> ByteString.copyFromUtf8("hello")
                else -> error("Unexpected scalar type ${field.type}")
            }
        val javaMessage = DynamicMessage.newBuilder(descriptor).setField(field, value).build()
        val message = TestEmptyMessage.deserialize(javaMessage.toByteArray())
        val expected =
            when (field.type) {
                FieldDescriptor.Type.UINT32, FieldDescriptor.Type.FIXED32 -> UInt.MAX_VALUE
                FieldDescriptor.Type.UINT64, FieldDescriptor.Type.FIXED64 -> ULong.MAX_VALUE
                FieldDescriptor.Type.BYTES -> Bytes.from("hello".encodeToByteArray())
                else -> value
            }

        assertThat(message.getField(field)).isEqualTo(expected)
    }

    @Test
    fun `unknown singular scalar uses last compatible value`() {
        val field = descriptor.findFieldByName("optional_int32")
        val number = field.number.toUInt()
        val message =
            unknownMessage(
                UnknownField.varint(number, 1),
                UnknownField.varint(number, 2),
                UnknownField.fixed32(number, 99u),
                UnknownField.lengthDelimited(number, byteArrayOf(3))
            )

        assertThat(message.getField(field)).isEqualTo(2)
    }

    @Test
    fun `unknown strings reject malformed UTF-8`() {
        val field = descriptor.findFieldByName("optional_string")
        val message = unknownMessage(UnknownField.lengthDelimited(field.number.toUInt(), byteArrayOf(0x80.toByte())))

        assertThrows<ProtoktDecodeException> { message.getField(field) }
    }

    @Test
    fun `unknown packed fields reject truncated values`() {
        listOf(
            "repeated_int32" to byteArrayOf(0x80.toByte()),
            "repeated_fixed32" to byteArrayOf(1, 2, 3),
            "repeated_fixed64" to byteArrayOf(1, 2, 3, 4, 5, 6, 7),
        ).forEach { (name, payload) ->
            val field = descriptor.findFieldByName(name)
            val message = unknownMessage(UnknownField.lengthDelimited(field.number.toUInt(), payload))

            assertThrows<ProtoktDecodeException> { message.getField(field) }
        }
    }

    @Test
    fun `unknown nested messages use descriptor and last occurrence`() {
        val field = descriptor.findFieldByName("optional_nested_message")
        val number = field.number.toUInt()
        val message =
            unknownMessage(
                UnknownField.lengthDelimited(number, byteArrayOf(8, 1)),
                UnknownField.lengthDelimited(number, byteArrayOf(8, 2)),
                UnknownField.varint(number, 99)
            )
        val nested = message.getField(field) as DynamicMessage

        assertThat(nested.descriptorForType).isEqualTo(field.messageType)
        assertThat(nested.getField(field.messageType.findFieldByName("bb"))).isEqualTo(2)
    }

    @Test
    fun `unknown nested messages normalize decode failures`() {
        val field = descriptor.findFieldByName("optional_nested_message")
        val message = unknownMessage(UnknownField.lengthDelimited(field.number.toUInt(), byteArrayOf(8, 0x80.toByte())))

        val failure = assertThrows<ProtoktDecodeException> { message.getField(field) }
        assertThat(failure).hasCauseThat().isInstanceOf(InvalidProtocolBufferException::class.java)
    }

    @Test
    fun `unknown maps retain last duplicate key and message values`() {
        val field = Struct.getDescriptor().findFieldByName("fields")
        fun entry(value: String) =
            Struct.newBuilder().putFields("key", Value.newBuilder().setStringValue(value).build()).build().toByteArray()

        val message = TestEmptyMessage.deserialize(entry("first") + entry("last"))
        val values = message.getField(field) as Map<*, *>
        val value = values["key"] as DynamicMessage

        assertThat(values.keys).containsExactly("key")
        assertThat(value.getField(Value.getDescriptor().findFieldByName("string_value"))).isEqualTo("last")
        assertThat(TestEmptyMessage {}.getField(field)).isEqualTo(emptyMap<Any, Any>())
    }

    @Test
    fun `unknown enum fields retain their numeric value`() {
        val field = descriptor.findFieldByName("optional_nested_enum")
        val message = unknownMessage(UnknownField.varint(field.number.toUInt(), 123))

        assertThat((message.getField(field) as protokt.v1.Enum).value).isEqualTo(123)
    }

    companion object {
        private val descriptor = UnittestProto3.TestAllTypes.getDescriptor()

        @JvmStatic
        fun optionalDescriptors() =
            listOf(
                descriptor.findFieldByName("optional_int32"),
                descriptor.findFieldByName("optional_int64"),
                descriptor.findFieldByName("optional_uint32"),
                descriptor.findFieldByName("optional_uint64"),
                descriptor.findFieldByName("optional_sint32"),
                descriptor.findFieldByName("optional_sint64"),
                descriptor.findFieldByName("optional_fixed32"),
                descriptor.findFieldByName("optional_fixed64"),
                descriptor.findFieldByName("optional_sfixed32"),
                descriptor.findFieldByName("optional_sfixed64"),
                descriptor.findFieldByName("optional_float"),
                descriptor.findFieldByName("optional_double"),
                descriptor.findFieldByName("optional_bool"),
                descriptor.findFieldByName("optional_string"),
                descriptor.findFieldByName("optional_bytes")
            )
    }
}

private fun unknownMessage(vararg fields: UnknownField) =
    TestEmptyMessage {
        unknownFields =
            UnknownFieldSet.Builder()
                .apply { fields.forEach(::add) }
                .build()
    }
