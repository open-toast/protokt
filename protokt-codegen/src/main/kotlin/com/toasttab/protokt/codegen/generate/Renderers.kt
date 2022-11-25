package com.toasttab.protokt.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent

internal fun deserializeValue(p: PropertyInfo) =
    if (p.repeated || p.wrapped || p.nullable || p.fieldType == "MESSAGE") {
        CodeBlock.of("null")
    } else {
        p.defaultValue
    }

internal fun deserializeWrapper(p: PropertyInfo) =
    if (p.nonNullOption) {
        buildCodeBlock {
            beginControlFlow("requireNotNull(%L)", p.name)
            add("StringBuilder(\"${p.name}\")\n")
            withIndent {
                add(
                    (
                        ".append(\" specified nonnull with (protokt." +
                            "${if (p.oneof) "oneof" else "property"}).non_null " +
                            "but was null\")"
                        ).bindSpaces()
                )
                add("\n")
            }
            endControlFlowWithoutNewline()
        }
    } else {
        if (p.map) {
            CodeBlock.of("%M(${p.name})", runtimeFunction("finishMap"))
        } else if (p.repeated) {
            CodeBlock.of("%M(${p.name})", runtimeFunction("finishList"))
        } else {
            buildCodeBlock {
                add(p.name)
                if (p.wrapped && !p.nullable) {
                    add(" ?: %L", p.defaultValue)
                }
            }
        }
    }