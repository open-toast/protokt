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

package protokt.v1.testing

import protokt.v1.Bytes
import protokt.v1.proto3_unittest.ForeignEnum
import protokt.v1.proto3_unittest.ForeignMessage
import protokt.v1.proto3_unittest.TestAllTypes
import protokt.v1.protobuf_unittest_import.ImportMessage
import protokt.v1.protobuf_unittest_import.PublicImportMessage

fun getAllTypesAllSet() =
    TestAllTypes {
        optionalInt32 = 101
        optionalInt64 = 102
        optionalUint32 = 103u
        optionalUint64 = 104u
        optionalSint32 = 105
        optionalSint64 = 106
        optionalFixed32 = 107u
        optionalFixed64 = 108u
        optionalSfixed32 = 109
        optionalSfixed64 = 110
        optionalFloat = 111f
        optionalDouble = 112.0
        optionalBool = true
        optionalString = "114"
        optionalNestedMessage = TestAllTypes.NestedMessage { bb = 115 }
        optionalForeignMessage = ForeignMessage { c = 116 }
        optionalNestedEnum = TestAllTypes.NestedEnum.FOO
        optionalForeignEnum = ForeignEnum.FOREIGN_FOO
        optionalStringPiece = "119"
        optionalCord = "120"
        optionalPublicImportMessage = PublicImportMessage { e = 121 }
        optionalLazyMessage = TestAllTypes.NestedMessage { bb = 122 }
        optionalUnverifiedLazyMessage = TestAllTypes.NestedMessage { bb = 123 }

        repeatedInt32 = listOf(124)
        repeatedInt64 = listOf(125)
        repeatedUint32 = listOf(126u)
        repeatedUint64 = listOf(127u)
        repeatedSint32 = listOf(128)
        repeatedSint64 = listOf(129)
        repeatedFixed32 = listOf(130u)
        repeatedFixed64 = listOf(131u)
        repeatedSfixed32 = listOf(132)
        repeatedSfixed64 = listOf(133)
        repeatedFloat = listOf(134f)
        repeatedDouble = listOf(135.0)
        repeatedBool = listOf(true)
        repeatedString = listOf("136")
        repeatedBytes = listOf(Bytes.from("137".toByteArray()))
        repeatedNestedMessage = listOf(TestAllTypes.NestedMessage { bb = 138 })
        repeatedForeignMessage = listOf(ForeignMessage { c = 139 })
        repeatedImportMessage = listOf(ImportMessage { d = 140 })
        repeatedNestedEnum = listOf(TestAllTypes.NestedEnum.FOO)
        repeatedForeignEnum = listOf(ForeignEnum.FOREIGN_FOO)
        repeatedStringPiece = listOf("143")
        repeatedCord = listOf("144")
        repeatedLazyMessage = listOf(TestAllTypes.NestedMessage { bb = 145 })

        oneofField = TestAllTypes.OneofField.OneofUint32(146u)
        optionalLazyImportMessage = ImportMessage { d = 147 }
    }
