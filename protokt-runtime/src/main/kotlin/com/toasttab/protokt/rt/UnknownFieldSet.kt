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

package com.toasttab.protokt.rt

class UnknownFieldSet
private constructor(
    val unknownFields: Map<Int, Field>
) {
    fun isNotEmpty() =
        unknownFields.isNotEmpty()

    fun size() =
        unknownFields.entries.sumBy { (k, v) -> v.size(k) }

    override fun equals(other: Any?) =
        other is UnknownFieldSet &&
            other.unknownFields == unknownFields

    override fun hashCode() =
        unknownFields.hashCode()

    override fun toString() =
        "UnknownFieldSet(unknownFields=$unknownFields)"

    companion object {
        private val EMPTY = UnknownFieldSet(emptyMap())

        fun empty() =
            EMPTY

        fun from(unknownFields: Map<Int, FieldBuilder>?) =
            if (unknownFields == null) {
                EMPTY
            } else {
                UnknownFieldSet(
                    copyMap(unknownFields.mapValues { (_, v) -> Field.from(v) })
                )
            }
    }
}

class Field
private constructor(
    val varint: List<VarintVal>,
    val fixed32: List<Fixed32Val>,
    val fixed64: List<Fixed64Val>,
    val lengthDelimited: List<LengthDelimitedVal>
) {
    fun size(fieldNumber: Int) =
        (sizeof(Tag(fieldNumber)) *
            (varint.size + fixed32.size + fixed64.size + lengthDelimited.size)) +
            asSequence().sumBy { it.size() }

    private fun asSequence(): Sequence<UnknownValue> =
        (varint.asSequence() + fixed32 + fixed64 + lengthDelimited)

    fun write(fieldNumber: Int, serializer: KtMessageSerializer) {
        asSequence().forEach { serializer.write(it, fieldNumber) }
    }

    private fun KtMessageSerializer.write(
        unknownValue: UnknownValue,
        fieldNumber: Int
    ) {
        write(fieldNumber, wireFormat(unknownValue))
        when (unknownValue) {
            is VarintVal -> write(unknownValue.value)
            is Fixed32Val -> write(unknownValue.value)
            is Fixed64Val -> write(unknownValue.value)
            is LengthDelimitedVal -> write(unknownValue.value)
        }
    }

    // In theory we ought to be able to simply call `unknownValue.value.wireFormat`.
    // If you actually try to do that, you get a ClassCastException along the lines of
    // `class java.lang.Long cannot be cast to class com.toasttab.protokt.rt.UInt64`.
    // This would appear to be a bug in Kotlin.
    private fun wireFormat(unknownValue: UnknownValue) =
        when (unknownValue) {
            is VarintVal -> unknownValue.value
            is Fixed32Val -> unknownValue.value
            is Fixed64Val -> unknownValue.value
            is LengthDelimitedVal -> unknownValue.value
            else -> error("unsupported type: ${unknownValue::class.simpleName}")
        }.wireFormat

    private fun KtMessageSerializer.write(fieldNumber: Int, wireFormat: Int) =
        write(Tag((fieldNumber shl 3) or wireFormat))

    override fun equals(other: Any?) =
        other is Field &&
            asSequence().zip(other.asSequence()).all { (a, b) -> a == b }

    override fun hashCode() =
        asSequence().sumBy { 31 * it.hashCode() }

    override fun toString(): String =
        "Field(" +
            "varint=$varint, " +
            "fixed32=$fixed32, " +
            "fixed64=$fixed64, " +
            "lengthDelimited=$lengthDelimited)"

    companion object {
        fun from(builder: FieldBuilder) =
            Field(
                finishList(builder.varint),
                finishList(builder.fixed32),
                finishList(builder.fixed64),
                finishList(builder.lengthDelimited)
            )
    }
}

class FieldBuilder {
    internal var varint: MutableList<VarintVal>? = null
    internal var fixed32: MutableList<Fixed32Val>? = null
    internal var fixed64: MutableList<Fixed64Val>? = null
    internal var lengthDelimited: MutableList<LengthDelimitedVal>? = null

    private fun varint() =
        varint.let {
            it ?: mutableListOf<VarintVal>()
                .apply { varint = this }
        }

    private fun fixed32() =
        fixed32.let {
            it ?: mutableListOf<Fixed32Val>()
                .apply { fixed32 = this }
        }

    private fun fixed64() =
        fixed64.let {
            it ?: mutableListOf<Fixed64Val>()
                .apply { fixed64 = this }
        }

    private fun lengthDelimited() =
        lengthDelimited.let {
            it ?: mutableListOf<LengthDelimitedVal>()
                .apply { lengthDelimited = this }
        }

    fun add(unknown: UnknownValue) =
        apply {
            when (unknown) {
                is VarintVal -> varint().add(unknown)
                is Fixed32Val -> fixed32().add(unknown)
                is Fixed64Val -> fixed64().add(unknown)
                is LengthDelimitedVal -> lengthDelimited().add(unknown)
            }
        }
}
