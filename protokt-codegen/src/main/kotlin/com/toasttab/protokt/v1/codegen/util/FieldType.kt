/*
 * Copyright (c) 2020 Toast, Inc.
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

package com.toasttab.protokt.v1.codegen.util

import com.toasttab.protokt.v1.Bytes
import com.toasttab.protokt.v1.Fixed32
import com.toasttab.protokt.v1.Fixed64
import com.toasttab.protokt.v1.Int32
import com.toasttab.protokt.v1.Int64
import com.toasttab.protokt.v1.KtEnum
import com.toasttab.protokt.v1.KtMessage
import com.toasttab.protokt.v1.KtMessageSerializer
import com.toasttab.protokt.v1.SFixed32
import com.toasttab.protokt.v1.SFixed64
import com.toasttab.protokt.v1.SInt32
import com.toasttab.protokt.v1.SInt64
import com.toasttab.protokt.v1.UInt32
import com.toasttab.protokt.v1.UInt64
import com.toasttab.protokt.v1.sizeOfSInt32
import com.toasttab.protokt.v1.sizeOfSInt64
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

    val writeFn
        get() =
            when (type.inlineRepresentation) {
                Int32::class -> "write"
                Fixed32::class -> KtMessageSerializer::writeFixed32.name
                SFixed32::class -> KtMessageSerializer::writeSFixed32.name
                UInt32::class -> KtMessageSerializer::writeUInt32.name
                SInt32::class -> KtMessageSerializer::writeSInt32.name
                Int64::class -> "write"
                Fixed64::class -> KtMessageSerializer::writeFixed64.name
                SFixed64::class -> KtMessageSerializer::writeSFixed64.name
                UInt64::class -> KtMessageSerializer::writeUInt64.name
                SInt64::class -> KtMessageSerializer::writeSInt64.name
                else -> "write"
            }

    sealed interface SizeFn
    class Const(val size: Int) : SizeFn
    class Method(val name: String) : SizeFn

    val sizeFn: SizeFn
        get() =
            when (this) {
                BOOL -> Const(1)
                DOUBLE -> Const(8)
                FLOAT -> Const(4)
                else ->
                    when (type.inlineRepresentation) {
                        Int32::class, UInt32::class, Int64::class, UInt64::class -> Method("sizeOf")
                        Fixed32::class, SFixed32::class -> Const(4)
                        Fixed64::class, SFixed64::class -> Const(8)
                        SInt32::class -> Method(::sizeOfSInt32.name)
                        SInt64::class -> Method(::sizeOfSInt64.name)
                        else -> Method("sizeOf")
                    }
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
    override val ktRepresentation: KClass<*>? = null
) : TypeImpl() {
    final override val scalar = false

    object Enum : Nonscalar(ktRepresentation = KtEnum::class)
    object Message : Nonscalar(ktRepresentation = KtMessage::class)
    object String : Nonscalar(kotlin.String::class)
    object Bytes : Nonscalar(com.toasttab.protokt.v1.Bytes::class)
}

private sealed class Scalar(
    override val kotlinRepresentation: KClass<*>? = null
) : TypeImpl() {
    final override val scalar = true

    object Bool : Scalar(Boolean::class)
    object Double : Scalar(kotlin.Double::class)
    object Float : Scalar(kotlin.Float::class)
}

private sealed class Boxed(
    override val inlineRepresentation: KClass<*>
) : Scalar() {
    final override val kotlinRepresentation
        get() = inlineRepresentation.declaredMemberProperties
            .single { it.name == com.toasttab.protokt.v1.Fixed32::value.name }
            .returnType
            .classifier as KClass<*>

    object Fixed32 : Boxed(com.toasttab.protokt.v1.Fixed32::class)
    object Fixed64 : Boxed(com.toasttab.protokt.v1.Fixed64::class)
    object Int32 : Boxed(com.toasttab.protokt.v1.Int32::class)
    object Int64 : Boxed(com.toasttab.protokt.v1.Int64::class)
    object SFixed32 : Boxed(com.toasttab.protokt.v1.SFixed32::class)
    object SFixed64 : Boxed(com.toasttab.protokt.v1.SFixed64::class)
    object SInt32 : Boxed(com.toasttab.protokt.v1.SInt32::class)
    object SInt64 : Boxed(com.toasttab.protokt.v1.SInt64::class)
    object UInt32 : Boxed(com.toasttab.protokt.v1.UInt32::class)
    object UInt64 : Boxed(com.toasttab.protokt.v1.UInt64::class)
}
