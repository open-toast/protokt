package com.toasttab.protokt.codegen.util

import arrow.core.None
import arrow.core.Some

fun String.decapitalize() =
    replaceFirstChar { it.lowercaseChar() }

fun String.capitalize() =
    replaceFirstChar { it.uppercaseChar() }

fun String.emptyToNone() = when {
    isEmpty() -> None
    else -> Some(this)
}

fun String.emptyOrPrecedeWithDot() =
    emptyToNone().fold({ "" }, { ".$it" })

fun String.emptyOrFollowWithDot() =
    emptyToNone().fold({ "" }, { "$it." })
