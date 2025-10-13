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
import org.junit.Test

class OneofImplementsTest {
    private val obj =
        ContainsOneofThatImplements {
            implementingOneof = ContainsOneofThatImplements.ImplementingOneof.ImplementsOne(
                ImplementsOneof1 { id = Id("val") }
            )
        }

    @Test
    fun `property shared between oneof types can be assigned and accessed without switching`() {
        val assigned: OneofModel? = obj.implementingOneof

        assertThat(assigned?.id).isEqualTo(Id("val"))
    }

    @Test
    fun `property shared between oneof types can be assigned and accessed without switching using non-null accessor`() {
        val assigned: OneofModel = obj.requireImplementingOneof

        assertThat(assigned.id).isEqualTo(Id("val"))
    }
}
