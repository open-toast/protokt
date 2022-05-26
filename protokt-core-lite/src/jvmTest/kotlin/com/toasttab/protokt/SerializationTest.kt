/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SerializationTest {
    @Test
    fun `serialize and deserialize`() {
        assertThat(
            Duration.deserialize(Duration.DurationDsl().apply { seconds = 4 }.build().serialize()).seconds
        ).isEqualTo(4)
    }
}
