/*
 * Copyright (c) 2020 Toast, Inc.
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

package com.toasttab.protokt.testing

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.Timestamp
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tutorial.AddressBook
import tutorial.Person
import com.example.tutorial.AddressBookProtos.AddressBook as JavaAddressBook
import com.example.tutorial.AddressBookProtos.Person as JavaPerson
import com.example.tutorial.AddressBookProtos.Person.PhoneNumber as JavaPhoneNumber
import com.google.protobuf.Timestamp as JavaTimestamp

class MessageJavaInteropTest {
    private val timestamp =
        Timestamp { seconds = System.currentTimeMillis() * 1000 }

    private val javaTimestamp =
        JavaTimestamp.newBuilder()
            .setSeconds(timestamp.seconds)
            .build()

    private val phoneNumber0 =
        Person.PhoneNumber {
            number = "617-555-6666"
            type = Person.PhoneType.WORK
        }

    private val javaPhoneNumber0 = JavaPerson.PhoneNumber
        .newBuilder()
        .setNumber("617-555-6666")
        .setType(JavaPerson.PhoneType.WORK)
        .build()

    @Nested
    inner class PhoneNumbers {
        @Test
        fun `kotlin smoke test`() {
            assertThat(Person.PhoneNumber.deserialize(phoneNumber0.serialize()))
                .isEqualTo(phoneNumber0)
        }

        @Test
        fun `java from kotlin`() {
            assertThat(JavaPhoneNumber.parseFrom(phoneNumber0.serialize()))
                .isEqualTo(javaPhoneNumber0)
        }

        @Test
        fun `kotlin from java`() {
            assertThat(Person.PhoneNumber.deserialize(javaPhoneNumber0.toByteArray()))
                .isEqualTo(phoneNumber0)
        }
    }

    @Nested
    inner class Persons {
        private val phoneNumber1 =
            Person.PhoneNumber {
                number = "617-555-6667"
                type = Person.PhoneType.MOBILE
            }

        private val phoneNumber2 =
            Person.PhoneNumber {
                number = "617-555-5555"
                type = Person.PhoneType.HOME
            }

        private val javaPhoneNumber1 = JavaPerson.PhoneNumber
            .newBuilder()
            .setNumber("617-555-6667")
            .setType(JavaPerson.PhoneType.MOBILE)
            .build()

        private val javaPhoneNumber2 = JavaPerson.PhoneNumber
            .newBuilder()
            .setNumber("617-555-5555")
            .setType(JavaPerson.PhoneType.HOME)
            .build()

        private val phones0 = listOf(phoneNumber0, phoneNumber1, phoneNumber2)
        private val javaPhones0 = listOf(javaPhoneNumber0, javaPhoneNumber1, javaPhoneNumber2)

        private val person0 =
            Person {
                name = "Hubert J. Farnsworth"
                id = 0
                email = "Farnsworth@toasttab.com"
                phones = phones0
                numbers = phones0.associate { it.number to it.type }
                oneofTest = Person.OneofTest.LastUpdatedTest(timestamp)
            }

        private val javaPerson0 =
            JavaPerson.newBuilder()
                .setName("Hubert J. Farnsworth")
                .setId(0)
                .setEmail("Farnsworth@toasttab.com")
                .addAllPhones(javaPhones0)
                .putAllNumbers(javaPhones0.associate { it.number to it.type })
                .setLastUpdatedTest(javaTimestamp)
                .build()

        @Test
        fun `kotlin smoke test`() {
            assertThat(Person.deserialize(person0.serialize()))
                .isEqualTo(person0)
        }

        @Test
        fun `java from kotlin`() {
            assertThat(JavaPerson.parseFrom(person0.serialize()))
                .isEqualTo(javaPerson0)
        }

        @Test
        fun `kotlin from java`() {
            assertThat(Person.deserialize(javaPerson0.toByteArray()))
                .isEqualTo(person0)
        }

        @Nested
        inner class AddressBooks {
            private val phones1 = listOf(
                Person.PhoneNumber {
                    number = "781-555-1212"
                    type = Person.PhoneType.WORK
                },
                Person.PhoneNumber {
                    number = "781-555-5555"
                    type = Person.PhoneType.HOME
                },
                Person.PhoneNumber {
                    number = "781-555-6666"
                    type = Person.PhoneType.MOBILE
                }
            )

            private val addressBook =
                AddressBook {
                    people = listOf(
                        person0,
                        Person {
                            name = "Dr. John A. Zoidberg"
                            id = 1
                            email = "Zoidberg@toasttab.com"
                            phones = phones1
                            numbers = phones1.associate { it.number to it.type }
                            oneofTest = Person.OneofTest.LastUpdatedTest(timestamp)
                        }
                    )
                }

            private val javaPhones1 = listOf(
                JavaPerson.PhoneNumber.newBuilder()
                    .setNumber("781-555-1212")
                    .setType(JavaPerson.PhoneType.WORK)
                    .build(),
                JavaPerson.PhoneNumber.newBuilder()
                    .setNumber("781-555-5555")
                    .setType(JavaPerson.PhoneType.HOME)
                    .build(),
                JavaPerson.PhoneNumber.newBuilder()
                    .setNumber("781-555-6666")
                    .setType(JavaPerson.PhoneType.MOBILE)
                    .build()
            )

            private val javaAddressBook =
                JavaAddressBook
                    .newBuilder()
                    .addPeople(javaPerson0)
                    .addPeople(
                        JavaPerson.newBuilder()
                            .setName("Dr. John A. Zoidberg")
                            .setId(1)
                            .setEmail("Zoidberg@toasttab.com")
                            .addAllPhones(javaPhones1)
                            .putAllNumbers(javaPhones1.associate { it.number to it.type })
                            .setLastUpdatedTest(javaTimestamp)
                            .build()
                    )
                    .build()

            @Test
            fun `kotlin smoke test`() {
                assertThat(AddressBook.deserialize(addressBook.serialize()))
                    .isEqualTo(addressBook)
            }

            @Test
            fun `java from kotlin`() {
                assertThat(JavaAddressBook.parseFrom(addressBook.serialize()))
                    .isEqualTo(javaAddressBook)
            }

            @Test
            fun `kotlin from java`() {
                assertThat(AddressBook.deserialize(javaAddressBook.toByteArray()))
                    .isEqualTo(addressBook)
            }
        }
    }
}
