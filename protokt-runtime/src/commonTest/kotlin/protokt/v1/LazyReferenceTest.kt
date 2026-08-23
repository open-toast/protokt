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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LazyReferenceTest {
    @Test
    fun retainsOneRepresentation() {
        val converter = CountingConverter()
        val reference = LazyReference(WireValue("value"), converter)

        assertEquals(Value("value"), reference.value())
        assertEquals(Value("value"), reference.value())
        assertEquals(1, converter.wrapCount)

        assertEquals(WireValue("value"), reference.wireValue())
        assertEquals(WireValue("value"), reference.wireValue())
        assertEquals(1, converter.unwrapCount)

        assertEquals(Value("value"), reference.value())
        assertEquals(2, converter.wrapCount)
    }

    @Test
    fun rejectsSameTypeConverter() {
        val converter =
            object : Converter<String, String> {
                override val wireType = String::class
                override val valueType = String::class

                override fun wrap(unwrapped: String) =
                    unwrapped

                override fun unwrap(wrapped: String) =
                    wrapped
            }

        assertFailsWith<IllegalArgumentException> {
            LazyReference("value", converter)
        }
    }

    @Test
    fun rejectsOverlappingTypeConverter() {
        val converter =
            object : Converter<Parent, Child> {
                override val wireType = Parent::class
                override val valueType = Child::class

                override fun wrap(unwrapped: Parent) =
                    Child(unwrapped.value)

                override fun unwrap(wrapped: Child) =
                    Parent(wrapped.value)
            }

        assertFailsWith<IllegalArgumentException> {
            LazyReference(Child("value"), converter)
        }

        val reference = LazyReference(Parent("value"), converter)
        assertFailsWith<IllegalArgumentException> {
            reference.value()
        }
    }

    @Test
    fun rejectsValueMatchingNeitherType() {
        assertFailsWith<IllegalArgumentException> {
            LazyReference(1, CountingConverter())
        }
    }
}

private data class WireValue(val value: String)

private data class Value(val value: String)

private open class Parent(val value: String)

private class Child(value: String) : Parent(value)

private class CountingConverter : Converter<WireValue, Value> {
    override val wireType = WireValue::class
    override val valueType = Value::class

    var wrapCount = 0
        private set

    var unwrapCount = 0
        private set

    override fun wrap(unwrapped: WireValue): Value {
        wrapCount++
        return Value(unwrapped.value)
    }

    override fun unwrap(wrapped: Value): WireValue {
        unwrapCount++
        return WireValue(wrapped.value)
    }
}
