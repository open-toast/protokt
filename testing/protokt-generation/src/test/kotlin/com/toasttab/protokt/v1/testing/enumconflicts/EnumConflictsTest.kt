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

package com.toasttab.protokt.v1.testing.enumconflicts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import toasttab.protokt.v1.testing.enumconflicts.AbstractKtDeserializer
import toasttab.protokt.v1.testing.enumconflicts.AbstractKtEnum
import toasttab.protokt.v1.testing.enumconflicts.Any
import toasttab.protokt.v1.testing.enumconflicts.Boolean
import toasttab.protokt.v1.testing.enumconflicts.Bytes
import toasttab.protokt.v1.testing.enumconflicts.BytesSlice
import toasttab.protokt.v1.testing.enumconflicts.Deserializer
import toasttab.protokt.v1.testing.enumconflicts.Double
import toasttab.protokt.v1.testing.enumconflicts.Enum
import toasttab.protokt.v1.testing.enumconflicts.Fixed32
import toasttab.protokt.v1.testing.enumconflicts.Fixed32Val
import toasttab.protokt.v1.testing.enumconflicts.Fixed64
import toasttab.protokt.v1.testing.enumconflicts.Fixed64Val
import toasttab.protokt.v1.testing.enumconflicts.Float
import toasttab.protokt.v1.testing.enumconflicts.Int
import toasttab.protokt.v1.testing.enumconflicts.Int32
import toasttab.protokt.v1.testing.enumconflicts.Int64
import toasttab.protokt.v1.testing.enumconflicts.KtDeserializer
import toasttab.protokt.v1.testing.enumconflicts.KtEnum
import toasttab.protokt.v1.testing.enumconflicts.KtEnumDeserializer
import toasttab.protokt.v1.testing.enumconflicts.KtEnumSerializer
import toasttab.protokt.v1.testing.enumconflicts.KtMessage
import toasttab.protokt.v1.testing.enumconflicts.KtSerializer
import toasttab.protokt.v1.testing.enumconflicts.LengthDelimitedVal
import toasttab.protokt.v1.testing.enumconflicts.List
import toasttab.protokt.v1.testing.enumconflicts.Long
import toasttab.protokt.v1.testing.enumconflicts.Map
import toasttab.protokt.v1.testing.enumconflicts.SFixed64
import toasttab.protokt.v1.testing.enumconflicts.SInt32
import toasttab.protokt.v1.testing.enumconflicts.SInt64
import toasttab.protokt.v1.testing.enumconflicts.String
import toasttab.protokt.v1.testing.enumconflicts.Tag
import toasttab.protokt.v1.testing.enumconflicts.UInt32
import toasttab.protokt.v1.testing.enumconflicts.UInt64
import toasttab.protokt.v1.testing.enumconflicts.Unit
import toasttab.protokt.v1.testing.enumconflicts.UnknownField
import toasttab.protokt.v1.testing.enumconflicts.UnknownValue
import toasttab.protokt.v1.testing.enumconflicts.VarintVal

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
