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

package com.toasttab.protokt.codegen.impl

import arrow.core.None
import arrow.core.Some
import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.PPackage
import org.junit.jupiter.api.Test

class DuplicateSimpleNameImportFiltererTest {
    @Test
    fun `duplicate import resolved in favor of non-protokt class`() {
        val imports =
            sequenceOf(
                Import.Class(PClass.fromName("java.time.Duration")),
                Import.Class(PClass.fromName("com.toasttab.protokt.Duration"))
            )

        assertThat(imports.filterDuplicateSimpleNames(PPackage.DEFAULT) { None })
            .containsExactly(
                Import.Class(PClass.fromName("java.time.Duration"))
            )
    }

    @Test
    fun `triple import resolved`() {
        val imports =
            sequenceOf(
                Import.Class(PClass.fromName("java.time.Duration")),
                Import.Class(PClass.fromName("com.toasttab.protokt.Duration")),
                Import.Class(PClass.fromName("com.toasttab.model.Duration"))
            )

        assertThat(imports.filterDuplicateSimpleNames(PPackage.DEFAULT) { None })
            .containsExactly(
                Import.Class(PClass.fromName("java.time.Duration"))
            )
    }

    @Test
    fun `import with duplicate in own package skipped`() {
        val imports = sequenceOf(Import.Class(PClass.fromName("some.Int")))

        assertThat(
            imports.filterDuplicateSimpleNames(PPackage.PROTOKT) { Some(Int::class) }
        ).isEmpty()
    }
}
