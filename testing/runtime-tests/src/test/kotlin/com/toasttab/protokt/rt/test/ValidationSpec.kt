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

package com.toasttab.protokt.rt.test

import com.example.tutorial.AddressBookProtos.AddressBook as JavaAddressBook
import com.example.tutorial.AddressBookProtos.Person as JavaPerson
import com.toasttab.protokt.Timestamp
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import tutorial.AddressBook
import tutorial.Person

class ValidationSpec : StringSpec({
    val timestamp = Timestamp(System.currentTimeMillis() * 1000)
    val phoneNumber = Person.PhoneNumber(
        "617-555-6666",
        Person.PhoneType.WORK
    )
    val phoneNumber2 = Person.PhoneNumber(
        "617-555-6667",
        Person.PhoneType.MOBILE
    )
    val phoneNumber3 = Person.PhoneNumber(
        "617-555-5555",
        Person.PhoneType.HOME
    )
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
        Person.PhoneNumber(
            "781-555-1212",
            Person.PhoneType.WORK),
        Person.PhoneNumber(
            "781-555-5555",
            Person.PhoneType.HOME
        ),
        Person.PhoneNumber(
            "781-555-6666",
            Person.PhoneType.MOBILE
        ))
    val person = Person(
        "Hubert J. Farnsworth",
        0,
        "Farnsworth@toasttab.com",
        phoneListKt,
        null,
        phoneListKt.map { it.number to it.type }.toMap(),
        Person.OneOfTest.LastUpdatedTest(timestamp))
    val person2 = Person(
        "Dr. John A. Zoidberg",
        1,
        "Zoidberg@toasttab.com",
        phoneListKt2,
        null,
        phoneListKt2.map { it.number to it.type }.toMap(),
        Person.OneOfTest.LastUpdatedTest(timestamp))
    val addressBookKt = AddressBook(listOf(person, person2))
    val addressJava = JavaAddressBook
        .newBuilder()
        .addPeople(JavaPerson.parseFrom(person.serialize()))
        .addPeople(JavaPerson.parseFrom(person2.serialize()))
        .build()

    "kotlin to kotlin" {
        val pn = phoneNumber.serialize()
        Person.PhoneNumber
            .deserialize(pn) shouldBe phoneNumber

        val pn2 = person2.serialize()
        Person.deserialize(pn2) shouldBe person2
    }

    "kotlin to kotlin large message" {
        AddressBook
            .deserialize(addressBookKt.serialize()) shouldBe addressBookKt
    }

    "kotlin to java" {
        JavaAddressBook
            .parseFrom(addressBookKt.serialize()) shouldBe addressJava
    }

    "ab java to kotlin" {
        AddressBook
            .deserialize(addressJava.toByteArray()) shouldBe addressBookKt
    }

    "phoneNumber kotlin to kotlin" {
        phoneListKt.forEach {
            Person.PhoneNumber.deserialize(it.serialize()) shouldBe it
        }
    }

    "phoneNumbers kotlin to java" {
        JavaPerson.PhoneNumber
            .parseFrom(phoneNumber.serialize()) shouldBe phoneNumberJava
        JavaPerson.PhoneNumber
            .parseFrom(phoneNumber2.serialize()) shouldBe phoneNumber2Java
        JavaPerson.PhoneNumber
            .parseFrom(phoneNumber3.serialize()) shouldBe phoneNumber3Java
    }

    "phoneNumbers java to kotlin" {
        Person.PhoneNumber
            .deserialize(phoneNumberJava.toByteArray()) shouldBe phoneNumber
        Person.PhoneNumber
            .deserialize(phoneNumber2Java.toByteArray()) shouldBe phoneNumber2
        Person.PhoneNumber
            .deserialize(phoneNumber3Java.toByteArray()) shouldBe phoneNumber3
    }
})
