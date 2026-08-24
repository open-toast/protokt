/*
 * Copyright (c) 2026 Toast, Inc.
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
import org.junit.jupiter.api.Test

@OptIn(OnlyForUseByGeneratedProtoCode::class)
class SizesTest {
    @Test
    fun `message size is evaluated once`() {
        var evaluations = 0
        val message =
            object : Message {
                override val unknownFields = UnknownFieldSet.empty()

                override fun serializedSize(): Int {
                    evaluations++
                    return 128
                }

                override fun serialize(writer: Writer) =
                    error("not used")

                override fun serialize(): ByteArray =
                    error("not used")
            }

        assertThat(Sizes.sizeOf(message)).isEqualTo(130)
        assertThat(evaluations).isEqualTo(1)
    }
}
