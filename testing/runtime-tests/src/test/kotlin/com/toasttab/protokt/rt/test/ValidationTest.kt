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

package com.toasttab.protokt.rt.test

import com.example.tutorial.AddressBook
import com.example.tutorial.AddressBookProtos.AddressBook as JavaAddressBook
import com.example.tutorial.AddressBookProtos.Person as JavaPerson
import com.example.tutorial.Person
import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.Timestamp
import org.junit.jupiter.api.Test

class ValidationTest {
    val timestamp = Timestamp { seconds = System.currentTimeMillis() * 1000 }
    val phoneNumber =
        Person.PhoneNumber {
            number = "617-555-6666"
            type = Person.PhoneType.WORK
        }
    val phoneNumber2 =
        Person.PhoneNumber {
            number = "617-555-6667"
            type = Person.PhoneType.MOBILE
        }
    val phoneNumber3 =
        Person.PhoneNumber {
            number = "617-555-5555"
            type = Person.PhoneType.HOME
        }
    val phoneNumberJava = JavaPerson.PhoneNumber
        .newBuilder()
        .setNumber("617-555-6666")
        .setType(JavaPerson.PhoneType.WORK)
        .build()
    val phoneNumber2Java = JavaPerson.PhoneNumber
        .newBuilder()
        .setNumber("617-555-6667")
        .setType(JavaPerson.PhoneType.MOBILE)
        .build()
    val phoneNumber3Java = JavaPerson.PhoneNumber
        .newBuilder()
        .setNumber("617-555-5555")
        .setType(JavaPerson.PhoneType.HOME)
        .build()
    val phoneListKt = listOf(phoneNumber, phoneNumber2, phoneNumber3)
    val phoneListKt2 = listOf(
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
    val person =
        Person {
            name = "Hubert J. Farnsworth"
            id = 0
            email = "Farnsworth@toasttab.com"
            phones = phoneListKt
            numbers = phoneListKt.map { it.number to it.type }.toMap()
            oneOfTest = Person.OneOfTest.LastUpdatedTest(timestamp)
        }
    val person2 =
        Person {
            name = "Dr. John A. Zoidberg"
            id = 1
            email = "Zoidberg@toasttab.com"
            phones = phoneListKt2
            numbers = phoneListKt2.map { it.number to it.type }.toMap()
            oneOfTest = Person.OneOfTest.LastUpdatedTest(timestamp)
        }
    val addressBookKt = AddressBook { people = listOf(person, person2) }
    val addressJava = JavaAddressBook
        .newBuilder()
        .addPeople(JavaPerson.parseFrom(person.serialize()))
        .addPeople(JavaPerson.parseFrom(person2.serialize()))
        .build()

    @Test
    fun `kotlin to kotlin`() {
        val pn = phoneNumber.serialize()
        assertThat(Person.PhoneNumber.deserialize(pn)).isEqualTo(phoneNumber)

        val pn2 = person2.serialize()
        assertThat(Person.deserialize(pn2)).isEqualTo(person2)
    }

    @Test
    fun `kotlin to kotlin large message`() {
        assertThat(AddressBook.deserialize(addressBookKt.serialize()))
            .isEqualTo(addressBookKt)
    }

    @Test
    fun `kotlin to java`() {
        assertThat(JavaAddressBook.parseFrom(addressBookKt.serialize()))
            .isEqualTo(addressJava)
    }

    @Test
    fun `ab java to kotlin`() {
        assertThat(AddressBook.deserialize(addressJava.toByteArray()))
            .isEqualTo(addressBookKt)
    }

    @Test
    fun `phoneNumber kotlin to kotlin`() {
        phoneListKt.forEach {
            assertThat(Person.PhoneNumber.deserialize(it.serialize())).isEqualTo(it)
        }
    }

    @Test
    fun `phoneNumbers kotlin to java`() {
        assertThat(JavaPerson.PhoneNumber.parseFrom(phoneNumber.serialize()))
            .isEqualTo(phoneNumberJava)
        assertThat(JavaPerson.PhoneNumber.parseFrom(phoneNumber2.serialize()))
            .isEqualTo(phoneNumber2Java)
        assertThat(JavaPerson.PhoneNumber.parseFrom(phoneNumber3.serialize()))
            .isEqualTo(phoneNumber3Java)
    }

    @Test
    fun `phoneNumbers java to kotlin`() {
        assertThat(Person.PhoneNumber.deserialize(phoneNumberJava.toByteArray()))
            .isEqualTo(phoneNumber)
        assertThat(Person.PhoneNumber.deserialize(phoneNumber2Java.toByteArray()))
            .isEqualTo(phoneNumber2)
        assertThat(Person.PhoneNumber.deserialize(phoneNumber3Java.toByteArray()))
            .isEqualTo(phoneNumber3)
    }
}
