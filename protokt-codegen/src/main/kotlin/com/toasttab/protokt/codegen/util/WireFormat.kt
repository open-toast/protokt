/*
 * Copyright (c) 2020 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.protokt.codegen.util

import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.Fixed32
import com.toasttab.protokt.rt.Fixed64
import com.toasttab.protokt.rt.Int32
import com.toasttab.protokt.rt.Int64
import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.SFixed32
import com.toasttab.protokt.rt.SFixed64
import com.toasttab.protokt.rt.SInt32
import com.toasttab.protokt.rt.SInt64
import com.toasttab.protokt.rt.UInt32
import com.toasttab.protokt.rt.UInt64
import kotlin.reflect.KClass

private val TYPE_0 =
    listOf(
        Boolean::class,
        KtEnum::class,
        Int32::class,
        Int64::class,
        SInt32::class,
        SInt64::class,
        UInt32::class,
        UInt64::class
    )

private val TYPE_1 =
    listOf(
        Double::class,
        Fixed64::class,
        SFixed64::class
    )

private val TYPE_2 =
    listOf(
        Bytes::class,
        KtMessage::class,
        String::class
    )

private val TYPE_5 =
    listOf(
        Float::class,
        Fixed32::class,
        SFixed32::class
    )

private val WIRE_TYPES =
    TYPE_0.associateWith { 0 } +
        TYPE_1.associateWith { 1 } +
        TYPE_2.associateWith { 2 } +
        TYPE_5.associateWith { 5 }

fun wireType(klass: KClass<*>) =
    WIRE_TYPES.getValue(klass)

fun computeTag(fieldNumber: Int, wireType: Int) =
    (fieldNumber shl 3) or wireType
