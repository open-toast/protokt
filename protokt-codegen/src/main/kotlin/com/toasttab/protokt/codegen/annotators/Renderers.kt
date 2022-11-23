package com.toasttab.protokt.codegen.annotators

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.toasttab.protokt.codegen.annotators.PropertyAnnotator.PropertyInfo
import com.toasttab.protokt.codegen.impl.runtimeFunction

internal fun deserializeType(p: PropertyInfo) =
    if (p.repeated || p.map) {
        p.deserializeType as ParameterizedTypeName
        ClassName(p.deserializeType.rawType.packageName, "Mutable" + p.deserializeType.rawType.simpleName)
            .parameterizedBy(p.deserializeType.typeArguments)
            .copy(nullable = true)
    } else {
        p.deserializeType
    }

internal fun deserializeValue(p: PropertyInfo) =
    if (p.repeated || p.wrapped || p.nullable || p.fieldType == "MESSAGE") {
        CodeBlock.of("null")
    } else {
        p.defaultValue
    }

internal fun deserializeVar(p: PropertyInfo) =
    if (p.fieldType == "MESSAGE" || p.repeated || p.oneof || p.nullable || p.wrapped) {
        CodeBlock.of("%L : %T = %L", p.name, deserializeType(p), deserializeValue(p))
    } else {
        CodeBlock.of("%L = %L", p.name, deserializeValue(p))
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
