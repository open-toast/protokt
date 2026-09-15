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

package protokt.v1.reflect

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import protokt.v1.Converter

class ConverterTypeValidationTest {
    @Test
    fun acceptsDisjointTypes() {
        validateConverterTypes(StringToIntConverter)
    }

    @Test
    fun rejectsSameType() {
        assertThrows<IllegalArgumentException> {
            validateConverterTypes(StringToStringConverter)
        }
    }

    @Test
    fun rejectsSubtype() {
        assertThrows<IllegalArgumentException> {
            validateConverterTypes(ParentToChildConverter)
        }
    }
}

private object StringToIntConverter : Converter<String, Int> {
    override val wireType = String::class
    override val valueType = Int::class

    override fun wrap(unwrapped: String) =
        unwrapped.length

    override fun unwrap(wrapped: Int) =
        wrapped.toString()
}

private object StringToStringConverter : Converter<String, String> {
    override val wireType = String::class
    override val valueType = String::class

    override fun wrap(unwrapped: String) =
        unwrapped

    override fun unwrap(wrapped: String) =
        wrapped
}

private object ParentToChildConverter : Converter<Parent, Child> {
    override val wireType = Parent::class
    override val valueType = Child::class

    override fun wrap(unwrapped: Parent) =
        Child()

    override fun unwrap(wrapped: Child) =
        Parent()
}

private open class Parent

private class Child : Parent()
