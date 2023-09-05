/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.BytesSlice

class BytesSliceTest {
    @Test
    fun testEqualsHashCode() {
        val slice1 = BytesSlice(byteArrayOf(1, 2, 3, 4, 5), 1, 3)
        val slice2 = BytesSlice(byteArrayOf(0, 1, 2, 3, 4, 5, 6), 2, 3)

        assertThat(slice1).isEqualTo(slice2)
        assertThat(slice1.hashCode()).isEqualTo(slice2.hashCode())
    }

    @Test
    fun testEqualsDifferentLengths() {
        val array = byteArrayOf(1, 2, 3, 4, 5)

        assertThat(BytesSlice(array, 0, 3)).isNotEqualTo(BytesSlice(array, 0, 4))
    }

    @Test
    fun testEqualsDifferentOffsets() {
        val array = byteArrayOf(1, 2, 3, 4, 5)

        assertThat(BytesSlice(array, 0, 3)).isNotEqualTo(BytesSlice(array, 1, 4))
    }
}
