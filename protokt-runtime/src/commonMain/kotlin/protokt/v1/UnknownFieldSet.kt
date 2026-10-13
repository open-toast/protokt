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

package protokt.v1

import protokt.v1.Collections.freezeList
import protokt.v1.Collections.freezeMap
import protokt.v1.Sizes.sizeOf

class UnknownFieldSet private constructor(
    private val fieldMap: Map<UInt, Field>
) {
    operator fun get(fieldNumber: UInt): Field? =
        fieldMap[fieldNumber]

    operator fun contains(fieldNumber: UInt): Boolean =
        fieldNumber in fieldMap

    fun isEmpty(): Boolean =
        fieldMap.isEmpty()

    @OnlyForUseByGeneratedProtoCode
    fun size() =
        fieldMap.entries.sumOf { (fieldNumber, field) -> field.size(fieldNumber) }

    @OnlyForUseByGeneratedProtoCode
    fun forEach(action: (UInt, Field) -> Unit) {
        fieldMap.forEach { (k, v) -> action(k, v) }
    }

    override fun equals(other: Any?) =
        other is UnknownFieldSet &&
            other.fieldMap == fieldMap

    override fun hashCode() =
        fieldMap.hashCode()

    override fun toString() =
        "UnknownFieldSet(fields=$fieldMap)"

    companion object {
        private val EMPTY = UnknownFieldSet(emptyMap())

        fun empty() =
            EMPTY

        @OnlyForUseByGeneratedProtoCode
        fun from(builder: Builder?) =
            builder?.build() ?: EMPTY
    }

    class Builder {
        private val fields = linkedMapOf<UInt, Field.Builder>()

        fun add(unknown: UnknownField) {
            fields.getOrPut(unknown.fieldNumber) { Field.Builder() }
                .add(unknown.value)
        }

        fun build() =
            UnknownFieldSet(freezeMap(fields.mapValues { (_, field) -> field.build() }))
    }

    // If unknown fields are keyed by tag instead of field number then the bit
    // arithmetic in this class can go away, but field number is a useful thing
    // to have to trace the origin of the unknown field.
    class Field
    private constructor(
        internal val orderedValues: List<UnknownValue>
    ) {
        val varint = freezeList(orderedValues.filterIsInstance<VarintVal>())
        val fixed32 = freezeList(orderedValues.filterIsInstance<Fixed32Val>())
        val fixed64 = freezeList(orderedValues.filterIsInstance<Fixed64Val>())
        val lengthDelimited = freezeList(orderedValues.filterIsInstance<LengthDelimitedVal>())

        @OnlyForUseByGeneratedProtoCode
        fun size(fieldNumber: UInt) =
            orderedValues.sumOf { sizeOf(WireFormat.makeTag(fieldNumber, it.wireType)) + it.size() }

        @OnlyForUseByGeneratedProtoCode
        fun write(fieldNumber: UInt, serializer: Writer) {
            orderedValues.forEach { it.write(fieldNumber, serializer) }
        }

        override fun equals(other: Any?) =
            other is Field && other.orderedValues == orderedValues

        override fun hashCode() =
            orderedValues.hashCode()

        override fun toString(): String =
            "Field(" +
                "varint=$varint, " +
                "fixed32=$fixed32, " +
                "fixed64=$fixed64, " +
                "lengthDelimited=$lengthDelimited)"

        class Builder
        internal constructor() {
            private val values = mutableListOf<UnknownValue>()

            fun add(unknown: UnknownValue) {
                values.add(unknown)
            }

            fun build() =
                Field(freezeList(values))
        }
    }
}

private val UnknownValue.wireType
    get() =
        when (this) {
            is VarintVal -> WireFormat.WIRETYPE_VARINT
            is Fixed32Val -> WireFormat.WIRETYPE_FIXED32
            is Fixed64Val -> WireFormat.WIRETYPE_FIXED64
            is LengthDelimitedVal -> WireFormat.WIRETYPE_LENGTH_DELIMITED
        }

private fun UnknownValue.write(fieldNumber: UInt, serializer: Writer) {
    serializer.writeTag(WireFormat.makeTag(fieldNumber, wireType))
    when (val unknownValue = this) {
        is VarintVal -> serializer.writeUInt64(unknownValue.value)
        is Fixed32Val -> serializer.writeFixed32(unknownValue.value)
        is Fixed64Val -> serializer.writeFixed64(unknownValue.value)
        is LengthDelimitedVal -> serializer.write(unknownValue.value)
    }
}
