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

package com.toasttab.protokt.rt.test

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import test.kt.HasAnEnum
import test.kt.HasMoreEnum
import test.kt.MoreEnum
import test.kt.SomeEnum

class EnumTest {
    @Test
    fun `round trip preserves unknown enums`() {
        val with3 = HasMoreEnum(MoreEnum.MORE_VALUE_3)
        val as2 = HasAnEnum.deserialize(with3.serialize())

        assertThat(as2.enum).isEqualTo(SomeEnum.from(2))

        val as3From2 = HasMoreEnum.deserialize(as2.serialize())

        assertThat(as3From2.enum).isEqualTo(MoreEnum.MORE_VALUE_3)
    }

    @Test
    fun `message with an enum can be called with empty constructor`() {
        assertThat(
            HasAnEnum()
        ).isEqualTo(
            HasAnEnum()
        )
    }
}
