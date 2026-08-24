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

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class SizesTest {
    @Test
    fun messageSizeIsEvaluatedOnce() {
        val message = CountingMessage()

        assertThat(Sizes.sizeOf(message)).isEqualTo(130)
        assertThat(message.sizeEvaluations).isEqualTo(1)
    }
}

private class CountingMessage : AbstractMessage() {
    var sizeEvaluations = 0
        private set

    override val unknownFields = UnknownFieldSet.empty()

    override fun serializedSize(): Int {
        sizeEvaluations++
        return 128
    }

    override fun serialize(writer: Writer) =
        Unit
}
