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

package com.toasttab.protokt.codegen.model

import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.wireType
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

enum class FieldType(
    private val type: TypeImpl
) {
    BOOL(Scalar.Bool),
    BYTES(Nonscalar.Bytes),
    DOUBLE(Scalar.Double),
    ENUM(Nonscalar.Enum),
    FIXED32(Boxed.Fixed32),
    FIXED64(Boxed.Fixed64),
    FLOAT(Scalar.Float),
    INT32(Boxed.Int32),
    INT64(Boxed.Int64),
    MESSAGE(Nonscalar.Message),
    SFIXED32(Boxed.SFixed32),
    SFIXED64(Boxed.SFixed64),
    SINT32(Boxed.SInt32),
    SINT64(Boxed.SInt64),
    STRING(Nonscalar.String),
    UINT32(Boxed.UInt32),
    UINT64(Boxed.UInt64);

    val protoktFieldType
        get() = when (type) {
            is Nonscalar.Bytes -> Bytes::class
            else ->
                requireNotNull(type.kotlinRepresentation) {
                    "no protokt field type for $this"
                }
        }

    val packable
        get() =
            type != Nonscalar.Bytes &&
                type != Nonscalar.Message &&
                type != Nonscalar.String

    val scalar
        get() = type.scalar

    val boxed
        get() = type.inlineRepresentation != null

    val boxer
        get() =
            requireNotNull(type.inlineRepresentation) {
                "no boxer for $this"
            }

    val wireType
        get() = type.wireType

    val kotlinRepresentation
        get() = type.kotlinRepresentation
}

private sealed class TypeImpl {
    open val kotlinRepresentation: KClass<*>? = null
    open val inlineRepresentation: KClass<*>? = null
    open val ktRepresentation: KClass<*>? = null

    abstract val scalar: Boolean

    val wireType
        get() = wireType(
            (inlineRepresentation ?: ktRepresentation ?: kotlinRepresentation)!!
        )
}

private sealed class Nonscalar(
    override val kotlinRepresentation: KClass<*>? = null,
    override val ktRepresentation: KClass<*>? = null,
) : TypeImpl() {
    override val scalar = false

    object Enum : Nonscalar(ktRepresentation = KtEnum::class)
    object Message : Nonscalar(ktRepresentation = KtMessage::class)
    object String : Nonscalar(kotlin.String::class)
    object Bytes : Nonscalar(
        ByteArray::class,
        com.toasttab.protokt.rt.Bytes::class
    )
}

private sealed class Scalar(
    override val kotlinRepresentation: KClass<*>? = null
) : TypeImpl() {
    override val scalar = true

    object Bool : Scalar(Boolean::class)
    object Double : Scalar(kotlin.Double::class)
    object Float : Scalar(kotlin.Float::class)
}

private sealed class Boxed(
    override val inlineRepresentation: KClass<*>
) : Scalar() {
    override val kotlinRepresentation
        get() = inlineRepresentation.declaredMemberProperties
            .single { it.name == com.toasttab.protokt.rt.Fixed32::value.name }
            .returnType
            .classifier as KClass<*>

    object Fixed32 : Boxed(com.toasttab.protokt.rt.Fixed32::class)
    object Fixed64 : Boxed(com.toasttab.protokt.rt.Fixed64::class)
    object Int32 : Boxed(com.toasttab.protokt.rt.Int32::class)
    object Int64 : Boxed(com.toasttab.protokt.rt.Int64::class)
    object SFixed32 : Boxed(com.toasttab.protokt.rt.SFixed32::class)
    object SFixed64 : Boxed(com.toasttab.protokt.rt.SFixed64::class)
    object SInt32 : Boxed(com.toasttab.protokt.rt.SInt32::class)
    object SInt64 : Boxed(com.toasttab.protokt.rt.SInt64::class)
    object UInt32 : Boxed(com.toasttab.protokt.rt.UInt32::class)
    object UInt64 : Boxed(com.toasttab.protokt.rt.UInt64::class)
}
