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
import org.junit.jupiter.params.provider.ValueSource
import proto3_unittest.UnittestProto3
import protokt.v1.google.protobuf.getField
import protokt.v1.google.protobuf.hasField
import protokt.v1.proto3_unittest.TestAllTypes
import protokt.v1.proto3_unittest.TestOneof2

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
    fun `default enum field is absent like protobuf java`() {
        val field = UnittestProto3.TestAllTypes.getDescriptor().findFieldByName("optional_nested_enum")

        assertThat(UnittestProto3.TestAllTypes.getDefaultInstance().hasField(field)).isFalse()
        assertThat(TestAllTypes {}.hasField(field)).isFalse()
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 123])
    fun `implicit enum presence matches protobuf java after decoding`(number: Int) {
        val field = UnittestProto3.TestAllTypes.getDescriptor().findFieldByName("optional_nested_enum")
        val javaMessage = UnittestProto3.TestAllTypes.newBuilder().setOptionalNestedEnumValue(number).build()
        val message = TestAllTypes.deserialize(javaMessage.toByteArray())

        assertThat(message.hasField(field)).isEqualTo(javaMessage.hasField(field))
    }

    @Test
    fun `oneof enum presence includes the zero value`() {
        val field = UnittestProto3.TestOneof2.getDescriptor().findFieldByName("foo_enum")
        val javaMessage = UnittestProto3.TestOneof2.newBuilder().setFooEnumValue(0).build()
        val message = TestOneof2.deserialize(javaMessage.toByteArray())

        assertThat(javaMessage.hasField(field)).isTrue()
        assertThat(message.hasField(field)).isTrue()
        assertThat(TestOneof2 {}.hasField(field)).isFalse()
    }

    @Test
    fun `optional enum presence includes the zero value`() {
        val javaMessage =
            com.toasttab.protokt.v1.testing.TestOuterClass.EnumPresence.newBuilder()
                .setChoiceValue(0)
                .build()
        val field = javaMessage.descriptorForType.findFieldByName("choice")
        val message = EnumPresence.deserialize(javaMessage.toByteArray())

        assertThat(javaMessage.hasField(field)).isTrue()
        assertThat(message.hasField(field)).isTrue()
        assertThat(EnumPresence {}.hasField(field)).isFalse()
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
