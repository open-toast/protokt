/*
 * Copyright (c) 2020 Toast Inc.
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

package com.toasttab.protokt.testing.options

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.testing.rt.propertyType
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.reflect.full.createType

class Proto3OptionalWrapperTypeTest {
    @Test
    fun `optional primitive wrapper type should be nullable`() {
        assertThat(
            TestProto3OptionalWrapperType::class
                .propertyType("optionalLocalDate")
        ).isEqualTo(LocalDate::class.createType(nullable = true))
    }

    @Test
    fun `optional primitive wrapper serializes otherwise default value`() {
        val date = LocalDate.now()

        val serialized =
            TestProto3OptionalWrapperType {
                optionalLocalDate = date
            }.serialize()

        val deserialized = TestProto3OptionalWrapperType.deserialize(serialized)

        assertThat(deserialized.optionalLocalDate).isEqualTo(date)
    }

    @Test
    fun `optional primitive wrapper doesn't serialize default values for nulls`() {
        val serialized = TestProto3OptionalWrapperType { }.serialize()

        val deserialized = TestProto3OptionalWrapperType.deserialize(serialized)

        assertThat(deserialized.optionalLocalDate).isNull()
    }
}
