/*
 * Copyright (c) 2023 Toast, Inc.
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

package protokt.v1.google.protobuf

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.KtMessage
import protokt.v1.testing.MapWrappers
import protokt.v1.testing.OneofWrappers
import protokt.v1.testing.RepeatedWrappers
import protokt.v1.testing.Wrappers
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class DynamicMessageTest {
    @Test
    fun `dynamic message serialization`() {
        val message = getAllTypesAllSet()

        verifyMessage(message)
    }

    @Test
    fun `dynamic message with wrapped fields`() {
        val message =
            Wrappers {
                uuid = UUID.randomUUID()
                // ipAddress = InetAddress.getByName("127.0.0.1")
                socketAddress = InetSocketAddress("127.0.0.1", 2319)
                instant = Instant.now()
                duration = Duration.ofSeconds(623, 2319)
                localDate = LocalDate.now()
                nullableUuid = UUID.randomUUID()
                nullableLocalDate = LocalDate.now().minusDays(1)
                optionalUuid = UUID.randomUUID()
                // optionalIpAddress = InetAddress.getByName("127.0.0.2")
                optionalLocalDate = LocalDate.now().minusDays(2)
                optionalString = "foo"
            }

        verifyMessage(message)
    }

    @Test
    fun `dynamic message with oneof fields`() {
        val message1 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.UuidOneof(UUID.randomUUID()) }

        verifyMessage(message1)

        val message2 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.IpAddressOneof(InetAddress.getByName("127.0.0.1")) }

        verifyMessage(message2)

        val message3 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.SocketAddressOneof(InetSocketAddress("127.0.0.1", 2319)) }

        verifyMessage(message3)

        val message4 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.InstantOneof(Instant.now()) }

        verifyMessage(message4)

        val message5 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.DurationOneof(Duration.ofSeconds(623, 14853)) }

        verifyMessage(message5)

        val message6 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.LocalDateOneof(LocalDate.now()) }

        verifyMessage(message6)

        val message7 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.NullableUuidOneof(UUID.randomUUID()) }

        verifyMessage(message7)

        val message8 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.NullableLocalDateOneof(LocalDate.now()) }

        verifyMessage(message8)

        val message9 =
            OneofWrappers { wrappedOneof = OneofWrappers.WrappedOneof.OptionalString("foo") }

        verifyMessage(message9)
    }

    @Test
    fun `dynamic message with repeated fields`() {
        val message =
            RepeatedWrappers {
                uuids = listOf(UUID.randomUUID(), UUID.randomUUID())
                uuidsWrapped = listOf(UUID.randomUUID(), UUID.randomUUID())
                strings = listOf("foo")
            }

        verifyMessage(message)
    }

    @Test
    fun `dynamic message with map fields`() {
        val message =
            MapWrappers {
                mapStringUuid =
                    mapOf(
                        LocalDate.now() to UUID.randomUUID(),
                        LocalDate.now().minusDays(1) to UUID.randomUUID()
                    )

                mapStringSocketAddress =
                    mapOf(
                        LocalDate.now() to InetSocketAddress("127.0.0.1", 2319),
                        LocalDate.now().minusDays(1) to InetSocketAddress("127.0.0.1", 2320)
                    )

                // todo: this needs to wrap properly
                // mapStringStringValue =
                // mapOf("foo" to StringValue { value = "bar" })
            }

        verifyMessage(message)
    }

    private fun verifyMessage(message: KtMessage) {
        val dynamicMessage = message.toDynamicMessage(RuntimeContext.getContextReflectively())

        assertThat(dynamicMessage.serializedSize).isEqualTo(message.messageSize)
        assertThat(dynamicMessage.toByteArray()).isEqualTo(message.serialize())
    }
}
