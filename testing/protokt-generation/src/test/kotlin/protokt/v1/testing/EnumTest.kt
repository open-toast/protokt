/*
 * Copyright (c) 2019 Toast, Inc.
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
import org.junit.jupiter.api.Test

class EnumTest {
    @Test
    fun `aliases with the same wire value are equal`() {
        assertThat(SomeEnum.VALUE_ALIAS).isEqualTo(SomeEnum.VALUE)
        assertThat(SomeEnum.VALUE_ALIAS.hashCode()).isEqualTo(SomeEnum.VALUE.hashCode())
    }

    @Test
    fun `known and unrecognized values with the same wire value are unequal`() {
        assertThat(SomeEnum.UNRECOGNIZED(0)).isNotEqualTo(SomeEnum.VALUE)
    }

    @Test
    fun `unrecognized values from the same enum are equal`() {
        val first = SomeEnum.UNRECOGNIZED(10)
        val second = SomeEnum.UNRECOGNIZED(10)

        assertThat(first).isEqualTo(second)
        assertThat(first.hashCode()).isEqualTo(second.hashCode())
    }

    @Test
    fun `equal wire values from different enums are unequal`() {
        assertThat(SomeEnum.VALUE).isNotEqualTo(MoreEnum.MORE_VALUE)
    }

    @Test
    fun `message equality uses alias semantics`() {
        val canonical = HasAnEnum { enum = SomeEnum.VALUE }
        val alias = HasAnEnum { enum = SomeEnum.VALUE_ALIAS }

        assertThat(alias).isEqualTo(canonical)
        assertThat(alias.hashCode()).isEqualTo(canonical.hashCode())
    }

    @Test
    fun `round trip preserves unknown enums`() {
        val with3 = HasMoreEnum { enum = MoreEnum.MORE_VALUE_3 }
        val as2 = HasAnEnum.deserialize(with3.serialize())

        assertThat(as2.enum).isEqualTo(SomeEnum.deserialize(2))

        val as3From2 = HasMoreEnum.deserialize(as2.serialize())

        assertThat(as3From2.enum).isEqualTo(MoreEnum.MORE_VALUE_3)
    }
}
