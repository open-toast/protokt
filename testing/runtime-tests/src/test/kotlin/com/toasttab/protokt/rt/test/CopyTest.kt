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

package com.toasttab.protokt.rt.test

import com.example.tutorial.Person
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CopyTest {
    @Test
    fun `copying an object preserves unmodified fields`() {
        val phoneNumber =
            Person.PhoneNumber {
                number = "617-555-6666"
                type = Person.PhoneType.WORK
            }

        val newNumber = phoneNumber.copy { number = "504-237-4012" }

        assertThat(newNumber)
            .isEqualTo(
                Person.PhoneNumber {
                    number = "504-237-4012"
                    type = Person.PhoneType.WORK
                }
            )
    }
}
