/*
 * Copyright (c) 2021 Toast, Inc.
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

package com.toasttab.protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.toasttab.protokt.v1.Bytes
import com.toasttab.protokt.v1.codegen.util.FieldType
import com.toasttab.protokt.v1.codegen.util.FieldType.BOOL
import com.toasttab.protokt.v1.codegen.util.FieldType.BYTES
import com.toasttab.protokt.v1.codegen.util.FieldType.DOUBLE
import com.toasttab.protokt.v1.codegen.util.FieldType.ENUM
import com.toasttab.protokt.v1.codegen.util.FieldType.FIXED32
import com.toasttab.protokt.v1.codegen.util.FieldType.FIXED64
import com.toasttab.protokt.v1.codegen.util.FieldType.FLOAT
import com.toasttab.protokt.v1.codegen.util.FieldType.INT32
import com.toasttab.protokt.v1.codegen.util.FieldType.INT64
import com.toasttab.protokt.v1.codegen.util.FieldType.MESSAGE
import com.toasttab.protokt.v1.codegen.util.FieldType.SFIXED32
import com.toasttab.protokt.v1.codegen.util.FieldType.SFIXED64
import com.toasttab.protokt.v1.codegen.util.FieldType.SINT32
import com.toasttab.protokt.v1.codegen.util.FieldType.SINT64
import com.toasttab.protokt.v1.codegen.util.FieldType.STRING
import com.toasttab.protokt.v1.codegen.util.FieldType.UINT32
import com.toasttab.protokt.v1.codegen.util.FieldType.UINT64

val FieldType.defaultValue: CodeBlock
    get() = when (this) {
        MESSAGE -> CodeBlock.of("null")
        ENUM -> error("enums do not have defaults; this is bug in the code generator")
        BOOL -> CodeBlock.of("false")
        FIXED32, INT32, SFIXED32, SINT32, UINT32 -> CodeBlock.of("0")
        FIXED64, INT64, SFIXED64, SINT64, UINT64 -> CodeBlock.of("0L")
        FLOAT -> CodeBlock.of("0.0F")
        DOUBLE -> CodeBlock.of("0.0")
        BYTES -> CodeBlock.of("%T.empty()", Bytes::class)
        STRING -> CodeBlock.of("\"\"")
    }
