@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/label.proto
package com.google.api

import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.sizeof
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit

/**
 * A description of a label.
 */
@KtGeneratedMessage("google.api.LabelDescriptor")
class LabelDescriptor private constructor(
    /**
     * The label key.
     */
    val key: String,
    /**
     * The type of data that can be assigned to the label.
     */
    val valueType: ValueType,
    /**
     * A human-readable description for the label.
     */
    val description: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (key.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(key)
        }
        if (valueType.value != 0) {
            result += sizeof(Tag(2)) + sizeof(valueType)
        }
        if (description.isNotEmpty()) {
            result += sizeof(Tag(3)) + sizeof(description)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (key.isNotEmpty()) {
            serializer.write(Tag(10)).write(key)
        }
        if (valueType.value != 0) {
            serializer.write(Tag(16)).write(valueType)
        }
        if (description.isNotEmpty()) {
            serializer.write(Tag(26)).write(description)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is LabelDescriptor &&
        other.key == key &&
        other.valueType == valueType &&
        other.description == description &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + valueType.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    override fun toString(): String = "LabelDescriptor(" +
        "key=$key, " +
        "valueType=$valueType, " +
        "description=$description" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: LabelDescriptorDsl.() -> Unit): LabelDescriptor =
            LabelDescriptor.Deserializer {
        key = this@LabelDescriptor.key
        valueType = this@LabelDescriptor.valueType
        description = this@LabelDescriptor.description
        unknownFields = this@LabelDescriptor.unknownFields
        dsl()
    }

    class LabelDescriptorDsl {
        var key: String = ""

        var valueType: ValueType = ValueType.from(0)

        var description: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): LabelDescriptor = LabelDescriptor(key,
        valueType,
        description,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<LabelDescriptor>,
            (LabelDescriptorDsl.() -> Unit) -> LabelDescriptor {
        override fun deserialize(deserializer: KtMessageDeserializer): LabelDescriptor {
            var key = ""
            var valueType = ValueType.from(0)
            var description = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return LabelDescriptor(key,
                            valueType,
                            description,
                            UnknownFieldSet.from(unknownFields))
                    10 -> key = deserializer.readString()
                    16 -> valueType =
                            deserializer.readEnum(com.google.api.LabelDescriptor.ValueType)
                    26 -> description = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: LabelDescriptorDsl.() -> Unit): LabelDescriptor =
                LabelDescriptorDsl().apply(dsl).build()
    }

    /**
     * Value types that can be used as label values.
     */
    sealed class ValueType(
        override val `value`: Int,
        override val name: String,
    ) : KtEnum() {
        /**
         * A variable-length string. This is the default.
         */
        object STRING : ValueType(0, "STRING")

        /**
         * Boolean; true or false.
         */
        object BOOL : ValueType(1, "BOOL")

        /**
         * A 64-bit signed integer.
         */
        object INT64 : ValueType(2, "INT64")

        class UNRECOGNIZED(
            `value`: Int,
        ) : ValueType(value, "UNRECOGNIZED")

        companion object Deserializer : KtEnumDeserializer<ValueType> {
            override fun from(`value`: Int): ValueType = when (value) {
              0 -> STRING
              1 -> BOOL
              2 -> INT64
              else -> UNRECOGNIZED(value)
            }
        }
    }
}
