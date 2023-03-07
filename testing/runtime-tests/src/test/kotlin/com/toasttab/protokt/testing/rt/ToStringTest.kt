/*
 * Copyright (c) 2021 Toast Inc.
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

package com.toasttab.protokt.testing.rt

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.rt.UnknownField
import com.toasttab.protokt.rt.UnknownFieldSet
import org.junit.jupiter.api.Test
import toasttab.protokt.testing.rt.Test2

class ToStringTest {
    @Test
    fun `toString prints the correct format for empty message`() {
        assertThat(
            Empty { }.toString()
        ).isEqualTo(
            "Empty()"
        )
    }

    @Test
    fun `toString prints the correct format for empty message with unknown fields`() {
        assertThat(
            Empty {
                unknownFields = UnknownFieldSet.Builder().add(UnknownField.fixed32(5, 10)).build()
            }.toString()
        ).isEqualTo(
            "Empty(unknownFields=UnknownFieldSet(unknownFields={5=Field(varint=[], fixed32=[Fixed32Val(value=Fixed32(value=10))], fixed64=[], lengthDelimited=[])}))"
        )
    }

    @Test
    fun `toString omits unknown fields when empty`() {
        assertThat(
            Test2 { extra = "foo" }.toString()
        ).isEqualTo(
            "Test2(`val`=[], extra=foo)"
        )
    }

    @Test
    fun `toString prints the correct format with non-empty unknown fields`() {
        assertThat(
            Test2 {
                extra = "foo"
                unknownFields = UnknownFieldSet.Builder().add(UnknownField.fixed32(5, 10)).build()
            }.toString()
        ).isEqualTo(
            "Test2(`val`=[], extra=foo, unknownFields=UnknownFieldSet(unknownFields={5=Field(varint=[], fixed32=[Fixed32Val(value=Fixed32(value=10))], fixed64=[], lengthDelimited=[])}))"
        )
    }
}
