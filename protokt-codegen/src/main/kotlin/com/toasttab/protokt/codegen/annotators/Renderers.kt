package com.toasttab.protokt.codegen.annotators

import com.squareup.kotlinpoet.CodeBlock
import com.toasttab.protokt.codegen.annotators.PropertyAnnotator.PropertyInfo
import com.toasttab.protokt.codegen.impl.runtimeFunction

internal fun deserializeValue(p: PropertyInfo) =
    if (p.repeated || p.wrapped || p.nullable || p.fieldType == "MESSAGE") {
        CodeBlock.of("null")
    } else {
        p.defaultValue
    }

internal fun deserializeWrapper(p: PropertyInfo) =
    if (p.nonNullOption) {
        CodeBlock.builder()
            .add("requireNotNull(%L)·{", p.name)
            .add("%S", "${p.name} specified nonnull with (protokt.${if (p.oneof) "oneof" else "property" }).non_null but was null")
            .add("}")
            .build()
    } else {
        if (p.map) {
            CodeBlock.of("%M(${p.name})", runtimeFunction("finishMap"))
        } else if (p.repeated) {
            CodeBlock.of("%M(${p.name})", runtimeFunction("finishList"))
        } else {
            CodeBlock.of(
                p.name +
                    if (p.wrapped && !p.nullable) {
                        " ?: ${p.defaultValue}"
                    } else {
                        ""
                    }
            )
        }
    }
