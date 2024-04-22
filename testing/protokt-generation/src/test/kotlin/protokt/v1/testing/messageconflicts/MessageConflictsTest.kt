/*
 * Copyright (c) 2023 Toast, Inc.
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

package protokt.v1.testing.messageconflicts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import protokt.v1.testing.enumconflicts.AbstractDecoder
import protokt.v1.testing.enumconflicts.Decoder
import protokt.v1.testing.enumconflicts.Encoder
import protokt.v1.testing.enumconflicts.EnumDecoder

class MessageConflictsTest {
    @Test
    fun `use enum conflicts classes`() {
        assertDoesNotThrow {
            listOf(
                Boolean::class,
                Double::class,
                Float::class,
                Int::class,
                List::class,
                Long::class,
                Map::class,
                String::class,
                Unit::class,
                Enum::class,
                Int32::class,
                Int64::class,
                Fixed32::class,
                Fixed64::class,
                SFixed64::class,
                SInt32::class,
                SInt64::class,
                UInt32::class,
                UInt64::class,
                Bytes::class,
                Deserializer::class,
                Decoder::class,
                Encoder::class,
                Tag::class,
                UnknownField::class,
                AbstractDecoder::class,
                Message::class,
                BytesSlice::class,
                EnumDecoder::class,
                UnknownField::class,
                UnknownValue::class,
                VarintVal::class,
                Fixed32Val::class,
                Fixed64Val::class,
                LengthDelimitedVal::class,
                Any::class
            )
        }
    }
}
