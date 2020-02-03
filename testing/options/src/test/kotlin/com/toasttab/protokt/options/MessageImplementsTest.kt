/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.options

import com.google.common.truth.Truth.assertThat
import com.toasttab.model.Id
import com.toasttab.model.ImplementsModel
import com.toasttab.model.ImplementsModel2
import com.toasttab.model.ImplementsWithDelegate
import com.toasttab.model.Model
import com.toasttab.model.Model2
import org.junit.jupiter.api.Test

class MessageImplementsTest {
    private val model = ImplementsModel { id = Id("asdf") }
    private val model2 = ImplementsModel2 { id = "asdf" }

    @Test
    fun `message with wrapped field can be assigned to its interface`() {
        val deserialized: Model = ImplementsModel.deserialize(model.serialize())

        assertThat(deserialized.id).isEqualTo(model.id)
    }

    @Test
    fun `message with primitive field can be assigned to its interface`() {
        val deserialized: Model2 = ImplementsModel2.deserialize(model2.serialize())

        assertThat(deserialized.id).isEqualTo(model2.id)
    }

    @Test
    fun `message implementing by a delegate can be assigned to its interface`() {
        val byDelegate: Model2 = ImplementsWithDelegate { modelTwo = model2 }

        assertThat(byDelegate.id).isEqualTo(model2.id)
    }
}
