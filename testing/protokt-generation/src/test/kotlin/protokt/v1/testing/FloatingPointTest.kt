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

package protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class FloatingPointTest {
    @Test
    fun `NaN payloads are equal and have equal hashes`() {
        val first =
            FloatingPointMessage {
                floatValue = Float.fromBits(0x7fc00001)
                doubleValue = Double.fromBits(0x7ff8000000000001L)
                optionalFloat = Float.fromBits(0x7fc00001)
                selection = FloatingPointMessage.Selection.SelectedDouble(Double.fromBits(0x7ff8000000000001L))
                floatValues = listOf(Float.fromBits(0x7fc00001))
                doubleValues = mapOf("nan" to Double.fromBits(0x7ff8000000000001L))
            }
        val second =
            FloatingPointMessage {
                floatValue = Float.fromBits(0x7fc00002)
                doubleValue = Double.fromBits(0x7ff8000000000002L)
                optionalFloat = Float.fromBits(0x7fc00002)
                selection = FloatingPointMessage.Selection.SelectedDouble(Double.fromBits(0x7ff8000000000002L))
                floatValues = listOf(Float.fromBits(0x7fc00002))
                doubleValues = mapOf("nan" to Double.fromBits(0x7ff8000000000002L))
            }

        assertThat(first).isEqualTo(second)
        assertThat(first.hashCode()).isEqualTo(second.hashCode())
    }

    @Test
    fun `signed zeroes are unequal`() {
        val positive =
            FloatingPointMessage {
                floatValue = 0.0f
                doubleValue = 0.0
                optionalFloat = 0.0f
                selection = FloatingPointMessage.Selection.SelectedFloat(0.0f)
                floatValues = listOf(0.0f)
                doubleValues = mapOf("zero" to 0.0)
            }
        val negative =
            FloatingPointMessage {
                floatValue = -0.0f
                doubleValue = -0.0
                optionalFloat = -0.0f
                selection = FloatingPointMessage.Selection.SelectedFloat(-0.0f)
                floatValues = listOf(-0.0f)
                doubleValues = mapOf("zero" to -0.0)
            }

        assertThat(positive).isNotEqualTo(negative)
    }

    @Test
    fun `negative zero is serialized and retains its raw bits`() {
        val positive = FloatingPointMessage { }
        val negative =
            FloatingPointMessage {
                floatValue = -0.0f
                doubleValue = -0.0
            }

        assertThat(positive.serializedSize()).isEqualTo(0)
        assertThat(negative.serializedSize()).isGreaterThan(0)

        val roundTrip = FloatingPointMessage.deserialize(negative.serialize())
        assertThat(roundTrip.floatValue.toRawBits()).isEqualTo((-0.0f).toRawBits())
        assertThat(roundTrip.doubleValue.toRawBits()).isEqualTo((-0.0).toRawBits())
    }

    @Test
    fun `converted floating point fields use bit equality`() {
        val first =
            WellKnownTypes {
                float = Float.fromBits(0x7fc00001)
                double = Double.fromBits(0x7ff8000000000001L)
            }
        val second =
            WellKnownTypes {
                float = Float.fromBits(0x7fc00002)
                double = Double.fromBits(0x7ff8000000000002L)
            }
        val negativeZero =
            WellKnownTypes {
                float = -0.0f
                double = -0.0
            }
        val positiveZero =
            WellKnownTypes {
                float = 0.0f
                double = 0.0
            }

        assertThat(first).isEqualTo(second)
        assertThat(first.hashCode()).isEqualTo(second.hashCode())
        assertThat(negativeZero).isNotEqualTo(positiveZero)

        val roundTrip = WellKnownTypes.deserialize(negativeZero.serialize())
        assertThat(roundTrip.float?.toRawBits()).isEqualTo((-0.0f).toRawBits())
        assertThat(roundTrip.double?.toRawBits()).isEqualTo((-0.0).toRawBits())
    }
}
