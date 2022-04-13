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
    fun `file with non-null int32 complains`(fieldType: String, fieldTypeName: String?) {
        val thrown = assertThrows<IllegalArgumentException> {
            runPlugin("non_null.proto") { replace("REPLACE", fieldType) }
        }

        println("Caught: $thrown")

        assertThat(thrown)
            .hasMessageThat()
            .isEqualTo(
                "(protokt.property).non_null is only applicable to message " +
                    "types and is incompatible with non-message type " +
                    (fieldTypeName ?: fieldType)
            )
    }

    companion object {
        @JvmStatic
        fun fieldTypes() =
            argLists().map {
                if (it is String) {
                    Arguments.of(it, null)
                } else {
                    Arguments.of(*(it as List<*>).toTypedArray())
                }
            }

        private fun argLists() =
            ineligibleAnonymousTypes().map { it.name.toLowerCase() } +
                ineligibleAnonymousTypes().map { "repeated ${it.name.toLowerCase()}" } +
                listOf(
                    listOf("Foo", "enum"),
                    listOf("repeated Foo", "repeated .toasttab.protokt.codegen.testing.TestMessageWithBadNonNulls.Foo"),
                    listOf("repeated Bar", "repeated .toasttab.protokt.codegen.testing.TestMessageWithBadNonNulls.Bar"),
                    listOf("map<int32, Foo>", "map<int32, .toasttab.protokt.codegen.testing.TestMessageWithBadNonNulls.Foo>")
                )

        private fun ineligibleAnonymousTypes() =
            FieldType.values().filterNot { it in setOf(FieldType.MESSAGE, FieldType.ENUM) }
    }
}
