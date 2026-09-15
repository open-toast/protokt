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
    fun `aliases with the same wire value remain distinct constants`() {
        assertThat(SomeEnum.VALUE_ALIAS).isNotEqualTo(SomeEnum.VALUE)
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
    fun `messages canonicalize enum aliases`() {
        val canonical = HasAnEnum { enum = SomeEnum.VALUE }
        val alias = HasAnEnum { enum = SomeEnum.VALUE_ALIAS }
        val recognizedNumber = HasAnEnum { enum = SomeEnum.UNRECOGNIZED(0) }
        val unknownNumber = HasAnEnum { enum = SomeEnum.UNRECOGNIZED(10) }

        assertThat(alias.enum).isSameInstanceAs(SomeEnum.VALUE)
        assertThat(recognizedNumber.enum).isSameInstanceAs(SomeEnum.VALUE)
        assertThat(alias).isEqualTo(canonical)
        assertThat(recognizedNumber).isEqualTo(canonical)
        assertThat(alias.hashCode()).isEqualTo(canonical.hashCode())
        assertThat(recognizedNumber.hashCode()).isEqualTo(canonical.hashCode())
        assertThat(unknownNumber.enum).isEqualTo(SomeEnum.UNRECOGNIZED(10))
    }

    @Test
    fun `messages canonicalize enum aliases in collections and oneofs`() {
        val message =
            HasEnumCollections {
                enums = listOf(SomeEnum.VALUE_ALIAS)
                enumsByName = mapOf("alias" to SomeEnum.VALUE_ALIAS)
                optionalEnum = SomeEnum.VALUE_ALIAS
                selection = HasEnumCollections.Selection.SelectedEnum(SomeEnum.VALUE_ALIAS)
            }

        assertThat(message.enums.single()).isSameInstanceAs(SomeEnum.VALUE)
        assertThat(message.enumsByName.getValue("alias")).isSameInstanceAs(SomeEnum.VALUE)
        assertThat(message.optionalEnum).isSameInstanceAs(SomeEnum.VALUE)
        assertThat((message.selection as HasEnumCollections.Selection.SelectedEnum).selectedEnum)
            .isSameInstanceAs(SomeEnum.VALUE)
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
