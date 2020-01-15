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

package com.toasttab.protokt.options

import com.google.common.truth.Truth.assertThat
import com.toasttab.model.NullableWrappersExample
import com.toasttab.protokt.ext.InetAddressValue
import com.toasttab.protokt.ext.InetSocketAddress
import com.toasttab.protokt.ext.UuidValue
import java.net.InetAddress
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import org.junit.jupiter.api.Test

class NullableWrappersExampleTest {
    @Test
    fun `serialization round trip works`() {
        val original = NullableWrappersExample(
            InetAddressValue(InetAddress.getLocalHost()),
            InetSocketAddress(InetAddress.getLocalHost(), 0),
            UuidValue(UUID.randomUUID()),
            Instant.now(),
            Duration.ofSeconds(5)
        )

        assertThat(NullableWrappersExample.deserialize(original.serialize()))
            .isEqualTo(original)
    }

    @Test
    fun `test declared nullability of java_net_InetAddress`() {
        assertThat(
            NullableWrappersExample::class.propertyIsMarkedNullable("address")
        ).isTrue()
    }

    @Test
    fun `test declared nullability of java_net_InetSocketAddress`() {
        assertThat(
            NullableWrappersExample::class.propertyIsMarkedNullable("socketAddress")
        ).isTrue()
    }

    @Test
    fun `test declared nullability of java_util_UUID`() {
        assertThat(
            NullableWrappersExample::class.propertyIsMarkedNullable("uuid")
        ).isTrue()
    }

    @Test
    fun `test declared nullability of java_time_Instant`() {
        assertThat(
            NullableWrappersExample::class.propertyIsMarkedNullable("instant")
        ).isTrue()
    }

    @Test
    fun `test declared nullability of java_time_Duration`() {
        assertThat(
            NullableWrappersExample::class.propertyIsMarkedNullable("duration")
        ).isTrue()
    }

    private fun KClass<*>.propertyIsMarkedNullable(name: String) =
        declaredMemberProperties
            .first { it.name == name }
            .returnType
            .isMarkedNullable
}
