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
import org.junit.jupiter.api.Test

class Edition2023PresenceTest {
    @Test
    fun `file with implicit presence and no field features has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("foo")).isFalse()
        assertThat(TestFileImplicit {}.foo).isEqualTo(0)

        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("corge")).isFalse()
        assertThat(TestFileImplicit {}.corge).isEqualTo(0)
    }

    @Test
    fun `file with implicit presence and no field features has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("bar")).isTrue()
        assertThat(TestFileImplicit {}.bar).isNull()
    }

    @Test
    fun `file with implicit presence and field with explicit presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("baz")).isTrue()
        assertThat(TestFileImplicit {}.baz).isNull()
    }

    @Test
    fun `file with implicit presence and field with explicit presence has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("qux")).isTrue()
        assertThat(TestFileImplicit {}.qux).isNull()
    }

    @Test
    fun `file with implicit presence and field with legacy required presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("garply")).isFalse()
        assertThat(TestFileImplicit {}.garply).isEqualTo(0)
    }

    @Test
    fun `file with implicit presence and field with legacy required for message type`() {
        // protokt doesn't support non-null message types
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("thud")).isTrue()
        assertThat(TestFileImplicit {}.thud).isNull()
    }

    @Test
    fun `file with explicit presence and no field features has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("foo")).isFalse()
        assertThat(TestFileImplicit {}.foo).isEqualTo(0)
    }

    @Test
    fun `file with explicit presence and no field features has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("bar")).isTrue()
        assertThat(TestFileImplicit {}.bar).isNull()
    }

    @Test
    fun `file with explicit presence and field with explicit presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("baz")).isTrue()
        assertThat(TestFileImplicit {}.baz).isNull()
    }

    @Test
    fun `file with explicit presence and field with explicit presence has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("qux")).isTrue()
        assertThat(TestFileImplicit {}.qux).isNull()
    }

    @Test
    fun `file with explicit presence and field with implicit presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("corge")).isFalse()
        assertThat(TestFileImplicit {}.corge).isEqualTo(0)
    }

    @Test
    fun `file with explicit presence and field with legacy required presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("garply")).isFalse()
        assertThat(TestFileImplicit {}.garply).isEqualTo(0)
    }

    @Test
    fun `file with explicit presence and field with legacy required for message type`() {
        // protokt doesn't support non-null message types
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("thud")).isTrue()
        assertThat(TestFileImplicit {}.thud).isNull()
    }
}