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

package protokt.v1.codegen.util

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.asTypeName
import protokt.v1.Sizes
import protokt.v1.codegen.generate.sizeOf
import protokt.v1.reflect.FieldType

sealed interface SizeFn {
    class Const(val size: Int) : SizeFn
    class Method(val method: MemberName) : SizeFn
}

internal val FieldType.sizeFn
    get() = when (this) {
        FieldType.Bool -> SizeFn.Const(1)
        FieldType.Double, FieldType.Fixed64, FieldType.SFixed64 -> SizeFn.Const(8)
        FieldType.Float, FieldType.Fixed32, FieldType.SFixed32 -> SizeFn.Const(4)
        FieldType.SInt32 -> SizeFn.Method(Sizes::class.asTypeName().member(Sizes::sizeOfSInt32.name))
        FieldType.SInt64 -> SizeFn.Method(Sizes::class.asTypeName().member(Sizes::sizeOfSInt64.name))
        else -> SizeFn.Method(sizeOf)
    }

internal val FieldType.defaultValue: CodeBlock
    get() = when (this) {
        FieldType.Message -> CodeBlock.of("null")
        FieldType.Enum -> error("enums defaults are discovered external to this property; this is bug in the code generator")
        FieldType.Bool -> CodeBlock.of("false")
        FieldType.Fixed32, FieldType.UInt32 -> CodeBlock.of("0u")
        FieldType.Int32, FieldType.SFixed32, FieldType.SInt32 -> CodeBlock.of("0")
        FieldType.Fixed64, FieldType.UInt64 -> CodeBlock.of("0uL")
        FieldType.Int64, FieldType.SFixed64, FieldType.SInt64 -> CodeBlock.of("0L")
        FieldType.Float -> CodeBlock.of("0.0f")
        FieldType.Double -> CodeBlock.of("0.0")
        FieldType.Bytes -> CodeBlock.of("%T.empty()", protokt.v1.Bytes::class)
        FieldType.String -> CodeBlock.of("\"\"")
    }
