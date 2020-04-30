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

package com.toasttab.protokt.testing.options

import com.google.common.truth.Truth.assertThat
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import org.junit.jupiter.api.Test

@Suppress("DEPRECATION")
class DeprecatedTest {
    @Test
    fun `deprecated foreign enum`() {
        assertClassDeprecation(
            DeprecatedForeignEnum::class,
            "deprecated in proto"
        )
    }

    @Test
    fun `foreign enum with deprecation`() {
        assertClassDeprecation(
            ForeignEnumWithDeprecation.OPTION_FOUR::class,
            "deprecated in proto"
        )
    }

    @Test
    fun `deprecated message`() {
        assertClassDeprecation(
            DeprecatedModel::class,
            "deprecated in proto"
        )
    }

    @Test
    fun `deprecated message field`() {
        assertFieldDeprecation(
            DeprecatedModel::class,
            "id",
            "mildly deprecated"
        )
    }

    @Test
    fun `deprecated message DSL field`() {
        assertFieldDeprecation(
            DeprecatedModel.DeprecatedModelDsl::class,
            "id",
            "mildly deprecated"
        )
    }

    @Test
    fun `deprecated oneof field`() {
        assertClassDeprecation(
            DeprecatedModel.DeprecatedOneof.Option::class,
            "mildly deprecated"
        )
    }

    @Test
    fun `deprecated enum`() {
        assertClassDeprecation(
            DeprecatedModel.DeprecatedEnum::class,
            "more deprecated"
        )
    }

    @Test
    fun `enum with deprecation`() {
        assertClassDeprecation(
            DeprecatedModel.EnumWithDeprecation.OPTION_TWO::class,
            "mildly deprecated"
        )
    }

    private fun assertFieldDeprecation(
        klass: KClass<*>,
        name: String,
        message: String
    ) {
        assertThat(
            klass
                .declaredMemberProperties.first { it.name == name }
                .annotationClasses
        ).contains(Deprecated::class)

        assertThat(
            klass
                .declaredMemberProperties.first { it.name == name }
                .annotations
                .filterIsInstance<Deprecated>()
                .onlyElement()
                .message
        ).isEqualTo(message)
    }

    private fun assertClassDeprecation(klass: KClass<*>, message: String) {
        assertThat(klass.annotationClasses).contains(Deprecated::class)

        assertThat(
            klass.annotations
                .filterIsInstance<Deprecated>()
                .onlyElement()
                .message
        ).isEqualTo(message)
    }

    private fun <T> Iterable<T>.onlyElement(): T {
        assertThat(this).hasSize(1)
        return first()
    }

    private val KAnnotatedElement.annotationClasses
        get() = annotations.map { it.annotationClass }
}
