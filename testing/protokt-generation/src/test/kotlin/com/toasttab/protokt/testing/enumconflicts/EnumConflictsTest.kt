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

package com.toasttab.protokt.testing.enumconflicts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import toasttab.protokt.testing.enumconflicts.AbstractKtDeserializer
import toasttab.protokt.testing.enumconflicts.AbstractKtEnum
import toasttab.protokt.testing.enumconflicts.Any
import toasttab.protokt.testing.enumconflicts.Boolean
import toasttab.protokt.testing.enumconflicts.Bytes
import toasttab.protokt.testing.enumconflicts.BytesSlice
import toasttab.protokt.testing.enumconflicts.Deserializer
import toasttab.protokt.testing.enumconflicts.Double
import toasttab.protokt.testing.enumconflicts.Enum
import toasttab.protokt.testing.enumconflicts.Fixed32
import toasttab.protokt.testing.enumconflicts.Fixed32Val
import toasttab.protokt.testing.enumconflicts.Fixed64
import toasttab.protokt.testing.enumconflicts.Fixed64Val
import toasttab.protokt.testing.enumconflicts.Float
import toasttab.protokt.testing.enumconflicts.Int
import toasttab.protokt.testing.enumconflicts.Int32
import toasttab.protokt.testing.enumconflicts.Int64
import toasttab.protokt.testing.enumconflicts.KtDeserializer
import toasttab.protokt.testing.enumconflicts.KtEnum
import toasttab.protokt.testing.enumconflicts.KtEnumDeserializer
import toasttab.protokt.testing.enumconflicts.KtEnumSerializer
import toasttab.protokt.testing.enumconflicts.KtMessage
import toasttab.protokt.testing.enumconflicts.KtSerializer
import toasttab.protokt.testing.enumconflicts.LengthDelimitedVal
import toasttab.protokt.testing.enumconflicts.List
import toasttab.protokt.testing.enumconflicts.Long
import toasttab.protokt.testing.enumconflicts.Map
import toasttab.protokt.testing.enumconflicts.SFixed64
import toasttab.protokt.testing.enumconflicts.SInt32
import toasttab.protokt.testing.enumconflicts.SInt64
import toasttab.protokt.testing.enumconflicts.String
import toasttab.protokt.testing.enumconflicts.Tag
import toasttab.protokt.testing.enumconflicts.UInt32
import toasttab.protokt.testing.enumconflicts.UInt64
import toasttab.protokt.testing.enumconflicts.Unit
import toasttab.protokt.testing.enumconflicts.UnknownField
import toasttab.protokt.testing.enumconflicts.UnknownValue
import toasttab.protokt.testing.enumconflicts.VarintVal

class EnumConflictsTest {
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
