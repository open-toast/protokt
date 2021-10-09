package com.toasttab.protokt.codegen.impl

import com.toasttab.protokt.codegen.template.Message.Message.PropertyInfo

fun deserializeType(p: PropertyInfo) =
    if (p.repeated || p.map) {
        "Mutable"
    } else {
        ""
    } + p.deserializeType

fun deserializeValue(p: PropertyInfo) =
    if (p.repeated || p.wrapped || p.nullable || p.fieldType == "MESSAGE") {
        "null"
    } else {
        p.defaultValue
    }
