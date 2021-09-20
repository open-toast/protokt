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

package com.toasttab.protokt.testing.pluginoptions

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import toasttab.protokt.testing.lite.LiteEnum
import toasttab.protokt.testing.lite.LiteMessage
import kotlin.reflect.full.declaredMemberProperties

class LiteOptionTest {
    @Test
    fun `descriptor object doesn't exist`() {
        assertThrows<ClassNotFoundException> {
            Class.forName("toasttab.protokt.testing.lite.LiteTest")
        }
    }

    @Test
    fun `enum has no descriptor reference`() {
        assertThat(
            LiteEnum.Deserializer::class.declaredMemberProperties.map { it.name }
        ).doesNotContain("descriptor")
    }

    @Test
    fun `message has no descriptor reference`() {
        assertThat(
            LiteMessage.Deserializer::class.declaredMemberProperties.map { it.name }
        ).doesNotContain("descriptor")
    }

    @Test
    fun `service descriptor doesn't exist`() {
        assertThrows<ClassNotFoundException> {
            Class.forName("toasttab.protokt.testing.lite.LiteService")
        }
    }
}
