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

package com.toasttab.protokt.v1.testing.pluginoptions

import com.google.common.truth.Truth
import com.toasttab.protokt.v1.TimestampProto
import org.junit.jupiter.api.Test
import toasttab.protokt.v1.testing.ijp.will_be_imported_file_descriptor
import toasttab.protokt.v1.testing.otherijp.uses_import_file_descriptor
import tutorial.AddressBookProtos
import tutorial.Person

class IgnoreJavaPackageTest {
    @Test
    fun `java_package option is ignored when plugin option requests it`() {
        Truth.assertThat(Person::class.java.`package`.name)
            .isEqualTo("tutorial")
    }

    @Test
    fun `descriptor dependencies reference correctly qualified entities`() {
        Truth.assertThat(
            uses_import_file_descriptor.descriptor.dependencies
        ).contains(
            will_be_imported_file_descriptor.descriptor
        )

        Truth.assertThat(
            AddressBookProtos.descriptor.dependencies
        ).contains(
            TimestampProto.descriptor
        )
    }
}
