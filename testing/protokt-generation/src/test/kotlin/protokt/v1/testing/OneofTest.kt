/*
 * Copyright (c) 2025 Toast, Inc.
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
import protokt.v1.testing.oneof.OneofExerciseModel
import kotlin.reflect.KVisibility
import kotlin.reflect.full.primaryConstructor

class OneofTest {
    @Test
    fun `oneofs are abstract and have internal constructors`() {
        assertThat(OneofExerciseModel.Oneof::class.isAbstract).isTrue()
        assertThat(OneofExerciseModel.Oneof::class.isSealed).isFalse()
        assertThat(OneofExerciseModel.Oneof::class.primaryConstructor!!.visibility).isEqualTo(KVisibility.INTERNAL)

        val oneof = OneofExerciseModel {}.oneof

        when (oneof) {
            // empty is permitted
        }

        val foo =
            when (oneof) {
                // this is forced
                else -> Unit
            }
    }
}
