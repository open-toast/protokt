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

package com.toasttab.protokt.codegen.model

import arrow.core.None
import arrow.core.Some
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class PClassTest {
    @Test
    fun `simple class is parsed`() {
        assertThat(PClass.fromName("foo.bar.Foo"))
            .isEqualTo(
                PClass(
                    "Foo",
                    PPackage.fromString("foo.bar"),
                    None
                )
            )
    }

    @Test
    fun `default package class is parsed`() {
        assertThat(PClass.fromName("Foo"))
            .isEqualTo(
                PClass(
                    "Foo",
                    PPackage.DEFAULT,
                    None
                )
            )
    }

    @Test
    fun `nested class is parsed`() {
        assertThat(PClass.fromName("foo.bar.Foo.Bar"))
            .isEqualTo(
                PClass(
                    "Bar",
                    PPackage.fromString("foo.bar"),
                    Some(
                        PClass(
                            "Foo",
                            PPackage.fromString("foo.bar"),
                            None
                        )
                    )
                )
            )
    }

    @Test
    fun `doubly nested class is parsed`() {
        assertThat(PClass.fromName("foo.bar.Foo.Bar.Baz"))
            .isEqualTo(
                PClass(
                    "Baz",
                    PPackage.fromString("foo.bar"),
                    Some(
                        PClass(
                            "Bar",
                            PPackage.fromString("foo.bar"),
                            Some(
                                PClass(
                                    "Foo",
                                    PPackage.fromString("foo.bar"),
                                    None
                                )
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `default package nested class is parsed`() {
        assertThat(PClass.fromName("Foo.Bar"))
            .isEqualTo(
                PClass(
                    "Bar",
                    PPackage.DEFAULT,
                    Some(
                        PClass(
                            "Foo",
                            PPackage.DEFAULT,
                            None
                        )
                    )
                )
            )
    }

    @Test
    fun `default package doubly nested class is parsed`() {
        assertThat(PClass.fromName("Foo.Bar.Baz"))
            .isEqualTo(
                PClass(
                    "Baz",
                    PPackage.DEFAULT,
                    Some(
                        PClass(
                            "Bar",
                            PPackage.DEFAULT,
                            Some(
                                PClass(
                                    "Foo",
                                    PPackage.DEFAULT,
                                    None
                                )
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `simple class qualifiedName`() {
        assertThat(PClass.fromName("foo.bar.Foo").qualifiedName)
            .isEqualTo("foo.bar.Foo")
    }

    @Test
    fun `default package class qualifiedName`() {
        assertThat(PClass.fromName("Foo").qualifiedName)
            .isEqualTo("Foo")
    }

    @Test
    fun `nested class qualifiedName`() {
        assertThat(PClass.fromName("foo.bar.Foo.Bar").qualifiedName)
            .isEqualTo("foo.bar.Foo.Bar")
    }

    @Test
    fun `doubly nested class qualifiedName`() {
        assertThat(PClass.fromName("foo.bar.Foo.Bar.Baz").qualifiedName)
            .isEqualTo("foo.bar.Foo.Bar.Baz")
    }

    @Test
    fun `default package nested class qualifiedName`() {
        assertThat(PClass.fromName("Foo.Bar").qualifiedName)
            .isEqualTo("Foo.Bar")
    }

    @Test
    fun `default package doubly nested class qualifiedName`() {
        assertThat(PClass.fromName("Foo.Bar.Baz").qualifiedName)
            .isEqualTo("Foo.Bar.Baz")
    }

    @Test
    fun `simple class nestedName`() {
        assertThat(PClass.fromName("foo.bar.Foo").nestedName)
            .isEqualTo("Foo")
    }

    @Test
    fun `default package class nestedName`() {
        assertThat(PClass.fromName("Foo").nestedName)
            .isEqualTo("Foo")
    }

    @Test
    fun `nested class nestedName`() {
        assertThat(PClass.fromName("foo.bar.Foo.Bar").nestedName)
            .isEqualTo("Foo.Bar")
    }

    @Test
    fun `doubly nested class nestedName`() {
        assertThat(PClass.fromName("foo.bar.Foo.Bar.Baz").nestedName)
            .isEqualTo("Foo.Bar.Baz")
    }

    @Test
    fun `default package nested class nestedName`() {
        assertThat(PClass.fromName("Foo.Bar").nestedName)
            .isEqualTo("Foo.Bar")
    }

    @Test
    fun `default package doubly nested class nestedName`() {
        assertThat(PClass.fromName("Foo.Bar.Baz").nestedName)
            .isEqualTo("Foo.Bar.Baz")
    }
}
