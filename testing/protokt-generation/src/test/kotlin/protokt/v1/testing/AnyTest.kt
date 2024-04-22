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

package protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.GeneratedMessage
import protokt.v1.google.protobuf.Timestamp
import protokt.v1.google.protobuf.pack
import protokt.v1.google.protobuf.unpack
import kotlin.reflect.full.findAnnotation

class AnyTest {
    @Test
    fun `test any pack and unpack`() {
        val timestamp = Timestamp { seconds = 1 }
        val packed = protokt.v1.google.protobuf.Any.pack(timestamp)

        assertThat(packed.typeUrl)
            .isEqualTo("type.googleapis.com/google.protobuf.Timestamp")

        assertThat(packed.unpack(Timestamp)).isEqualTo(timestamp)
    }

    @Test
    fun `nested messages get correct type name`() {
        assertThat(DocTestRoot.NestedDocTest::class.findAnnotation<GeneratedMessage>()!!.fullTypeName)
            .isEqualTo("protokt.v1.testing.DocTestRoot.NestedDocTest")
    }
}
