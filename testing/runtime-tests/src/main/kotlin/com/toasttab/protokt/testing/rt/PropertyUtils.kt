package com.toasttab.protokt.testing.rt

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

fun KClass<*>.propertyIsMarkedNullable(name: String) =
    declaredMemberProperties
        .first { it.name == name }
        .returnType
        .isMarkedNullable
