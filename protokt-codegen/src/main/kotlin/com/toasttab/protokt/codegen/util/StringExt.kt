package com.toasttab.protokt.codegen.util

fun String.decapitalize() =
    replaceFirstChar { it.lowercaseChar() }

fun String.capitalize() =
    replaceFirstChar { it.uppercaseChar() }
