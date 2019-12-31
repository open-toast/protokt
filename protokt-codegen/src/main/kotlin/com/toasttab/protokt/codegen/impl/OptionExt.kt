package com.toasttab.protokt.codegen.impl

import arrow.core.Option

fun <T : Any> Option<T>.require(): T =
    requireNotNull(orNull())
