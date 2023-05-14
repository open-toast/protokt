/*
 * Copyright (c) 2022 Toast, Inc.
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

package com.toasttab.protokt.v1

import com.google.common.truth.Truth
import com.toasttab.protokt.v1.codegen.util.FieldType
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class NonNullValidationTest : AbstractProtoktCodegenTest() {
    @ParameterizedTest
    @MethodSource("fieldTypes")
    fun `field with bad non-null option`(fieldType: String, fieldTypeName: String?) {
        assertFailure(
            "non_null.proto",
            fieldType,
            "Error generating code for file test_file.proto: message TestMessageWithBadNonNullField, field value",
            "java.lang.IllegalArgumentException: (protokt.property).non_null is only applicable to message types " +
                "and is inapplicable to non-message " + (fieldTypeName ?: fieldType)
        )
    }

    @ParameterizedTest
    @MethodSource("fieldTypes")
    fun `field in nested message with bad non-null option`(fieldType: String, fieldTypeName: String?) {
        assertFailure(
            "non_null_nested.proto",
            fieldType,
            "Error generating code for file test_file.proto: message Outer.TestNestedMessageWithBadNonNullField, field value",
            "java.lang.IllegalArgumentException: (protokt.property).non_null is only applicable to message types " +
                "and is inapplicable to non-message " + (fieldTypeName ?: fieldType)
        )
    }

    @ParameterizedTest
    @MethodSource("fieldTypesOneof")
    fun `oneof type`(fieldType: String, fieldTypeName: String?) {
        assertFailure(
            "non_null_oneof.proto",
            fieldType,
            "Error generating code for file test_file.proto: message TestMessageWithBadNonNullOneof, field bar",
            "java.lang.IllegalArgumentException: (protokt.property).non_null is only applicable to top level types " +
                "and is inapplicable to oneof field " + (fieldTypeName ?: fieldType)
        )
    }

    @ParameterizedTest
    @MethodSource("fieldTypesOptional")
    fun `optional field`(fieldType: String, fieldTypeName: String?) {
        assertFailure(
            "non_null_optional.proto",
            fieldType,
            "Error generating code for file test_file.proto: message TestMessageWithBadNonNullOptionalField, field value",
            "java.lang.IllegalArgumentException: (protokt.property).non_null is not applicable to optional fields " +
                "and is inapplicable to optional " + (fieldTypeName ?: fieldType)
        )
    }

    private fun assertFailure(
        fileName: String,
        fieldType: String,
        line0: String,
        line1: String
    ) {
        val result = runPlugin(fileName) { replace("REPLACE", fieldType) } as Failure

        println(result.err)

        Truth.assertThat(result.exitCode).isEqualTo(-1)
        Truth.assertThat(result.err.lines()[0]).isEqualTo(line0)
        Truth.assertThat(result.err.lines()[1]).isEqualTo(line1)
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
            ineligibleAnonymousTypes().map { it.simpleName!!.lowercase() } +
                ineligibleAnonymousTypes().map { "repeated ${it.simpleName!!.lowercase()}" } +
                listOf(
                    listOf("Foo", "enum"),
                    listOf("repeated Foo", "repeated .toasttab.protokt.v1.codegen.testing.Foo"),
                    listOf("repeated Bar", "repeated .toasttab.protokt.v1.codegen.testing.Bar"),
                    listOf("map<int32, Foo>", "map<int32, .toasttab.protokt.v1.codegen.testing.Foo>")
                )

        @JvmStatic
        fun fieldTypesOptional() =
            mapToArgs(argListsOptional())

        private fun argListsOptional() =
            ineligibleAnonymousTypes().map { it.simpleName!!.lowercase() } +
                listOf(
                    listOf("Foo", ".toasttab.protokt.v1.codegen.testing.Foo"),
                    listOf("Bar", ".toasttab.protokt.v1.codegen.testing.Bar")
                )

        @JvmStatic
        fun fieldTypesOneof() =
            mapToArgs(argListsOneof())

        private fun argListsOneof() =
            ineligibleAnonymousTypes().map { it.simpleName!!.lowercase() } +
                listOf(
                    listOf("Foo", ".toasttab.protokt.v1.codegen.testing.Foo"),
                    listOf("Bar", ".toasttab.protokt.v1.codegen.testing.Bar")
                )

        private fun ineligibleAnonymousTypes() =
            FieldType::class
                .sealedSubclasses
                .flatMap { it.sealedSubclasses }
                .filterNot { it in setOf(FieldType.Message::class, FieldType.Enum::class) }
    }
}
