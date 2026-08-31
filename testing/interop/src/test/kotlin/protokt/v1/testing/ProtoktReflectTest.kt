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
import com.google.protobuf.Descriptors.FieldDescriptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import proto3_unittest.UnittestProto3
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
