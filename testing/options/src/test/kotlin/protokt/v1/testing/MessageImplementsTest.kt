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

class MessageImplementsTest {
    private val model = ImplementsModel { id = Id("asdf") }
    private val model2 = ImplementsModel2 { id = "asdf" }
    private val model3 = ImplementsModel3 { id = "asdf" }

    @Test
    fun `message with wrapped field can be assigned to its interface`() {
        val deserialized: IModel = ImplementsModel.deserialize(model.serialize())

        assertThat(deserialized.id).isEqualTo(model.id)
    }

    @Test
    fun `message with primitive field can be assigned to its interface`() {
        val deserialized: IModel2 = ImplementsModel2.deserialize(model2.serialize())

        assertThat(deserialized.id).isEqualTo(model2.id)
    }

    @Test
    fun `message implementing by a delegate can be assigned to its interface`() {
        val byDelegate: IModel2 = ImplementsWithDelegate { modelTwo = model2 }

        assertThat(byDelegate.id).isEqualTo(model2.id)
    }

    @Test
    fun `message implementing by a delegate can serialized and deserialized`() {
        val byDelegate = ImplementsWithDelegate { modelTwo = model2 }
        val serialized = byDelegate.serialize()

        assertThat(serialized.size).isGreaterThan(model2.messageSize())
        assertThat(ImplementsWithDelegate.deserialize(serialized)).isEqualTo(byDelegate)
    }

    @Test
    fun `message implementing by a nullable delegate has its delegated property nullable when the delegate has a non-null accessor`() {
        assertThat(ImplementsModelAgainAgain::class.propertyIsMarkedNullable("bar")).isTrue()
    }

    @Test
    fun `message implementing by a nullable delegate can be assigned to its interface`() {
        val byDelegate: IModel2 = ImplementsWithNullableDelegate { modelTwo = model2 }

        assertThat(byDelegate.id).isEqualTo(model2.id)
    }

    @Test
    fun `message implementing by a nullable delegate can serialized and deserialized`() {
        val byDelegate = ImplementsWithNullableDelegate { modelTwo = model2 }
        val serialized = byDelegate.serialize()

        assertThat(serialized.size).isGreaterThan(model2.messageSize())
        assertThat(ImplementsWithNullableDelegate.deserialize(serialized)).isEqualTo(byDelegate)
    }

    @Test
    fun `message implementing message extender by a delegate can be assigned to its interface`() {
        val byDelegate: IModel3 = ImplementsWithDelegate2 { modelThree = model3 }

        assertThat(byDelegate.id).isEqualTo(model3.id)
    }

    @Test
    fun `message implementing message extender by a delegate can serialized and deserialized`() {
        val byDelegate = ImplementsWithDelegate2 { modelThree = model3 }
        val serialized = byDelegate.serialize()

        assertThat(serialized.size).isGreaterThan(model3.messageSize())
        assertThat(ImplementsWithDelegate2.deserialize(serialized)).isEqualTo(byDelegate)
    }

    @Test
    fun `message implementing message extender by a nullable delegate can be assigned to its interface`() {
        val byDelegate: IModel3 = ImplementsWithNullableDelegate2 { modelThree = model3 }

        assertThat(byDelegate.id).isEqualTo(model3.id)
    }

    @Test
    fun `message implementing message extender by a nullable delegate can serialized and deserialized`() {
        val byDelegate = ImplementsWithNullableDelegate2 { modelThree = model3 }
        val serialized = byDelegate.serialize()

        assertThat(serialized.size).isGreaterThan(model3.messageSize())
        assertThat(ImplementsWithNullableDelegate2.deserialize(serialized)).isEqualTo(byDelegate)
    }
}
