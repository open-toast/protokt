/*
 * Copyright (c) 2021 Toast, Inc.
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

package com.toasttab.protokt.codegen

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.Fixed32
import com.toasttab.protokt.Fixed64
import com.toasttab.protokt.Int32
import com.toasttab.protokt.Int64
import com.toasttab.protokt.SFixed32
import com.toasttab.protokt.SFixed64
import com.toasttab.protokt.SInt32
import com.toasttab.protokt.SInt64
import com.toasttab.protokt.UInt32
import com.toasttab.protokt.UInt64
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties

class NumberTypesFieldNameRegressionTest {
    @Test
    fun `assert number type classes have the right field name`() {
        listOf(
            Int32::class,
            Fixed32::class,
            SFixed32::class,
            UInt32::class,
            SInt32::class,
            Int64::class,
            Fixed64::class,
            SFixed64::class,
            UInt64::class,
            SInt64::class
        ).forEach {
            assertThat(it.declaredMemberProperties.single().name).isEqualTo("value")
        }
    }
}
