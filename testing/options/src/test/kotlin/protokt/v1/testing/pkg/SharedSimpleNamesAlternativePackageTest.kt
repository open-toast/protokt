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

package protokt.v1.testing.pkg

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.testing.propertyType
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class SharedSimpleNamesAlternativePackageTest {
    @Test
    fun `check types of each duration`() {
        checkDurationTypes(ImportsWrapperModel::class)
    }
}

fun checkDurationTypes(klass: KClass<*>) {
    assertThat(klass.propertyType("nativeDuration"))
        .isEqualTo(protokt.v1.google.protobuf.Duration::class.createType())

    assertThat(klass.propertyType("javaDuration"))
        .isEqualTo(java.time.Duration::class.createType())

    assertThat(klass.propertyType("superfluousDuration"))
        .isEqualTo(protokt.v1.testing.Duration::class.createType())
}
