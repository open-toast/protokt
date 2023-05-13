/*
 * Copyright (c) 2020 Toast, Inc.
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

package com.toasttab.protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.v1.Bytes
import org.junit.jupiter.api.Test
import toasttab.protokt.v1.testing.WellKnownTypes

class WellKnownTypesTest {
    @Test
    fun `serialization round trip works`() {
        val original =
            WellKnownTypes {
                double = 4.5
                float = 4.6f
                int64 = 23
                uint64 = 231
                int32 = 12
                uint32 = 998
                bool = false
                string = "some-string"
                bytes = Bytes(byteArrayOf(4, 5, 3))
            }

        assertThat(WellKnownTypes.deserialize(original.serialize()))
            .isEqualTo(original)
    }

    @Test
    fun `test declared nullability`() {
        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("double")
        ).isTrue()

        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("float")
        ).isTrue()

        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("int64")
        ).isTrue()

        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("uint64")
        ).isTrue()

        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("int32")
        ).isTrue()

        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("uint32")
        ).isTrue()

        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("bool")
        ).isTrue()

        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("string")
        ).isTrue()

        assertThat(
            WellKnownTypes::class.propertyIsMarkedNullable("bytes")
        ).isTrue()
    }
}
