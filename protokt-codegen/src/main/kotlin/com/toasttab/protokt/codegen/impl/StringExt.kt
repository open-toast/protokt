package com.toasttab.protokt.codegen.impl

import arrow.core.None
import arrow.core.Some

internal fun String.emptyToNone() = when {
    isEmpty() -> None
    else -> Some(this)
}

internal fun String.emptyOrPrecedeWithDot() =
    emptyToNone().fold({ "" }, { ".$it" })

internal fun String.emptyOrFollowWithDot() =
    emptyToNone().fold({ "" }, { "$it." })
