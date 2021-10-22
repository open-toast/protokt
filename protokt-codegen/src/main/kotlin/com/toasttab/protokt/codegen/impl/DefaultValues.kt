package com.toasttab.protokt.codegen.impl

import com.squareup.kotlinpoet.CodeBlock
import com.toasttab.protokt.codegen.model.FieldType

val FieldType.defaultValue: CodeBlock
    get() = when (this) {
        FieldType.MESSAGE -> CodeBlock.of("null")
        FieldType.ENUM -> throw IllegalAccessException(
            "|An Enum type doesn't have a standard default value. " +
                "If you have `enum Foo`, you can use Foo.from(0)"
        )
        FieldType.BOOL -> CodeBlock.of("false")
        FieldType.FIXED32, FieldType.INT32, FieldType.SFIXED32, FieldType.SINT32, FieldType.UINT32 -> CodeBlock.of("0")
        FieldType.FIXED64, FieldType.INT64, FieldType.SFIXED64, FieldType.SINT64, FieldType.UINT64 -> CodeBlock.of("0L")
        FieldType.FLOAT -> CodeBlock.of("0.0F")
        FieldType.DOUBLE -> CodeBlock.of("0.0")
        FieldType.BYTES -> CodeBlock.of("%T.empty()", com.toasttab.protokt.rt.Bytes::class)
        FieldType.STRING -> CodeBlock.of("\"\"")
    }
