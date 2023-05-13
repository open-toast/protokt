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

package com.toasttab.protokt.v1.testing.options

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.v1.testing.propertyIsMarkedNullable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import toasttab.protokt.v1.testing.options.NullableWrappersExample
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.UUID

class NullableWrappersExampleTest {
    @Test
    fun `serialization round trip works`() {
        val original = NullableWrappersExample {
            address = InetAddress.getLocalHost()
            socketAddress = InetSocketAddress(InetAddress.getLocalHost(), 8080)
            uuid = UUID.randomUUID()
            instant = Instant.now()
            duration = Duration.ofSeconds(5)
        }

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

    @Test
    fun `type can be constructed with no values`() {
        assertDoesNotThrow {
            NullableWrappersExample {}
        }
    }
}
