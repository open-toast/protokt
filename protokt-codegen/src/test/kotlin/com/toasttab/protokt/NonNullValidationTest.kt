/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.codegen.model.FieldType
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class NonNullValidationTest : AbstractProtoktCodegenTest() {
    @ParameterizedTest
    @MethodSource("fieldTypes")
    fun `field with bad non-null option`(fieldType: String, fieldTypeName: String?) {
        val thrown = assertThrows<IllegalArgumentException> {
            runPlugin("non_null.proto") { replace("REPLACE", fieldType) }
        }

        println("Caught: $thrown")

        assertThat(thrown)
            .hasMessageThat()
            .isEqualTo(
                "(protokt.property).non_null is only applicable to message " +
                    "types and is inapplicable to non-message " +
                    (fieldTypeName ?: fieldType)
            )
    }

    @ParameterizedTest
    @MethodSource("fieldTypesOneof")
    fun `oneof type`(fieldType: String, fieldTypeName: String?) {
        val thrown = assertThrows<IllegalArgumentException> {
            runPlugin("non_null_oneof.proto") { replace("REPLACE", fieldType) }
        }

        println("Caught: $thrown")

        assertThat(thrown)
            .hasMessageThat()
            .isEqualTo(
                "(protokt.property).non_null is only applicable to top level " +
                    "types and is inapplicable to oneof field " +
                    (fieldTypeName ?: fieldType)
            )
    }

    @ParameterizedTest
    @MethodSource("fieldTypesOptional")
    fun `optional field`(fieldType: String, fieldTypeName: String?) {
        val thrown = assertThrows<IllegalArgumentException> {
            runPlugin("non_null_optional.proto") { replace("REPLACE", fieldType) }
        }

        println("Caught: $thrown")

        assertThat(thrown)
            .hasMessageThat()
            .isEqualTo(
                "(protokt.property).non_null is not applicable to optional " +
                    "fields and is inapplicable to optional " +
                    (fieldTypeName ?: fieldType)
            )
    }

    companion object {
        @JvmStatic
        fun fieldTypes() =
            mapToArgs(argLists())

        private fun mapToArgs(list: List<*>) =
            list.map {
                if (it is String) {
                    Arguments.of(it, null)
                } else {
                    Arguments.of(*(it as List<*>).toTypedArray())
                }
            }

        private fun argLists() =
            ineligibleAnonymousTypes().map { it.name.lowercase() } +
                ineligibleAnonymousTypes().map { "repeated ${it.name.lowercase()}" } +
                listOf(
                    listOf("Foo", "enum"),
                    listOf("repeated Foo", "repeated .toasttab.protokt.codegen.testing.TestMessageWithBadNonNullField.Foo"),
                    listOf("repeated Bar", "repeated .toasttab.protokt.codegen.testing.TestMessageWithBadNonNullField.Bar"),
                    listOf("map<int32, Foo>", "map<int32, .toasttab.protokt.codegen.testing.TestMessageWithBadNonNullField.Foo>")
                )

        @JvmStatic
        fun fieldTypesOptional() =
            mapToArgs(argListsOptional())

        private fun argListsOptional() =
            ineligibleAnonymousTypes().map { it.name.lowercase() } +
                listOf(
                    listOf("Foo", ".toasttab.protokt.codegen.testing.TestMessageWithBadNonNullOptionalField.Foo"),
                    listOf("Bar", ".toasttab.protokt.codegen.testing.TestMessageWithBadNonNullOptionalField.Bar"),
                )

        @JvmStatic
        fun fieldTypesOneof() =
            mapToArgs(argListsOneof())

        private fun argListsOneof() =
            ineligibleAnonymousTypes().map { it.name.lowercase() } +
                listOf(
                    listOf("Foo", ".toasttab.protokt.codegen.testing.TestMessageWithBadNonNullOneof.Foo"),
                    listOf("Bar", ".toasttab.protokt.codegen.testing.TestMessageWithBadNonNullOneof.Bar"),
                )

        private fun ineligibleAnonymousTypes() =
            FieldType.values().filterNot { it in setOf(FieldType.MESSAGE, FieldType.ENUM) }
    }
}
