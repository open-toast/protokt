/*
 * Copyright (c) 2019. Toast Inc.
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
import com.toasttab.model.CachingId
import com.toasttab.model.Id
import com.toasttab.model.OneOfWrapperModel
import com.toasttab.model.WrapperModel
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test

class WrapperTypesTest {
    private val model =
        WrapperModel(
            id = Id("asdf"),
            ipAddress = InetAddress.getByAddress(byteArrayOf(0, 0, 0, 1)),
            uuid = UUID.randomUUID(),
            cachingId = CachingId("asdf".toByteArray()),
            socketAddress = InetSocketAddress(InetAddress.getLocalHost(), 8080),
            instant = Instant.now(),
            duration = Duration.ofSeconds(5)
        )

    @Test
    fun `round trip should preserve model`() {
        assertThat(WrapperModel.deserialize(model.serialize())).isEqualTo(model)
    }

    @Test
    fun `round trip should preserve generic wrapper`() {
        val deserialized = WrapperModel.deserialize(model.serialize())

        assertThat(deserialized.id).isEqualTo(model.id)
    }

    @Test
    fun `round trip should preserve ip address`() {
        val deserialized = WrapperModel.deserialize(model.serialize())

        assertThat(deserialized.ipAddress).isEqualTo(model.ipAddress)
    }

    @Test
    fun `round trip should preserve uuid`() {
        val deserialized = WrapperModel.deserialize(model.serialize())

        assertThat(deserialized.uuid).isEqualTo(model.uuid)
    }

    @Test
    fun `round trip should preserve socket address`() {
        val deserialized = WrapperModel.deserialize(model.serialize())

        assertThat(deserialized.socketAddress).isEqualTo(model.socketAddress)
    }

    @Test
    fun `round trip should preserve instant`() {
        val deserialized = WrapperModel.deserialize(model.serialize())

        assertThat(deserialized.instant).isEqualTo(model.instant)
    }

    @Test
    fun `round trip should preserve duration`() {
        val deserialized = WrapperModel.deserialize(model.serialize())

        assertThat(deserialized.duration).isEqualTo(model.duration)
    }

    @Test
    fun `round trip should preserve generic wrapper oneOf`() {
        val deserialized = OneOfWrapperModel.deserialize(
            OneOfWrapperModel(
                wrappedOneof = OneOfWrapperModel.WrappedOneof.IdOneof(model.id)
            ).serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneOfWrapperModel.WrappedOneof.IdOneof).idOneof
        ).isEqualTo(model.id)
    }

    @Test
    fun `round trip should preserve uuid oneOf`() {
        val deserialized = OneOfWrapperModel.deserialize(
            OneOfWrapperModel(
                wrappedOneof = OneOfWrapperModel.WrappedOneof.UuidOneof(model.uuid)
            ).serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneOfWrapperModel.WrappedOneof.UuidOneof).uuidOneof
        ).isEqualTo(model.uuid)
    }

    @Test
    fun `round trip should preserve ip address oneOf`() {
        val deserialized = OneOfWrapperModel.deserialize(
            OneOfWrapperModel(
                wrappedOneof = OneOfWrapperModel.WrappedOneof.IpAddressOneof(model.ipAddress)
            ).serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneOfWrapperModel.WrappedOneof.IpAddressOneof).ipAddressOneof
        ).isEqualTo(model.ipAddress)
    }

    @Test
    fun `round trip should preserve instant oneOf`() {
        val deserialized = OneOfWrapperModel.deserialize(
            OneOfWrapperModel(
                wrappedOneof = OneOfWrapperModel.WrappedOneof.InstantOneof(model.instant)
            ).serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneOfWrapperModel.WrappedOneof.InstantOneof).instantOneof
        ).isEqualTo(model.instant)
    }

    @Test
    fun `round trip should preserve socket address oneOf`() {
        val deserialized = OneOfWrapperModel.deserialize(
            OneOfWrapperModel(
                wrappedOneof = OneOfWrapperModel.WrappedOneof.SocketAddressOneof(model.socketAddress)
            ).serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as OneOfWrapperModel.WrappedOneof.SocketAddressOneof).socketAddressOneof
        ).isEqualTo(model.socketAddress)
    }
}
