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

package com.toasttab.protokt.testing.pluginoptions

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.google_protobuf_timestamp
import org.junit.jupiter.api.Test
import toasttab.protokt.testing.ijp.toasttab_testing_ijp_will_us_be_us_imported
import toasttab.protokt.testing.otherijp.toasttab_testing_otherijp_uses_us_import
import tutorial.Person
import tutorial.tutorial_addressbook

class IgnoreJavaPackageTest {
    @Test
    fun `java_package option is ignored when plugin option requests it`() {
        assertThat(Person::class.java.`package`.name)
            .isEqualTo("tutorial")
    }

    @Test
    fun `descriptor dependencies reference correctly qualified entities`() {
        assertThat(
            toasttab_testing_otherijp_uses_us_import.descriptor.dependencies
        ).contains(
            toasttab_testing_ijp_will_us_be_us_imported.descriptor
        )

        assertThat(
            tutorial_addressbook.descriptor.dependencies
        ).contains(
            google_protobuf_timestamp.descriptor
        )
    }
}
