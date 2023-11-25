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

package protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import protokt.v1.Bytes
import protokt.v1.testing.OneofWrappers.WrappedOneof
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class WrapperTypesTest {
    private val model =
        Wrappers {
            id = Id("asdf")
            ipAddress = InetAddress.getByAddress(byteArrayOf(0, 0, 0, 1))
            uuid = UUID.randomUUID()
            cachingId = CachingId(Bytes.from("asdf".toByteArray()))
            socketAddress = InetSocketAddress(InetAddress.getLocalHost(), 8080)
            instant = Instant.now()
            duration = Duration.ofSeconds(5)
            localDate = LocalDate.of(1950, 10, 4)
            nullableUuid = UUID.randomUUID()
            nullableLocalDate = LocalDate.of(1950, 10, 4)
            googleDate = LocalDate.of(1950, 10, 4)
            optionalUuid = UUID.randomUUID()
            optionalIpAddress = InetAddress.getByAddress(byteArrayOf(0, 0, 0, 1))
            optionalLocalDate = LocalDate.of(1950, 10, 4)
            nonNullUuid = UUID.randomUUID()
            nonNullIpAddress = InetAddress.getByAddress(byteArrayOf(0, 0, 0, 1))
            nonNullLocalDate = LocalDate.of(1950, 10, 4)
        }

    @Test
    fun `uuid property is nullable`() {
        assertThat(Wrappers::class.propertyIsMarkedNullable("uuid")).isTrue()
    }

    @Test
    fun `ipAddress property is nullable`() {
        assertThat(Wrappers::class.propertyIsMarkedNullable("ipAddress")).isTrue()
    }

    @Test
    fun `localDate property is nullable`() {
        assertThat(Wrappers::class.propertyIsMarkedNullable("localDate")).isTrue()
    }

    @Test
    fun `nonnull uuid property is not nullable`() {
        assertThat(Wrappers::class.propertyIsMarkedNullable("nonNullUuid")).isFalse()
    }

    @Test
    fun `nonnull ipAddress property is not nullable`() {
        assertThat(Wrappers::class.propertyIsMarkedNullable("nonNullIpAddress")).isFalse()
    }

    @Test
    fun `nonnull localDate property is not nullable`() {
        assertThat(Wrappers::class.propertyIsMarkedNullable("nonNullLocalDate")).isFalse()
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
    fun `round trip should preserve localdate`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.localDate).isEqualTo(model.localDate)
    }

    @Test
    fun `round trip should preserve nullable uuid`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.nullableUuid).isEqualTo(model.nullableUuid)
    }

    @Test
    fun `round trip should preserve nullable uuid when null`() {
        val deserialized =
            Wrappers.deserialize(model.copy { nullableUuid = null }.serialize())

        assertThat(deserialized.nullableUuid).isNull()
    }

    @Test
    fun `round trip should preserve nullable localdate`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.nullableLocalDate).isEqualTo(model.nullableLocalDate)
    }

    @Test
    fun `round trip should preserve nullable localdate when null`() {
        val deserialized =
            Wrappers.deserialize(model.copy { nullableLocalDate = null }.serialize())

        assertThat(deserialized.nullableLocalDate).isNull()
    }

    @Test
    fun `round trip should preserve google localdate`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.googleDate).isEqualTo(model.googleDate)
    }

    @Test
    fun `round trip should preserve optional uuid`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.optionalUuid).isEqualTo(model.optionalUuid)
    }

    @Test
    fun `round trip should preserve optional uuid when null`() {
        val deserialized =
            Wrappers.deserialize(model.copy { optionalUuid = null }.serialize())

        assertThat(deserialized.optionalUuid).isNull()
    }

    @Test
    fun `round trip should preserve optional localdate`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.optionalLocalDate).isEqualTo(model.optionalLocalDate)
    }

    @Test
    fun `round trip should preserve optional localdate when null`() {
        val deserialized =
            Wrappers.deserialize(model.copy { optionalLocalDate = null }.serialize())

        assertThat(deserialized.optionalLocalDate).isNull()
    }

    @Test
    fun `round trip should preserve optional ipaddress`() {
        val deserialized = Wrappers.deserialize(model.serialize())

        assertThat(deserialized.optionalIpAddress).isEqualTo(model.optionalIpAddress)
    }

    @Test
    fun `round trip should preserve optional ipaddress when null`() {
        val deserialized =
            Wrappers.deserialize(model.copy { optionalIpAddress = null }.serialize())

        assertThat(deserialized.optionalIpAddress).isNull()
    }

    @Test
    fun `round trip should preserve id oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.IdOneof(model.id)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.IdOneof).idOneof
        ).isEqualTo(model.id)
    }

    @Test
    fun `round trip should preserve uuid oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.UuidOneof(model.uuid!!)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.UuidOneof).uuidOneof
        ).isEqualTo(model.uuid)
    }

    @Test
    fun `round trip should preserve ip address oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.IpAddressOneof(model.ipAddress!!)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.IpAddressOneof).ipAddressOneof
        ).isEqualTo(model.ipAddress)
    }

    @Test
    fun `round trip should preserve caching id oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.CachingIdOneof(model.cachingId)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.CachingIdOneof).cachingIdOneof
        ).isEqualTo(model.cachingId)
    }

    @Test
    fun `round trip should preserve socket address oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.SocketAddressOneof(model.socketAddress)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.SocketAddressOneof).socketAddressOneof
        ).isEqualTo(model.socketAddress)
    }

    @Test
    fun `round trip should preserve instant oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.InstantOneof(model.instant)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.InstantOneof).instantOneof
        ).isEqualTo(model.instant)
    }

    @Test
    fun `round trip should preserve duration oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.DurationOneof(model.duration)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.DurationOneof).durationOneof
        ).isEqualTo(model.duration)
    }

    @Test
    fun `round trip should preserve localdate oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.LocalDateOneof(model.localDate!!)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.LocalDateOneof).localDateOneof
        ).isEqualTo(model.localDate)
    }

    @Test
    fun `round trip should preserve google localdate oneof`() {
        val deserialized = OneofWrappers.deserialize(
            OneofWrappers {
                wrappedOneof = WrappedOneof.GoogleDateOneof(model.googleDate)
            }.serialize()
        )

        assertThat(
            (deserialized.wrappedOneof as WrappedOneof.GoogleDateOneof).googleDateOneof
        ).isEqualTo(model.googleDate)
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
