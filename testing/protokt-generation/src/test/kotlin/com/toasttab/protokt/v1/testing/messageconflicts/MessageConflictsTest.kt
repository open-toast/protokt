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

package com.toasttab.protokt.v1.testing.messageconflicts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import toasttab.protokt.v1.testing.messageconflicts.AbstractKtDeserializer
import toasttab.protokt.v1.testing.messageconflicts.AbstractKtEnum
import toasttab.protokt.v1.testing.messageconflicts.Any
import toasttab.protokt.v1.testing.messageconflicts.Boolean
import toasttab.protokt.v1.testing.messageconflicts.Bytes
import toasttab.protokt.v1.testing.messageconflicts.BytesSlice
import toasttab.protokt.v1.testing.messageconflicts.Deserializer
import toasttab.protokt.v1.testing.messageconflicts.Double
import toasttab.protokt.v1.testing.messageconflicts.Enum
import toasttab.protokt.v1.testing.messageconflicts.Fixed32
import toasttab.protokt.v1.testing.messageconflicts.Fixed32Val
import toasttab.protokt.v1.testing.messageconflicts.Fixed64
import toasttab.protokt.v1.testing.messageconflicts.Fixed64Val
import toasttab.protokt.v1.testing.messageconflicts.Float
import toasttab.protokt.v1.testing.messageconflicts.Int
import toasttab.protokt.v1.testing.messageconflicts.Int32
import toasttab.protokt.v1.testing.messageconflicts.Int64
import toasttab.protokt.v1.testing.messageconflicts.KtDeserializer
import toasttab.protokt.v1.testing.messageconflicts.KtEnum
import toasttab.protokt.v1.testing.messageconflicts.KtEnumDeserializer
import toasttab.protokt.v1.testing.messageconflicts.KtEnumSerializer
import toasttab.protokt.v1.testing.messageconflicts.KtMessage
import toasttab.protokt.v1.testing.messageconflicts.KtSerializer
import toasttab.protokt.v1.testing.messageconflicts.LengthDelimitedVal
import toasttab.protokt.v1.testing.messageconflicts.List
import toasttab.protokt.v1.testing.messageconflicts.Long
import toasttab.protokt.v1.testing.messageconflicts.Map
import toasttab.protokt.v1.testing.messageconflicts.SFixed64
import toasttab.protokt.v1.testing.messageconflicts.SInt32
import toasttab.protokt.v1.testing.messageconflicts.SInt64
import toasttab.protokt.v1.testing.messageconflicts.String
import toasttab.protokt.v1.testing.messageconflicts.Tag
import toasttab.protokt.v1.testing.messageconflicts.UInt32
import toasttab.protokt.v1.testing.messageconflicts.UInt64
import toasttab.protokt.v1.testing.messageconflicts.Unit
import toasttab.protokt.v1.testing.messageconflicts.UnknownField
import toasttab.protokt.v1.testing.messageconflicts.UnknownValue
import toasttab.protokt.v1.testing.messageconflicts.VarintVal

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
                KtDeserializer::class,
                KtSerializer::class,
                KtEnumSerializer::class,
                Tag::class,
                UnknownField::class,
                KtEnum::class,
                AbstractKtDeserializer::class,
                KtMessage::class,
                AbstractKtEnum::class,
                BytesSlice::class,
                KtEnumDeserializer::class,
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
