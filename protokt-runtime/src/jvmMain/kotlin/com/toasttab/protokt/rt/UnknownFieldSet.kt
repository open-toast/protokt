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

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
class UnknownFieldSet
private constructor(
    val unknownFields: Map<Int, Field>
) {
    fun size() =
        unknownFields.entries.sumOf { (k, v) -> v.size(k) }

    fun isEmpty() =
        unknownFields.isEmpty()

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

        fun from(builder: Builder?) =
            builder?.build() ?: EMPTY
    }

    class Builder {
        private val map = mutableMapOf<Int, Field.Builder>()

        fun add(unknown: UnknownField) {
            map.getOrPut(unknown.fieldNumber) { Field.Builder() }
                .add(unknown.value)
        }

        fun build() =
            UnknownFieldSet(finishMap(map.mapValues { (_, v) -> v.build() }))
    }

    class Field
    private constructor(
        val varint: List<VarintVal>,
        val fixed32: List<Fixed32Val>,
        val fixed64: List<Fixed64Val>,
        val lengthDelimited: List<LengthDelimitedVal>
    ) {
        private val size
            get() = varint.size + fixed32.size + fixed64.size + lengthDelimited.size

        fun size(fieldNumber: Int) =
            (sizeof(Tag(fieldNumber)) * size) + asSequence().sumOf { it.size() }

        private fun asSequence(): Sequence<UnknownValue> =
            (varint.asSequence() + fixed32 + fixed64 + lengthDelimited)

        fun write(fieldNumber: Int, serializer: KtMessageSerializer) {
            asSequence().forEach { serializer.write(it, fieldNumber) }
        }

        private fun KtMessageSerializer.write(
            unknownValue: UnknownValue,
            fieldNumber: Int
        ) {
            when (unknownValue) {
                is VarintVal -> write(fieldNumber, 0).write(unknownValue.value)
                is Fixed32Val -> write(fieldNumber, 5).write(unknownValue.value)
                is Fixed64Val -> write(fieldNumber, 1).write(unknownValue.value)
                is LengthDelimitedVal -> write(fieldNumber, 2).write(unknownValue.value)
            }
        }

        private fun KtMessageSerializer.write(fieldNumber: Int, wireType: Int) =
            write(Tag((fieldNumber shl 3) or wireType))

        override fun equals(other: Any?) =
            equalsUsingSequence(other, { it.size }, Field::asSequence)

        override fun hashCode() =
            hashCodeUsingSequence(asSequence())

        override fun toString(): String =
            "Field(" +
                "varint=$varint, " +
                "fixed32=$fixed32, " +
                "fixed64=$fixed64, " +
                "lengthDelimited=$lengthDelimited)"

        class Builder
        internal constructor() {
            private var varint: MutableList<VarintVal>? = null
            private var fixed32: MutableList<Fixed32Val>? = null
            private var fixed64: MutableList<Fixed64Val>? = null
            private var lengthDelimited: MutableList<LengthDelimitedVal>? = null

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

            fun add(unknown: UnknownValue) {
                when (unknown) {
                    is VarintVal -> varint().add(unknown)
                    is Fixed32Val -> fixed32().add(unknown)
                    is Fixed64Val -> fixed64().add(unknown)
                    is LengthDelimitedVal -> lengthDelimited().add(unknown)
                }
            }

            fun build() =
                Field(
                    finishList(varint),
                    finishList(fixed32),
                    finishList(fixed64),
                    finishList(lengthDelimited)
                )
        }
    }
}
