/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.codegen.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PPackageTest {
    @Test
    fun `basic package can be parsed`() {
        assertThat(PPackage.fromString("foo.bar").toString())
            .isEqualTo("foo.bar")
    }

    @Test
    fun `basic package can be parsed from fully qualified class name`() {
        assertThat(PPackage.fromClassName("foo.bar.Baz"))
            .isEqualTo(PPackage.fromString("foo.bar"))
    }

    @Test
    fun `default package is default`() {
        assertThat(PPackage.DEFAULT.default)
            .isTrue()
    }

    @Test
    fun `default package can be parsed`() {
        assertThat(PPackage.fromString(""))
            .isEqualTo(PPackage.DEFAULT)
    }

    @Test
    fun `default package can be parsed from fully qualified class name`() {
        assertThat(PPackage.fromClassName("Baz"))
            .isEqualTo(PPackage.DEFAULT)
    }

    @Test
    fun `fromClassName throws on name starting with dot`() {
        val thrown = assertThrows<IllegalArgumentException> {
            PPackage.fromClassName(".SomeName")
        }

        assertThat(thrown).hasMessageThat()
            .contains("package with separator in one char")
    }

    @Test
    fun `fromClassName throws on name with no uppercase`() {
        val thrown = assertThrows<IllegalArgumentException> {
            PPackage.fromClassName("somename")
        }

        assertThat(thrown).hasMessageThat()
            .contains("No capital letter")
    }

    @Test
    fun `fromClassName throws on name without dot before name`() {
        val thrown = assertThrows<IllegalArgumentException> {
            PPackage.fromClassName("someName")
        }

        assertThat(thrown).hasMessageThat()
            .contains("Char before first capital letter must be")
    }
}
