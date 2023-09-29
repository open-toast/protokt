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

package protokt.v1

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BytesSliceTest {
    @Test
    fun testEqualsHashCode() {
        val slice1 = BytesSlice(byteArrayOf(1, 2, 3, 4, 5), 1, 3)
        val slice2 = BytesSlice(byteArrayOf(0, 1, 2, 3, 4, 5, 6), 2, 3)

        assertEquals(slice1, slice2)
        assertEquals(slice1.hashCode(), slice2.hashCode())
    }

    @Test
    fun testEqualsDifferentLengths() {
        val array = byteArrayOf(1, 2, 3, 4, 5)

        assertNotEquals(BytesSlice(array, 0, 3), BytesSlice(array, 0, 4))
    }

    @Test
    fun testEqualsDifferentOffsets() {
        val array = byteArrayOf(1, 2, 3, 4, 5)

        assertNotEquals(BytesSlice(array, 0, 3), BytesSlice(array, 1, 4))
    }
}
