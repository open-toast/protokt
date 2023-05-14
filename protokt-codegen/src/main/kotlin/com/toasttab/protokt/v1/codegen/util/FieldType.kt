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
import com.toasttab.protokt.v1.KtEnum
import com.toasttab.protokt.v1.KtMessage
import com.toasttab.protokt.v1.KtMessageSerializer
import com.toasttab.protokt.v1.sizeOfSInt32
import com.toasttab.protokt.v1.sizeOfSInt64
import kotlin.reflect.KClass

enum class FieldType(
    private val type: TypeImpl
) {
    BOOL(Scalar.Bool),
    BYTES(Nonscalar.Bytes),
    DOUBLE(Scalar.Double),
    ENUM(Nonscalar.Enum),
    FIXED32(Scalar.Fixed32),
    FIXED64(Scalar.Fixed64),
    FLOAT(Scalar.Float),
    INT32(Scalar.Int32),
    INT64(Scalar.Int64),
    MESSAGE(Nonscalar.Message),
    SFIXED32(Scalar.SFixed32),
    SFIXED64(Scalar.SFixed64),
    SINT32(Scalar.SInt32),
    SINT64(Scalar.SInt64),
    STRING(Nonscalar.String),
    UINT32(Scalar.UInt32),
    UINT64(Scalar.UInt64);

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
            when (this) {
                INT32 -> "write"
                FIXED32 -> KtMessageSerializer::writeFixed32.name
                SFIXED32 -> KtMessageSerializer::writeSFixed32.name
                UINT32 -> KtMessageSerializer::writeUInt32.name
                SINT32 -> KtMessageSerializer::writeSInt32.name
                INT64 -> "write"
                FIXED64 -> KtMessageSerializer::writeFixed64.name
                SFIXED64 -> KtMessageSerializer::writeSFixed64.name
                UINT64 -> KtMessageSerializer::writeUInt64.name
                SINT64 -> KtMessageSerializer::writeSInt64.name
                else -> "write"
            }

    sealed interface SizeFn
    class Const(val size: Int) : SizeFn
    class Method(val name: String) : SizeFn

    val sizeFn: SizeFn
        get() =
            when (this) {
                BOOL -> Const(1)
                DOUBLE, FIXED64, SFIXED64 -> Const(8)
                FLOAT, FIXED32, SFIXED32 -> Const(4)
                INT32, UINT32, INT64, UINT64 -> Method("sizeOf")
                SINT32 -> Method(::sizeOfSInt32.name)
                SINT64 -> Method(::sizeOfSInt64.name)
                else -> Method("sizeOf")
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

    val scalar
        get() = this is Scalar

    val wireType
        get() = when (this) {
            Scalar.Bool,
            Nonscalar.Enum,
            Scalar.Int32,
            Scalar.Int64,
            Scalar.SInt32,
            Scalar.SInt64,
            Scalar.UInt32,
            Scalar.UInt64 -> 0

            Scalar.Double,
            Scalar.Fixed64,
            Scalar.SFixed64 -> 1

            Nonscalar.Bytes,
            Nonscalar.Message,
            Nonscalar.String -> 2

            Scalar.Float,
            Scalar.Fixed32,
            Scalar.SFixed32 -> 5
        }
}

private sealed class Nonscalar(
    override val kotlinRepresentation: KClass<*>? = null,
    override val ktRepresentation: KClass<*>? = null
) : TypeImpl() {
    object Enum : Nonscalar(ktRepresentation = KtEnum::class)
    object Message : Nonscalar(ktRepresentation = KtMessage::class)
    object String : Nonscalar(kotlin.String::class)
    object Bytes : Nonscalar(com.toasttab.protokt.v1.Bytes::class)
}

private sealed class Scalar(
    override val kotlinRepresentation: KClass<*>? = null
) : TypeImpl() {
    object Bool : Scalar(Boolean::class)
    object Double : Scalar(kotlin.Double::class)
    object Float : Scalar(kotlin.Float::class)
    object Fixed32 : Scalar(UInt::class)
    object Fixed64 : Scalar(ULong::class)
    object Int32 : Scalar(Int::class)
    object Int64 : Scalar(Long::class)
    object SFixed32 : Scalar(Int::class)
    object SFixed64 : Scalar(Long::class)
    object SInt32 : Scalar(Int::class)
    object SInt64 : Scalar(Long::class)
    object UInt32 : Scalar(UInt::class)
    object UInt64 : Scalar(ULong::class)
}
