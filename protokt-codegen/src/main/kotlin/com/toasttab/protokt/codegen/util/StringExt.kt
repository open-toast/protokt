package com.toasttab.protokt.codegen.util

import arrow.core.None
import arrow.core.Some

fun String.emptyToNone() = when {
    isEmpty() -> None
    else -> Some(this)
}
