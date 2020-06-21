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
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WrapperTypesTest {
    private val model =
        Wrappers {
            id = Id("asdf")
            ipAddress = InetAddress.getByAddress(byteArrayOf(0, 0, 0, 1))
            uuid = UUID.randomUUID()
            cachingId = CachingId("asdf".toByteArray())
            socketAddress = InetSocketAddress(InetAddress.getLocalHost(), 8080)
            instant = Instant.now()
            duration = Duration.ofSeconds(5)
        }

    @Test
    fun `round trip should preserve model`() {
        assertThat(Wrappers.deserialize(model.serialize())).isEqualTo(model)
    }

    @Test
    fun `round trip should preserve generic wrapper`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.id).isEqualTo(model.id)
    }

    @Test
    fun `round trip should preserve ip address`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.ipAddress).isEqualTo(model.ipAddress)
    }

    @Test
    fun `round trip should preserve uuid`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.uuid).isEqualTo(model.uuid)
    }

    @Test
    fun `round trip should preserve socket address`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.socketAddress).isEqualTo(model.socketAddress)
    }

    @Test
    fun `round trip should preserve instant`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.instant).isEqualTo(model.instant)
    }

    @Test
    fun `round trip should preserve duration`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.duration).isEqualTo(model.duration)
    }

    @Test
    fun `round trip should preserve generic wrapper oneOf`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = OneofWrappers.WrappedOneof.IdOneof(model.id)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneofWrappers.WrappedOneof.IdOneof).idOneof
        ).isEqualTo(model.id)
    }

    @Test
    fun `round trip should preserve uuid oneOf`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = OneofWrappers.WrappedOneof.UuidOneof(model.uuid)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneofWrappers.WrappedOneof.UuidOneof).uuidOneof
        ).isEqualTo(model.uuid)
    }

    @Test
    fun `round trip should preserve ip address oneOf`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = OneofWrappers.WrappedOneof.IpAddressOneof(model.ipAddress)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneofWrappers.WrappedOneof.IpAddressOneof).ipAddressOneof
        ).isEqualTo(model.ipAddress)
    }

    @Test
    fun `round trip should preserve instant oneOf`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = OneofWrappers.WrappedOneof.InstantOneof(model.instant)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneofWrappers.WrappedOneof.InstantOneof).instantOneof
        ).isEqualTo(model.instant)
    }

    @Test
    fun `round trip should preserve socket address oneOf`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = OneofWrappers.WrappedOneof.SocketAddressOneof(model.socketAddress)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneofWrappers.WrappedOneof.SocketAddressOneof).socketAddressOneof
        ).isEqualTo(model.socketAddress)
    }

    @Test
    fun `wrapped primitive should not be nullable`() {
        val thrown = assertThrows<IllegalArgumentException> {
            model.copy { id = null }
        }

        assertThat(thrown).hasMessageThat()
            .isEqualTo("id is a wrapped primitive, was not specified, and has no default value")
    }

    @Test
    fun `wrapped message should not be nullable`() {
        val thrown = assertThrows<IllegalArgumentException> {
            model.copy { instant = null }
        }

        assertThat(thrown).hasMessageThat()
            .isEqualTo("instant specified nonnull with (protokt.property).non_null but was null")
    }

    @Test
    fun `round trip preserves repeated wrapped types`() {
        val list = listOf(UUID.randomUUID())

        val obj =
            RepeatedWrappers {
                uuids = list
                uuidsWrapped = list
            }

        assertThat(obj.uuids).isEqualTo(list)
        assertThat(obj.uuidsWrapped).isEqualTo(list)

        assertThat(RepeatedWrappers.deserialize(obj.serialize()))
            .isEqualTo(obj)
    }

    @Test
    fun `round trip preserves map wrapped types`() {
        val map = mapOf(StringBox("some-string") to UUID.randomUUID())

        val obj =
            MapWrappers {
                mapStringUuid = map
            }

        assertThat(obj.mapStringUuid).isEqualTo(map)

        assertThat(MapWrappers.deserialize(obj.serialize()))
            .isEqualTo(obj)
    }
}
