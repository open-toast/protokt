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

package com.toasttab.protokt.testing

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.Any
import com.toasttab.protokt.Timestamp
import com.toasttab.protokt.pack
import com.toasttab.protokt.unpack
import org.junit.jupiter.api.Test

class AnyTest {
    @Test
    fun `test any pack and unpack`() {
        val timestamp = Timestamp { seconds = 1 }
        val packed = Any.pack(timestamp)

        assertThat(packed.typeUrl)
            .isEqualTo("type.googleapis.com/google.protobuf.Timestamp")

        assertThat(packed.unpack(Timestamp)).isEqualTo(timestamp)
    }
}
